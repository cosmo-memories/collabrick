package nz.ac.canterbury.seng302.homehelper.service.renovation;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.InvitationException;
import nz.ac.canterbury.seng302.homehelper.exceptions.token.TokenExpiredException;
import nz.ac.canterbury.seng302.homehelper.exceptions.token.TokenInvalidException;
import nz.ac.canterbury.seng302.homehelper.model.activity.ActivityType;
import nz.ac.canterbury.seng302.homehelper.model.renovation.InvitationStatus;
import nz.ac.canterbury.seng302.homehelper.model.user.PublicUserDetailsRenovation;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.InvitationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationMemberRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.activity.ActivityService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import nz.ac.canterbury.seng302.homehelper.validation.renovation.InvitationValidation;
import nz.ac.canterbury.seng302.homehelper.validation.user.UserValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole.MEMBER;

/**
 * Service responsible for handling invitation related operations.
 */
@Service
public class InvitationService {

    private final Logger logger = LoggerFactory.getLogger(InvitationService.class);

    private final EmailService emailService;
    private final UserService userService;
    private final InvitationRepository invitationRepository;
    private final RenovationMemberRepository renovationMemberRepository;
    private final RenovationService renovationService;
    private final ChatChannelService chatChannelService;
    private final BrickAiService brickAiService;
    private final ActivityService activityService;

    @Autowired
    public InvitationService(EmailService emailService, UserService userService,
                             RenovationMemberRepository renovationMemberRepository, InvitationRepository invitationRepository,
                             RenovationService renovationService, ChatChannelService chatChannelService, BrickAiService brickAiService, ActivityService activityService) {
        this.emailService = emailService;
        this.userService = userService;
        this.renovationMemberRepository = renovationMemberRepository;
        this.invitationRepository = invitationRepository;
        this.renovationService = renovationService;
        this.chatChannelService = chatChannelService;
        this.brickAiService = brickAiService;
        this.activityService = activityService;
    }

    /**
     * Send an email informing recipient about the provided Invitation.
     * Probably redundant, consider removing.
     *
     * @param invitation Invitation object
     */
    public void sendInvitationEmail(Invitation invitation) {
        emailService.sendInvitationMail(invitation);
    }

    public void saveInvitation(Invitation invitation) {
        invitationRepository.save(invitation);
    }

    public Optional<Invitation> getInvitation(String invitationId) {
        return invitationRepository.findByInvitationId(UUID.fromString(invitationId));
    }

    /**
     * Finds a list of public user detail suggestions based on a user's past collaboration history
     * and existing invitations for a specific renovation. The resulting list contains unique users or invitees
     * whose email matches the given input. Each user detail object includes metadata indicating whether the
     * user is already a member of the renovation or has been invited to it.
     *
     * @param user       the user performing the search.
     * @param renovation the renovation for which collaborators/invitees are being queried.
     * @param input      the partial email to autocomplete against.
     * @return a list of distinct public user details matching the input, including membership/invitation status.
     */
    public List<PublicUserDetailsRenovation> findUserAutoCompletionMatches(User user, Renovation renovation, String input) {
        Set<Long> memberIds = renovation.getMembers().stream()
                .map(u -> u.getUser().getId())
                .collect(Collectors.toSet());
        List<Invitation> invitations = invitationRepository.findByRenovationOwner(user, input);
        List<PublicUserDetailsRenovation> memberDetails = getMemberDetails(renovation, user, input, memberIds);
        List<PublicUserDetailsRenovation> invitationDetails = getInvitationDetails(invitations);
        Map<String, PublicUserDetailsRenovation> uniqueByEmail = new LinkedHashMap<>();
        memberDetails.forEach(details -> uniqueByEmail.put(details.getEmail(), details));

        invitationDetails.forEach(details -> {
            if (details.isInvited() && details.getRenovationId() != renovation.getId()) {
                details.setInvited(false);
            }

            PublicUserDetailsRenovation existing = uniqueByEmail.get(details.getEmail());
            if (existing == null) {
                // if the email is not in uniqueByEmail yet add them
                uniqueByEmail.put(details.getEmail(), details);
                return;
            }


            // if the user is a member (already in uniqueByEmail) then disregard it (we want to keep the entry that
            // shows they are a member)
            if (existing.isMember()) {
                return;
            }

            // if they are already marked as invited on this renovation, then disregard (they will have been invited to
            // another renovation)
            if (existing.getRenovationId() == renovation.getId() && existing.isInvited()) {
                return;
            }

            uniqueByEmail.put(details.getEmail(), details);
        });

        logger.info("Found {} users that user with ID {} has collaborated with", uniqueByEmail.size(), user.getId());
        return new ArrayList<>(uniqueByEmail.values());
    }

