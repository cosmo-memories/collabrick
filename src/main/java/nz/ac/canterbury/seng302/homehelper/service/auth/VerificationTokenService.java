package nz.ac.canterbury.seng302.homehelper.service.auth;

import jakarta.mail.MessagingException;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.entity.user.VerificationToken;
import nz.ac.canterbury.seng302.homehelper.repository.auth.VerificationTokenRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.InvitationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service class for VerificationToken, defined by the @link{Service} annotation.
 * This class links automatically with @link{VerificationTokenRepository}, see @link{Autowired}
 * constructor below
 */
@Service
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final InvitationService invitationService;

    /**
     * Constructs a @code{VerificationTokenService} using verificationTokenRepository
     *
     * @param verificationTokenRepository the repository for handling verification tokens
     * @param userService                 the service for handling users
     * @param emailService                the service handling email related operations
     * @param invitationService           the invitation service for handling invitations
     */
    @Autowired
    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository, UserService userService, EmailService emailService, InvitationService invitationService) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.invitationService = invitationService;
    }

    /**
     * Sends a generated 6-digit code to a user's email
     *
     * @param user The user that the code is sent to
     */
    public void sendVerificationEmail(User user) throws MessagingException {
        // Deletes token before sending to prevent a user getting two tokens
        String verificationCode = generateVerificationEmailCode();
        verificationTokenRepository.deleteByUser(user);

        // Sets the expiry date 10 minutes in the future
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10);
        VerificationToken verificationToken = new VerificationToken(verificationCode, user, expiryDate);
        verificationTokenRepository.save(verificationToken);

        emailService.sendRegistrationVerificationMail(user, verificationToken);
    }

    /**
     * Verifies a user based on the provided verification token. It retrieves the verification token associated with
     * the given token, if the token is valid the associated user if marked as activated.
     *
     * @param token the verification token used to verify the user
     * @return true if the user was successfully verified and activated, false is the token was invalid or expired.
     */
    public boolean verifyUserByToken(String token) {
        Optional<VerificationToken> optionalVerificationToken = verificationTokenRepository.findByToken(token);
        if (optionalVerificationToken.isEmpty()) {
            return false;
        }

        VerificationToken verificationToken = optionalVerificationToken.get();
        if (verificationToken.isTokenExpired()) {
            verificationTokenRepository.delete(verificationToken);
            return false;
        }

        User user = verificationToken.getUser();
        user.setActivated(true);
        userService.saveUser(user);
        verificationToken.setUser(user);
        verificationTokenRepository.delete(verificationToken);
        invitationService.acceptInvitationsPendingRegistration(user.getEmail());
        return true;
    }

    /**
     * Deletes any expired tokens every 10 seconds
     */
    @Scheduled(fixedRate = 10000)
    public void cleanExpiredTokens() {
        // Gets all tokens that are expired
        /* Note: The LocalDateTime is gathered from the server that is running the application, not the user
        This means that if implemented on a separate server, a user cannot break this by changing their system's time */
        List<VerificationToken> expiredVerificationTokens = verificationTokenRepository.findByExpiryDateBefore(LocalDateTime.now());

        for (VerificationToken verificationToken : expiredVerificationTokens) {
            User user = verificationToken.getUser();
            verificationTokenRepository.delete(verificationToken);
            invitationService.setInvitationsPendingRegistrationToFalse(user.getEmail());

            if (!user.isActivated()) {
                userService.deleteUser(user);
            }
        }
    }

    /**
     * Generates a random 6-digit number that is not associated with an existing token.
     *
     * @return a unique 6-digit code (e.g. 012345) formatted as a string
     */
    public String generateVerificationEmailCode() {
        String code;
        do {
            code = generateCode();
        } while (verificationTokenRepository.findByToken(code).isPresent());
        return code;
    }

    /**
     * Generates a random 6-digit number formatted to a string of 6 characters long.
     *
     * @return a 6-digit code (e.g. 012345) formatted as a string
     */
    public String generateCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }
}
