package nz.ac.canterbury.seng302.homehelper.unit.config.security;

import nz.ac.canterbury.seng302.homehelper.config.security.CustomAuthenticationProvider;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UserDetailsInvalidException;
import nz.ac.canterbury.seng302.homehelper.exceptions.user.UserNotActivatedException;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.validation.user.UserValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomAuthenticationProviderTests {

    @Mock
    private User user;

    @Mock
    private Authentication authentication;

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomAuthenticationProvider authenticationProvider;

    @Test
    void authenticate_EmailDoesNotExist_ThrowsUserDetailsInvalidException() {
        String email = "jane.doe@gmail.com";
        when(authentication.getName()).thenReturn(email);
        when(userService.findUserByEmail(email)).thenReturn(null);

        UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class, () ->
                authenticationProvider.authenticate(authentication));
        assertEquals(UserValidation.EMAIL_UNKNOWN_OR_PASSWORD_INVALID, exception.getCredentialsError());
    }

    @Test
    void authenticate_IncorrectPassword_ThrowsUserDetailsInvalidException() {
        String email = "jane.doe@gmail.com";
        String wrongPassword = "wrongpassword";

        when(authentication.getName()).thenReturn(email);
        when(authentication.getCredentials()).thenReturn(wrongPassword);
        when(userService.findUserByEmail(email)).thenReturn(user);
        when(user.getPassword()).thenReturn("correctpassword");
        when(userService.verifyPassword(wrongPassword, "correctpassword")).thenReturn(false);

        UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class, () ->
                authenticationProvider.authenticate(authentication));
        assertEquals(UserValidation.EMAIL_UNKNOWN_OR_PASSWORD_INVALID, exception.getCredentialsError());
    }

    @Test
    void authenticate_UserNotActivated_ThrowsUserNotActivatedException() {
        String email = "jane.doe@gmail.com";
        String password = "correctpassword";

        when(authentication.getName()).thenReturn(email);
        when(authentication.getCredentials()).thenReturn(password);
        when(userService.findUserByEmail(email)).thenReturn(user);
        when(user.getPassword()).thenReturn(password);
        when(userService.verifyPassword(password, password)).thenReturn(true);
        when(user.isActivated()).thenReturn(false);

        UserNotActivatedException exception = assertThrows(UserNotActivatedException.class, () ->
                authenticationProvider.authenticate(authentication));
        assertEquals(UserValidation.USER_NOT_ACTIVATED, exception.getMessage());
    }

    @Test
    void authenticate_EmailAndPasswordValid_AuthenticationIsSuccessful() {
        String email = "jane.doe@gmail.com";
        String password = "correctpassword";

        when(authentication.getName()).thenReturn(email);
        when(authentication.getCredentials()).thenReturn(password);
        when(userService.findUserByEmail(email)).thenReturn(user);
        when(user.getPassword()).thenReturn(password);
        when(userService.verifyPassword(password, password)).thenReturn(true);
        when(user.isActivated()).thenReturn(true);
        when(user.getId()).thenReturn(123L);
        when(user.getAuthorities()).thenReturn(Collections.emptyList());

        Authentication result = authenticationProvider.authenticate(authentication);

        assertNotNull(result);
        assertEquals(123L, result.getPrincipal());
        assertNull(result.getCredentials());
        assertEquals(Collections.emptyList(), result.getAuthorities());
    }
}
