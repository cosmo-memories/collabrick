package nz.ac.canterbury.seng302.homehelper.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.InvitationException;
import nz.ac.canterbury.seng302.homehelper.exceptions.token.TokenExpiredException;
import nz.ac.canterbury.seng302.homehelper.exceptions.token.TokenInvalidException;
import nz.ac.canterbury.seng302.homehelper.model.activity.ActivityType;
import nz.ac.canterbury.seng302.homehelper.service.activity.ActivityService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.InvitationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import nz.ac.canterbury.seng302.homehelper.validation.renovation.InvitationValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Controller for handling renovation invitations
 */
@Controller
public class InvitationController {

    private final Logger logger = LoggerFactory.getLogger(InvitationController.class);
    private final InvitationService invitationService;
    private final RenovationService renovationService;
    private final UserService userService;
    private final ActivityService activityService;

    @Autowired
    public InvitationController(InvitationService invitationService, RenovationService renovationService, UserService userService, ActivityService activityService) {
        this.invitationService = invitationService;
        this.renovationService = renovationService;
        this.userService = userService;
        this.activityService = activityService;
    }

    /**
     * Sends invitations to a list of email addresses for a given renovation.
     *
     * @param id       the renovation ID
     * @param invitees a list of invitee email addresses; may be null or empty
     * @param model    the Spring model to pass attributes to the view
     * @param request  the HTTP servlet request, used to identify the logged-in user
     * @return a redirect to the renovation's page on success, or the individual renovation page with
     * error messages if validation fails
     * @throws NoResourceFoundException if the renovation does not exist or the user is not the owner
     */
    @PostMapping("/myRenovations/{id}/invite")
    public String inviteMembersToRenovation(@PathVariable String id,
                                            @RequestParam(name = "invitees[]", required = false) ArrayList<String> invitees,
                                            Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) throws NoResourceFoundException {

        User user = UserUtil.getUserFromHttpServletRequest(userService, request);
        Renovation renovation = renovationService.getRenovation(Long.parseLong(id))
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Renovation not found"));

        if (!renovation.getOwner().equals(user)) {
            logger.error("User {} invite users to {} without permission", user.getId(), renovation.getId());
            throw new NoResourceFoundException(HttpMethod.GET, "Renovation not found");
        }

        logger.info("Invitees {}", invitees);

        try {
            // Send invites
            invitationService.validateInvitationData(invitees, renovation);
            for (String email : invitees) {
                Invitation invitation = invitationService.createInvite(email, renovation);
                invitationService.sendInvitationEmail(invitation);
            }
            redirectAttributes.addFlashAttribute("showToast", true);
            return "redirect:/renovation/" + id + "/members";
        } catch (InvitationException e) {
            // One or more validation errors occurred
            model.addAttribute("renovation", renovation);
            if (e.getMessages().contains(InvitationValidation.USER_LIST_EMPTY_ERROR)) {
                model.addAttribute("invitationErrorMessage", InvitationValidation.USER_LIST_EMPTY_ERROR);
            }
            // Frontend already handles input errors so these are just for safety on backend
            // Don't need to display to the user at this point
            return "redirect:/renovation/" + id;
        }
    }