    /**
     * Retrieves a list of collaborators the given user has previously worked with, filtered
     * by the provided input. Adds flags indicating whether each user is already a member
     * of the current renovation.
     *
     * @param user      the user performing the search.
     * @param input     the name or email input to match against collaborators.
     * @param memberIds a set of user IDs that are already members of the renovation.
     * @return a list of matching public user details for collaborators.
     */
    private List<PublicUserDetailsRenovation> getMemberDetails(Renovation renovation, User user, String input, Set<Long> memberIds) {
        return renovationMemberRepository.findCollaboratorsInRenovations(user, input)
                .stream()
                .map(u -> new PublicUserDetailsRenovation(u, memberIds.contains(u.getId()), false, renovation.getId()))
                .toList();
    }

    /**
     * Converts a list of renovation invitations into public user detail objects.
     * For each invitation:
     * - If the invite is linked to a registered user, use their details.
     * - If not, attempt to resolve the user by email.
     * - If resolution fails, return a placeholder user detail using the invitation's email.
     *
     * @param invitations the list of invitations associated with the renovation.
     * @return a list of public user details representing the invitees.
     */
    private List<PublicUserDetailsRenovation> getInvitationDetails(List<Invitation> invitations) {
        return invitations.stream()
                .map(invite -> {
                    User invitedUser = invite.getUser();
                    if (invitedUser != null) {
                        return new PublicUserDetailsRenovation(invitedUser, invite.getRenovation().getId(), invite.getInvitationStatus());
                    }

                    String email = invite.getEmail();
                    User resolvedUser = userService.findUserByEmail(email);
                    if (resolvedUser != null) {
                        return new PublicUserDetailsRenovation(resolvedUser, invite.getRenovation().getId(), invite.getInvitationStatus());
                    }

                    return new PublicUserDetailsRenovation(-1, "", "", email, null, invite.getRenovation().getId(), invite.getInvitationStatus());
                })
                .toList();
    }

    /**
     * Finds an Invitation for the given User and Renovation if it exists.
     *
     * @param user User
     * @param reno Renovation
     * @return Invite
     */
    public Optional<Invitation> findByRenovationAndUser(User user, Renovation reno) {
        Optional<Invitation> invite = invitationRepository.findByRenovationAndUser(reno, user);
        if (invite.isEmpty()) {
            logger.info("Invite for user {} {} and renovation {} not found", user.getFname(), user.getLname(), reno.getId());
        } else {
            logger.info("Found invite for user {} {} for renovation {}", user.getFname(), user.getLname(), reno.getId());
        }
        return invite;
    }

    /**
     * Finds a list of Invitations for the specified Renovation.
     *
     * @param reno Renovation
     * @return Invitation list
     */
    public List<Invitation> findInvitesByRenovation(Renovation reno) {
        List<Invitation> invites = invitationRepository.findByRenovation(reno);
        logger.info("Found {} invites for renovation {}", invites.size(), reno.getId());
        return invites;
    }

    /**
     * Finds a list of Invitations for the specified User.
     *
     * @param user User
     * @return Invitation list
     */
    public List<Invitation> findInvitesByUser(User user) {
        List<Invitation> invites = invitationRepository.findByUser(user);
        logger.info("Found {} invites for user {} {}", invites.size(), user.getFname(), user.getLname());
        return invites;
    }

    /**
     * Finds a list of Invitations for the specified email address.
     *
     * @param email Email address
     * @return Invitation list
     */
    public List<Invitation> findInvitesByEmail(String email) {
        List<Invitation> invites = invitationRepository.findByEmail(email);
        logger.info("Found {} invites for email {}", invites.size(), email);
        return invites;
    }

