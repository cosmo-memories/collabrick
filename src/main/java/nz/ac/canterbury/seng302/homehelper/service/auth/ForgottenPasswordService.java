package nz.ac.canterbury.seng302.homehelper.service.auth;

import nz.ac.canterbury.seng302.homehelper.entity.user.ForgottenPasswordToken;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UserUpdatePasswordException;
import nz.ac.canterbury.seng302.homehelper.exceptions.token.TokenExpiredException;
import nz.ac.canterbury.seng302.homehelper.exceptions.token.TokenInvalidException;
import nz.ac.canterbury.seng302.homehelper.repository.auth.ForgottenPasswordTokenRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.validation.user.UserValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for handling forgotten password tokens. This service provides methods to create, find, and validate
 * forgotten password tokens.
 */
@Service
public class ForgottenPasswordService {

    /**
     * The duration for which a token remains valid before expiration.
     */
    private static final TemporalAmount TOKEN_EXPIRATION = Duration.ofMinutes(10);
    private final Logger logger = LoggerFactory.getLogger(ForgottenPasswordService.class);
    private final EmailService emailService;

    private final UserService userService;

    private final ForgottenPasswordTokenRepository forgottenPasswordTokenRepository;

    /**
     * Constructs a new ForgottenPasswordService.
     *
     * @param emailService                     The service responsible for sending emails.
     * @param userService                      The service for user-related operations.
     * @param forgottenPasswordTokenRepository The repository for managing forgotten password tokens.
     */
    @Autowired
    public ForgottenPasswordService(EmailService emailService, UserService userService, ForgottenPasswordTokenRepository forgottenPasswordTokenRepository) {
        this.emailService = emailService;
        this.userService = userService;
        this.forgottenPasswordTokenRepository = forgottenPasswordTokenRepository;
    }

    /**
     * Sends a password reset email to the specified email address.
     *
     * @param email the email of the user requesting the password reset.
     * @throws IllegalArgumentException If the email format is invalid.
     */
    public void sendResetPasswordEmail(String email) {
        String emailValidationError = UserValidation.validateEmailFormat(email);
        if (!emailValidationError.isEmpty()) {
            throw new IllegalArgumentException(emailValidationError);
        }

        User user = userService.findUserByEmail(email);
        if (user == null) {
            return;
        }

        createForgottenPasswordToken(user).ifPresent(forgottenPasswordToken ->
                emailService.sendResetPasswordMail(user, forgottenPasswordToken));
    }

    /**
     * Resets the user's password if the provided token is valid.
     *
     * @param token                The password reset token.
     * @param newPassword          The new password.
     * @param confirmedNewPassword The confirmed new password.
     * @throws TokenInvalidException       If the token is invalid.
     * @throws TokenExpiredException       If the token has expired.
     * @throws UserUpdatePasswordException if the old password is incorrect, the new password does not meet strength
     *                                     requirements or the new passwords do not match.
     */
    public void resetPassword(String token, String newPassword, String confirmedNewPassword) {
        ForgottenPasswordToken forgottenPasswordToken = findForgottenPasswordToken(token);
        User user = forgottenPasswordToken.getUser();
        userService.updateUserPassword(user, newPassword, confirmedNewPassword);
        forgottenPasswordTokenRepository.delete(forgottenPasswordToken);
    }

    /**
     * Finds a forgotten password token by its unique identifier and validates its expiration.
     *
     * @param token the forgotten password token as a string
     * @return the forgotten password token
     * @throws TokenInvalidException If the token is invalid.
     * @throws TokenExpiredException If the token has expired.
     */
    public ForgottenPasswordToken findForgottenPasswordToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new TokenInvalidException("Reset password link is invalid");
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(token);
        } catch (IllegalArgumentException e) {
            throw new TokenInvalidException("Reset password link is invalid");
        }

        return findForgottenPasswordToken(uuid)
                .orElseThrow(() -> new TokenExpiredException("Reset password link has expired"));
    }

    /**
     * Finds a forgotten password token by its unique identifier and validates its expiration.
     *
     * @param token the UUID of the forgotten password token
     * @return an Optional containing the token if valid, or empty if expired or not found
     */
    public Optional<ForgottenPasswordToken> findForgottenPasswordToken(UUID token) {
        return forgottenPasswordTokenRepository.findById(token)
                .flatMap(this::validateTokenOrDeleteFilter);
    }

    /**
     * Finds a forgotten password token associated with a given user and validates its expiration.
     *
     * @param user the user whose token is to be found
     * @return an Optional containing the token if valid, or empty if expired or not found
     */
    public Optional<ForgottenPasswordToken> findForgottenPasswordToken(User user) {
        return forgottenPasswordTokenRepository.findByUser(user)
                .flatMap(this::validateTokenOrDeleteFilter);
    }

    /**
     * Creates a new forgotten password token for the specified user if none exists.
     *
     * @param user the user for whom the token is to be created
     * @return an Optional containing the newly created token, or empty if a valid token already exists
     */
    public Optional<ForgottenPasswordToken> createForgottenPasswordToken(User user) {
        // check if there is already a forgotten password token for the given user that is not expired
        if (findForgottenPasswordToken(user).isPresent()) {
            return Optional.empty();
        }

        LocalDateTime expiryDate = LocalDateTime.now().plus(TOKEN_EXPIRATION);
        ForgottenPasswordToken token = new ForgottenPasswordToken(user, expiryDate);
        return Optional.of(forgottenPasswordTokenRepository.save(token));
    }

    /**
     * Validates whether a token has expired. If expired, deletes it from the repository.
     *
     * @param token the forgotten password token to validate.
     * @return an Optional containing the valid token, or empty if expired.
     */
    private Optional<ForgottenPasswordToken> validateTokenOrDeleteFilter(ForgottenPasswordToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            forgottenPasswordTokenRepository.delete(token);
            return Optional.empty();
        }
        return Optional.of(token);
    }
}