    /**
     * Opens an invitation link based on a provided token.
     * If the user is not logged in, they will be redirected to login or registration,
     * depending on whether they already have an account.
     * If logged in and the email matches the invitation, the invitation will be accepted.
     *
     * @param model              the Spring model to populate view attributes
     * @param invitationToken    the unique invitation token
     * @param request            the HTTP servlet request, used to retrieve the logged-in user
     * @param redirectAttributes attributes to pass in redirects
     * @return a redirect to the appropriate page.
     */
    @GetMapping("/invitation")
    public String openInvitation(Model model,
                                 @RequestParam(name = "token") String invitationToken,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            Invitation invitation = invitationService.validateInvitationToken(invitationToken);
            Optional<User> optionalUser = UserUtil.getOptionalUserFromHttpServletRequest(userService, request);

            if (optionalUser.isEmpty()) {
                // user is not logged in
                User user = userService.findUserByEmail(invitation.getEmail());
                User dto = new User();
                redirectAttributes.addFlashAttribute("user", dto);

                if (user != null) {
                    dto.setEmail(user.getEmail());
                    return "redirect:/login?redirect=" +
                            UriUtils.encode("/invitation?token=" + invitationToken, StandardCharsets.UTF_8);
                } else {
                    dto.setEmail(invitation.getEmail());
                    redirectAttributes.addFlashAttribute("location", new Location());
                    invitationService.markAsAcceptedPendingRegistration(invitation);

                    return "redirect:/register";
                }
            } else {
                // user is logged in
                User user = UserUtil.getUserFromHttpServletRequest(userService, request);
                if (user.getEmail().equals(invitation.getEmail())) {
                    invitationService.acceptInvitation(invitation);
                    return "redirect:/renovation/" + invitation.getRenovation().getId();
                }
                return renderExpiredOrInvalidPage(model, invitationToken, false);
            }
        } catch (TokenExpiredException e) {
            logger.error(e.getMessage());
            return renderExpiredOrInvalidPage(model, invitationToken, true);

        } catch (TokenInvalidException e) {
            logger.error(e.getMessage());
            return renderExpiredOrInvalidPage(model, invitationToken, false);

        } catch (Exception e) {
            logger.error(e.getMessage());
            return "pages/notFoundPage";
        }
    }

    /**
     * Declines an invitation using a provided token.
     *
     * @param model           the Spring model to populate view attributes
     * @param invitationToken the unique invitation token
     * @return the declined invitation page if successful, or the expired/invalid page if
     * the token is invalid or expired
     */
    @GetMapping("/decline-invitation")
    public String declineInvitation(Model model, @RequestParam(name = "token") String invitationToken) {
        try {
            Invitation invitation = invitationService.validateInvitationToken(invitationToken);

            model.addAttribute("invitee", invitation.getEmail());
            model.addAttribute("renovationName", invitation.getRenovation().getName());
            model.addAttribute("renovationOwner", invitation.getRenovation().getOwner().getFname() + " "
                    + invitation.getRenovation().getOwner().getLname());
            invitationService.declineInvitation(invitation);

            // LiveUpdate notification for renovation owner:
            if (userService.findUserByEmail(invitation.getEmail()) == null) {
                LiveUpdate update = new LiveUpdate(null, invitation.getRenovation(), ActivityType.INVITE_DECLINED, invitation);
                try {
                    activityService.saveLiveUpdate(update);
                    activityService.sendUpdate(update);
                } catch (Exception e) {
                    logger.error("Failed to send live update", e);
                }
            } else {
                LiveUpdate update = new LiveUpdate(userService.findUserByEmail(invitation.getEmail()), invitation.getRenovation(), ActivityType.INVITE_DECLINED, invitation);
                try {
                    activityService.saveLiveUpdate(update);
                    activityService.sendUpdate(update);
                } catch (Exception e) {
                    logger.error("Failed to send live update", e);
                }
            }

        } catch (TokenExpiredException e) {
            logger.error(e.getMessage());
            return renderExpiredOrInvalidPage(model, invitationToken, true);

        } catch (TokenInvalidException e) {
            logger.error(e.getMessage());
            return renderExpiredOrInvalidPage(model, invitationToken, false);

        } catch (Exception e) {
            logger.error(e.getMessage());
            return "pages/notFoundPage";
        }
        return "pages/declinedInvitationPage";
    }

    /**
     * Displays the expired or invalid invitation page.
     *
     * @return the view name for the expired/invalid invitation page
     */
    @GetMapping("/invitation/expiredInvalidInvitation")
    public String invitationExpired() {
        return "pages/expiredInvalidInvitationPage";
    }

    /**
     * Helper method to populate the model for the expired/invalid invitation page and return the page.
     *
     * @param model           the model of the expired/invalid page
     * @param invitationToken the invitation token that was caught by the exception
     * @param isExpired       boolean of if the exception was TokenExpiredException or TokenInvalidException
     * @return the expired/invalid page to show
     */
    private String renderExpiredOrInvalidPage(Model model, String invitationToken, boolean isExpired) {
        Invitation invitation = invitationService.getInvitation(invitationToken).orElse(null);
        if (invitation != null) {
            model.addAttribute("isInviteExpired", isExpired);
            model.addAttribute("invitee", invitation.getEmail());
            model.addAttribute("renovationName", invitation.getRenovation().getName());
            model.addAttribute("renovationOwner", invitation.getRenovation().getOwner().getFname() + " "
                    + invitation.getRenovation().getOwner().getLname());
            return "pages/expiredInvalidInvitationPage";
        }
        return "pages/notFoundPage";
    }



    /**
     * Handles POST requests to remove a pending invitation from a renovation.
     * Only the owner of the renovation can remove (expire) an invitation. If the user is not
     * logged in or does not own the renovation, the user is redirected to a "not found" page.
     *
     * @param renovationId        The ID of the renovation where the invitation is being removed.
     * @param invitation          The {@link Invitation} object to be removed/expired.
     * @param request             The HTTP request used to get the current logged-in user.
     * @param redirectAttributes  Flash attributes used to show confirmation toasts upon redirection.
     * @return A redirect string to the renovation members page, or a not found page if unauthorized.
     */
    @PostMapping("/renovation/{renovationId}/removeInvitation")
    public String removeRenovationMember(@PathVariable String renovationId,
                                         @RequestParam(name = "invitation") Invitation invitation,
                                         HttpServletRequest request, RedirectAttributes redirectAttributes){
        Optional<User> optionalUser = UserUtil.getOptionalUserFromHttpServletRequest(userService, request);
        if (optionalUser.isEmpty()) {
            return "pages/notFoundPage";
        }
        User user = UserUtil.getUserFromHttpServletRequest(userService, request);
        Renovation renovation = invitation.getRenovation();
        if(!user.getEmail().equals(renovation.getOwner().getEmail())) {
            return  "pages/notFoundPage";
        }
        if(!invitation.isResolved()) {
            invitationService.expireInvitation(invitation);
            redirectAttributes.addFlashAttribute("showRemoveConfirmationToast", true);
            if(invitation.getUser() == null) {
                redirectAttributes.addFlashAttribute("removee", invitation.getEmail());
            } else {
                redirectAttributes.addFlashAttribute("removee", invitation.getUser().getFname() + " " + invitation.getUser().getLname());
            }
        }
        return "redirect:/renovation/" + renovationId + "/members";


    }
}