    /**
     * Create a new Invitation for the specified Renovation.
     * The User with the given email will be invited if they exist, otherwise an invitation is sent to the unregistered email.
     *
     * @param email Email to send invite to; user with this email may or may not exist
     * @param reno  Renovation to invite the recipient to
     * @return New Invitation object
     */
    public Invitation createInvite(String email, Renovation reno) throws IllegalArgumentException {
        String emailValidationError = UserValidation.validateEmailFormat(email);
        if (!emailValidationError.isEmpty()) {
            throw new IllegalArgumentException(emailValidationError);
        }

        invitationRepository.findByRenovationAndEmail(reno, email)
                .forEach(invitation -> {
                    InvitationStatus status = invitation.getInvitationStatus();
                    if (status == InvitationStatus.EXPIRED || status == InvitationStatus.DECLINED) {
                        invitationRepository.delete(invitation);
                    }
                });


        User user = userService.findUserByEmail(email);
        Invitation invite;

        if (user == null) {
            invite = new Invitation(email, reno);
        } else {
            invite = new Invitation(user, reno);
        }
        invitationRepository.save(invite);
        return invite;


    }

    /**
     * Helper method to validate and return an invitation for accept/decline flow.
     *
     * @param token the invitation token from request
     * @return the Invitation object if valid, or throws TokenInvalidException / TokenExpiredException
     */
    public Invitation validateInvitationToken(String token) {
        Optional<Invitation> invitationOpt = getInvitation(token);

        // Check if token exists
        if (invitationOpt.isEmpty()) {
            throw new TokenInvalidException("Token invalid");
        }

        Invitation invitation = invitationOpt.get();
        // Check for expired
        if (invitation.getInvitationStatus() == InvitationStatus.EXPIRED) {
            throw new TokenExpiredException("Token expired");
        }

        // Check for non-pending
        if (invitation.getInvitationStatus() != InvitationStatus.PENDING) {
            throw new TokenInvalidException("Token invalid");
        }

        return invitation;
    }

    /**
     * Accepts an invitation.
     * Updates the renovation with the new member and marks the invitation as resolved.
     * Also adds member to renovation general chat and creates a private AI chat channel for them.
     *
     * @param invitation invitation to accept and resolve
     */
    @Transactional
    public void acceptInvitation(Invitation invitation) {
        try {
            User realUser = userService.findUserByEmail(invitation.getEmail());
            Renovation renovation = invitation.getRenovation();
            renovation.addMember(realUser, MEMBER);
            renovationService.saveRenovation(renovation, renovation.getOwner());
            Optional<ChatChannel> optChannel = chatChannelService.findByRenovationAndName(renovation, "general");
            if (optChannel.isPresent()) {
                ChatChannel channel = optChannel.get();
                chatChannelService.addMemberToChatChannel(channel.getId(), realUser.getId());
            }
            brickAiService.createAiChannel(renovation, realUser);
            invitation.acceptInvitation();
            saveInvitation(invitation);

            // LiveUpdate notification for renovation owner:
            LiveUpdate update = new LiveUpdate(userService.findUserByEmail(invitation.getEmail()), invitation.getRenovation(), ActivityType.INVITE_ACCEPTED, invitation);
            try {
                activityService.saveLiveUpdate(update);
                activityService.sendUpdate(update);
            } catch (Exception e) {
                logger.error("Failed to send live update", e);
            }

        } catch (Exception e) {
            logger.error("Error accepting invitation that has already been resolved", e);
        }
    }

    /**
     * Declines an invitation
     *
     * @param invitation invitation to be declined
     */
    public void declineInvitation(Invitation invitation) {
        try {
            invitation.declineInvitation();
            saveInvitation(invitation);
        } catch (Exception e) {
            logger.error("Error declining invitation that has already been resolved", e);
        }
    }

    /**
     * Marks an invitation as accepted pending registration.
     *
     * @param invitation the invitation to mark as registration
     */
    public void markAsAcceptedPendingRegistration(Invitation invitation) {
        invitation.setAcceptedPendingRegistration(true);
        saveInvitation(invitation);
    }

    /**
     * Expires an invitation
     *
     * @param invitation invitation to be expried
     */
    public void expireInvitation(Invitation invitation) {
        invitation.expireInvitation();
        saveInvitation(invitation);
    }


