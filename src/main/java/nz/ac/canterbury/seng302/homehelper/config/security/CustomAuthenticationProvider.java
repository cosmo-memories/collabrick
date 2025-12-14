package nz.ac.canterbury.seng302.homehelper.config.security;

import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UserDetailsInvalidException;
import nz.ac.canterbury.seng302.homehelper.exceptions.user.UserNotActivatedException;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.validation.user.UserValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Custom Authentication Provider class to handle authentication based on our existing User entity. This class verifies
 * user credentials using the UserService and provides authentication tokens for successfully authenticated users.
 * Implemented by following the Spring Security Handout on LEARN
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserService userService;

    /**
     * Constructs a new CustomAuthenticationProvider.
     *
     * @param userService the user service responsible for retrieving user details and verifying credentials.
     */
    @Autowired
    public CustomAuthenticationProvider(@Lazy UserService userService) {
        this.userService = userService;
    }

    /**
     * Authenticates a user by validating the provided email and password against the stored credentials.
     *
     * @param authentication An authentication object containing the user's email and password.
     * @return A UsernamePasswordAuthenticationToken if authentication is successful.
     * @throws UserDetailsInvalidException if the email and password combination is incorrect.
     * @throws UserNotActivatedException   if the user's account is not activated.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws UserDetailsInvalidException {
        String email = authentication.getName();
        User user = userService.findUserByEmail(email);
        if (user == null || !userService.verifyPassword(authentication.getCredentials().toString(), user.getPassword())) {
            UserDetailsInvalidException exception = new UserDetailsInvalidException();
            exception.setCredentialsError(UserValidation.EMAIL_UNKNOWN_OR_PASSWORD_INVALID);
            throw exception;
        }
        if (!user.isActivated()) {
            throw new UserNotActivatedException(UserValidation.USER_NOT_ACTIVATED);
        }
        return new UsernamePasswordAuthenticationToken(user.getId(), null, user.getAuthorities());
    }

    /**
     * Specifies which authentication classes this provider supports.
     *
     * @param authentication The authentication class being checked.
     * @return true if the class is UsernamePasswordAuthenticationToken, otherwise false.
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}