    /**
     * Every 10 seconds, set the status of any expired invitation to expired
     * For the invitation to be determined to be expired, it must be PENDING, not ACCEPTED/DECLINED
     */
    @Scheduled(fixedRate = 10000)
    public void expireInvitations() {
        /* Note: The LocalDateTime is gathered from the server that is running the application, not the user
        This means that if implemented on a separate server, a user cannot break this by changing their system's time */
        List<Invitation> expiredInvitation = invitationRepository.findByExpiryDateBefore(LocalDateTime.now());

        for (Invitation invitation : expiredInvitation) {
            if (invitation.getInvitationStatus() == InvitationStatus.PENDING) {
                expireInvitation(invitation);
            }
        }

    }


    /**
     * Perform all Invitation validation steps on supplied information.
     *
     * @param emails     List of emails to invite
     * @param renovation Renovation to invite to
     */
    public void validateInvitationData(ArrayList<String> emails, Renovation renovation) {
        // For later refactoring: make this less wildly inefficient
        InvitationException exception = new InvitationException();
        boolean valid = true;

        // Check list is nonempty
        String listEmptyError = InvitationValidation.checkUsers(emails);
        if (!listEmptyError.isEmpty()) {
            valid = false;
            exception.addEmail("");
            exception.addMessage(listEmptyError);
            throw exception;
        }

        // Check all emails are in a valid format
        for (String email : emails) {
            String emailValidationError = UserValidation.validateEmailFormat(email);
            if (!emailValidationError.isEmpty()) {
                valid = false;
                exception.addEmail(email);
                exception.addMessage(emailValidationError);
            }
        }

        // Check Renovation owner's email is not in list
        User owner = renovation.getOwner();
        String ownerEmailError = InvitationValidation.checkSelf(emails, owner);
        if (!ownerEmailError.isEmpty()) {
            valid = false;
            exception.addEmail(owner.getEmail());
            exception.addMessage(ownerEmailError);
        }

        // Check there are no duplicates in list
        for (String email : emails) {
            String duplicateUserError = InvitationValidation.checkDuplicates(emails, email);
            if (!duplicateUserError.isEmpty()) {
                valid = false;
                exception.addEmail(email);
                exception.addMessage(duplicateUserError);
            }
        }

        // Check no one in the list has been invited to this Reno already
        for (String email : emails) {
            String userAlreadyInvitedError = InvitationValidation.checkEmail(invitationRepository.findByRenovation(renovation), email);
            if (!userAlreadyInvitedError.isEmpty()) {
                valid = false;
                exception.addEmail(email);
                exception.addMessage(userAlreadyInvitedError);
            }
        }

        // Check emails are not already members of this Reno
        for (String email : emails) {
            String emailAlreadyAMemberError = InvitationValidation.checkEmailMembership(renovation.getMembers(), email);
            if (!emailAlreadyAMemberError.isEmpty()) {
                valid = false;
                exception.addEmail(email);
                exception.addMessage(emailAlreadyAMemberError);
            }
        }

        // If errors occurred:
        if (!valid) {
            throw exception;
        }
        //Otherwise everything is valid
    }

    /**
     * Accepts all invitations for the given email that are marked as accepted
     * but pending user registration.
     *
     * @param email the email address associated with the pending invitations
     */
    public void acceptInvitationsPendingRegistration(String email) {
        List<Invitation> invitations = invitationRepository.findByEmailAndAcceptedPendingRegistrationIsTrue(email);
        invitations.forEach(this::acceptInvitation);
    }

    /**
     * Updates all invitations for the given email that are marked as accepted but pending registration by setting
     * their acceptedPendingRegistration flag to false.
     *
     * @param email the email address associated with the pending invitations
     */
    public void setInvitationsPendingRegistrationToFalse(String email) {
        List<Invitation> invitations = invitationRepository.findByEmailAndAcceptedPendingRegistrationIsTrue(email);
        invitations.forEach(invitation -> invitation.setAcceptedPendingRegistration(false));
        invitationRepository.saveAll(invitations);
    }


    /**
     * Deletes an invitation by renovation and user
     * @param renovation the renovation
     * @param user the user
     */
    public void deleteInvitation(Renovation renovation, User user) {
        invitationRepository.deleteByRenovationAndEmail(renovation, user.getEmail());
    }
}
