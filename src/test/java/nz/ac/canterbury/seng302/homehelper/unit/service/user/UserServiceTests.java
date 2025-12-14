package nz.ac.canterbury.seng302.homehelper.unit.service.user;

import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UserDetailsInvalidException;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UserUpdatePasswordException;
import nz.ac.canterbury.seng302.homehelper.model.auth.UserPasswordUpdate;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.validation.user.UserValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UserServiceTests {

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private UserService userService;
    private EmailService emailService;
    private LocationService locationService;
    private User user;

    @BeforeEach
    public void setUp() {
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        userRepository = Mockito.mock(UserRepository.class);
        emailService = Mockito.mock(EmailService.class);
        locationService = Mockito.mock(LocationService.class);
        userService = new UserService(userRepository, passwordEncoder, emailService, locationService);
        user = new User("John", "Smith", "john@smith.nz", "password", "password");
    }

    /* Invalid update password requests */
    @Test
    void testUpdatePassword_EmptyOldPassword_UserUpdatePasswordExceptionThrown() {
        UserPasswordUpdate passwordUpdate = new UserPasswordUpdate("", "new password", "retyped new password");
        UserUpdatePasswordException exception = assertThrows(UserUpdatePasswordException.class, () -> userService.updateUserPassword(user, passwordUpdate));
        assertEquals(UserValidation.OLD_PASSWORD_INCORRECT, exception.getOldPasswordError());
    }

    @Test
    void testUpdatePassword_EmptyNewPassword_UserUpdatePasswordExceptionThrownWithNewPasswordError() {
        UserPasswordUpdate passwordUpdate = new UserPasswordUpdate("password", "", "");
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            // mock the password encoder verifying the password matching
            when(passwordEncoder.matches(eq(user.getPassword()), eq(passwordUpdate.getOldPassword())))
                    .thenReturn(true);
            // mock the user validation returning a low strength password error
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn(UserValidation.PASSWORD_LOW_STRENGTH);

            // mock the user validation returning a matched password case
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(any(), any()))
                    .thenReturn("");

            UserUpdatePasswordException exception = assertThrows(UserUpdatePasswordException.class,
                    () -> userService.updateUserPassword(user, passwordUpdate));
            assertEquals(UserValidation.PASSWORD_LOW_STRENGTH, exception.getNewPasswordError());
        }
    }

    @Test
    void testUpdatePassword_EmptyNewRetypedPassword_UserUpdatePasswordExceptionThrownWithRetypedNewPasswordError() {
        UserPasswordUpdate passwordUpdate = new UserPasswordUpdate("password", "Abc123!!", "");
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            // mock the password encoder verifying the password matching
            when(passwordEncoder.matches(eq(user.getPassword()), eq(passwordUpdate.getOldPassword())))
                    .thenReturn(true);
            // mock the user validation not returning a low strength password error
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn("");
            // mock the user validation returning a new password mismatch error
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(eq(passwordUpdate.getNewPassword()), eq(passwordUpdate.getRetypedNewPassword())))
                    .thenReturn(UserValidation.PASSWORD_RETYPE_NO_MATCH);

            UserUpdatePasswordException exception = assertThrows(UserUpdatePasswordException.class,
                    () -> userService.updateUserPassword(user, passwordUpdate));
            assertEquals(UserValidation.PASSWORD_RETYPE_NO_MATCH, exception.getRetypedNewPasswordError());
        }
    }

    @Test
    void testUpdatePassword_OldPasswordIncorrect_UserUpdatePasswordExceptionThrownWithOldPasswordError() {
        UserPasswordUpdate passwordUpdate = new UserPasswordUpdate("wrongPassword", "Abc123!!", "Abc123!!");
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            // mock the password encoder not verifying the password matching
            when(passwordEncoder.matches(eq(user.getPassword()), eq(passwordUpdate.getOldPassword())))
                    .thenReturn(false);

            UserUpdatePasswordException exception = assertThrows(UserUpdatePasswordException.class,
                    () -> userService.updateUserPassword(user, passwordUpdate));
            assertEquals(UserValidation.OLD_PASSWORD_INCORRECT, exception.getOldPasswordError());
        }
    }

    @Test
    void testUpdatePassword_LowStrengthNewPassword_UserUpdatePasswordExceptionThrownWithNewPasswordError() {
        UserPasswordUpdate passwordUpdate = new UserPasswordUpdate("password", "password", "password");
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            // mock the password encoder verifying the password matching
            when(passwordEncoder.matches(eq(user.getPassword()), eq(passwordUpdate.getOldPassword())))
                    .thenReturn(true);
            // mock the user validation returning a low strength password error
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn(UserValidation.PASSWORD_LOW_STRENGTH);

            // mock the user validation returning a matched password case
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(any(), any()))
                    .thenReturn("");

            UserUpdatePasswordException exception = assertThrows(UserUpdatePasswordException.class,
                    () -> userService.updateUserPassword(user, passwordUpdate));
            assertEquals(UserValidation.PASSWORD_LOW_STRENGTH, exception.getNewPasswordError());
        }
    }

    @Test
    void testUpdatePassword_LowStrengthNewPassword_AndPasswordsDontMatch_UserUpdatePasswordExceptionThrownWithNewPasswordError_AndWithRetypedNewPasswordError() {
        UserPasswordUpdate passwordUpdate = new UserPasswordUpdate("password", "password", "p");
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            // mock the password encoder verifying the password matching
            when(passwordEncoder.matches(eq(user.getPassword()), eq(passwordUpdate.getOldPassword())))
                    .thenReturn(true);
            // mock the user validation returning a low strength password error
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn(UserValidation.PASSWORD_LOW_STRENGTH);

            // mock the user validation returning a matched password case
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(any(), any()))
                    .thenReturn(UserValidation.PASSWORD_RETYPE_NO_MATCH);

            UserUpdatePasswordException exception = assertThrows(UserUpdatePasswordException.class,
                    () -> userService.updateUserPassword(user, passwordUpdate));
            assertEquals(UserValidation.PASSWORD_LOW_STRENGTH, exception.getNewPasswordError());
            assertEquals(UserValidation.PASSWORD_RETYPE_NO_MATCH, exception.getRetypedNewPasswordError());
        }
    }

    @Test
    void testUpdatePassword_MismatchNewRetypedPasswords_UserUpdatePasswordExceptionThrownWithRetypedNewPasswordError() {
        UserPasswordUpdate passwordUpdate = new UserPasswordUpdate("password", "Abc123!!", "Cba123!!");
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            // mock the password encoder verifying the password matching
            when(passwordEncoder.matches(eq(user.getPassword()), eq(passwordUpdate.getOldPassword())))
                    .thenReturn(true);
            // mock the user validation not returning a low strength password error
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn("");
            // mock the user validation returning a new password mismatch error
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(eq(passwordUpdate.getNewPassword()), eq(passwordUpdate.getRetypedNewPassword())))
                    .thenReturn(UserValidation.PASSWORD_RETYPE_NO_MATCH);

            UserUpdatePasswordException exception = assertThrows(UserUpdatePasswordException.class,
                    () -> userService.updateUserPassword(user, passwordUpdate));
            assertEquals(UserValidation.PASSWORD_RETYPE_NO_MATCH, exception.getRetypedNewPasswordError());
        }
    }


    /* Valid update password requests */
    @Test
    void testUpdatePassword_successfulUpdate1() {
        UserPasswordUpdate passwordUpdate = new UserPasswordUpdate("password", "NewPass123!!", "NewPass123!!");
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            // mock the password encoder verifying the password matching
            when(passwordEncoder.matches(eq(user.getPassword()), eq(passwordUpdate.getOldPassword())))
                    .thenReturn(true);
            // mock the password util hashing the password
            when(passwordEncoder.encode(eq(passwordUpdate.getNewPassword()))).thenReturn("hashedNewPass123!!!");
            // mock the user validation not returning a low strength password error
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(eq(passwordUpdate.getNewPassword())))
                    .thenReturn("");
            // mock the user validation not returning a new password mismatch error
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(eq(passwordUpdate.getNewPassword()), eq(passwordUpdate.getRetypedNewPassword())))
                    .thenReturn("");

            userService.updateUserPassword(user, passwordUpdate);

            // Ensure password was updated
            assertEquals("hashedNewPass123!!!", user.getPassword());

            // Verify that the user is saved
            verify(userRepository, times(1)).save(user);
        }
    }

    /* Test addUser */
    @Test
    void addUser_EmptyFirstName_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("First")))
                    .thenReturn("First" + UserValidation.NAME_EMPTY);
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("Last")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(any(), any()))
                    .thenReturn("");
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class, () -> userService.addUser(user));
            assertEquals("First" + UserValidation.NAME_EMPTY, exception.getFirstNameError());
        }
    }

    @Test
    void addUser_FirstNameSpecialCharacters_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("First")))
                    .thenReturn("First" + UserValidation.NAME_INVALID_CHARACTERS);
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("Last")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(any(), any()))
                    .thenReturn("");
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class, () -> userService.addUser(user));
            assertEquals("First" + UserValidation.NAME_INVALID_CHARACTERS, exception.getFirstNameError());
        }
    }

    @Test
    void addUser_LastNameSpecialCharacters_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("First")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("Last")))
                    .thenReturn("Last" + UserValidation.NAME_INVALID_CHARACTERS);
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(any(), any()))
                    .thenReturn("");
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class, () -> userService.addUser(user));
            assertEquals("Last" + UserValidation.NAME_INVALID_CHARACTERS, exception.getLastNameError());
        }
    }

    @Test
    void addUser_EmailFormatInvalid_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("First")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("Last")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn(UserValidation.EMAIL_INVALID_FORMAT);
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(any(), any()))
                    .thenReturn("");
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class, () -> userService.addUser(user));
            assertEquals(UserValidation.EMAIL_INVALID_FORMAT, exception.getEmailError());
        }
    }

    @Test
    void addUser_EmailInUse_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("First")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("Last")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), any()))
                    .thenReturn(UserValidation.EMAIL_IN_USE);
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(any(), any()))
                    .thenReturn("");
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class, () -> userService.addUser(user));
            assertEquals(UserValidation.EMAIL_IN_USE, exception.getEmailError());
        }
    }

    @Test
    void addUser_PasswordWeak_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("First")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("Last")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn(UserValidation.PASSWORD_LOW_STRENGTH);
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(any(), any()))
                    .thenReturn("");
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class, () -> userService.addUser(user));
            assertEquals(UserValidation.PASSWORD_LOW_STRENGTH, exception.getPasswordError());
        }
    }

    @Test
    void addUser_PasswordRetypeNoMatch_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("First")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("Last")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(any(), any()))
                    .thenReturn(UserValidation.PASSWORD_RETYPE_NO_MATCH);
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class, () -> userService.addUser(user));
            assertEquals(UserValidation.PASSWORD_RETYPE_NO_MATCH, exception.getPasswordError());
        }
    }

    @Test
    void addUser_PasswordRetypeNoMatchAndPasswordWeak_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("First")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("Last")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn(UserValidation.PASSWORD_LOW_STRENGTH);
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(any(), any()))
                    .thenReturn(UserValidation.PASSWORD_RETYPE_NO_MATCH);
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class, () -> userService.addUser(user));
            assertEquals(UserValidation.PASSWORD_LOW_STRENGTH, exception.getPasswordError());
        }
    }

    @Test
    void addUser_MultipleErrors_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("First")))
                    .thenReturn("First" + UserValidation.NAME_EMPTY);
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("Last")))
                    .thenReturn("Last" + UserValidation.NAME_TOO_LONG);
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), any()))
                    .thenReturn(UserValidation.EMAIL_IN_USE);
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(any(), any()))
                    .thenReturn(UserValidation.PASSWORD_RETYPE_NO_MATCH);
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class, () -> userService.addUser(user));
            assertEquals("First" + UserValidation.NAME_EMPTY, exception.getFirstNameError());
            assertEquals("Last" + UserValidation.NAME_TOO_LONG, exception.getLastNameError());
            assertEquals(UserValidation.EMAIL_IN_USE, exception.getEmailError());
            assertEquals(UserValidation.PASSWORD_RETYPE_NO_MATCH, exception.getPasswordError());
        }
    }


    @Test
    void addUser_SuccessfulRegister() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("First")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("Last")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordStrength(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validatePasswordMatch(any(), any()))
                    .thenReturn("");
            when(passwordEncoder.encode(eq(user.getPassword()))).thenReturn("hashedNewPass123!!!");
            userService.addUser(user);
            assertEquals("hashedNewPass123!!!", user.getPassword());

            verify(userRepository, times(1)).save(user);
        }
    }

    /*Test validateUserForSignIn */

    @Test
    void validateUserForSignIn_EmailFormatIncorrect_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn(UserValidation.EMAIL_INVALID_FORMAT);
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class, () -> userService.validateUserForSignIn(user));
            assertEquals(UserValidation.EMAIL_INVALID_FORMAT, exception.getEmailError());
        }
    }

    @Test
    void validateUserForSignIn_LoginValid() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("");
            when(userRepository.findByEmail(any())).thenReturn(List.of(user));
            when(userService.verifyPassword(any(), any())).thenReturn(true);
            assertDoesNotThrow(() -> userService.validateUserForSignIn(user));
        }
    }

    /* Tests for updateUser */
    @Test
    void updateUser_EmptyFirstName_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(eq(""), eq("First")))
                    .thenReturn("First" + UserValidation.NAME_EMPTY);
            mockedUserValidation.when(() -> UserValidation.validateName(eq(""), eq("Last")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(eq("")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), eq("")))
                    .thenReturn("");
            String newFirstName = "";
            String newLastName = "";
            String newEmail = "";
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class,
                    () -> userService.updateUser(user, newFirstName, newLastName, newEmail));
            assertEquals("First" + UserValidation.NAME_EMPTY, exception.getFirstNameError());
        }
    }


    @Test
    void updateUser_FirstNameSpecialCharacters_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(eq("Jan$$"), eq("First")))
                    .thenReturn("First" + UserValidation.NAME_INVALID_CHARACTERS);
            mockedUserValidation.when(() -> UserValidation.validateName(eq(""), eq("Last")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(eq("")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), eq("")))
                    .thenReturn("");
            String newFirstName = "Jan$$";
            String newLastName = "";
            String newEmail = "";
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class,
                    () -> userService.updateUser(user, newFirstName, newLastName, newEmail));
            assertEquals("First" + UserValidation.NAME_INVALID_CHARACTERS, exception.getFirstNameError());
        }
    }


    @Test
    void updateUser_LastNameSpecialCharacters_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(eq("Jan"), eq("First")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateName(eq("Sm#th"), eq("Last")))
                    .thenReturn("Last" + UserValidation.NAME_INVALID_CHARACTERS);
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(eq("")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), eq("")))
                    .thenReturn("");
            String newFirstName = "Jan";
            String newLastName = "Sm#th";
            String newEmail = "";
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class,
                    () -> userService.updateUser(user, newFirstName, newLastName, newEmail));
            assertEquals("Last" + UserValidation.NAME_INVALID_CHARACTERS, exception.getLastNameError());
        }
    }


    @Test
    void updateUser_EmailFormatInvalid_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(eq("Jan"), eq("First")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateName(eq("Smith"), eq("Last")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(eq("email@email")))
                    .thenReturn(UserValidation.EMAIL_INVALID_FORMAT);
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), any()))
                    .thenReturn("");
            String newFirstName = "Jan";
            String newLastName = "Smith";
            String newEmail = "email@email";
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class,
                    () -> userService.updateUser(user, newFirstName, newLastName, newEmail));
            assertEquals(UserValidation.EMAIL_INVALID_FORMAT, exception.getEmailError());
        }
    }


    @Test
    void updateUser_EmailInUse_ThrowsUserDetailsInvalidException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("First")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("Last")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), any()))
                    .thenReturn(UserValidation.EMAIL_IN_USE);
            String newFirstName = "Jan";
            String newLastName = "Smith";
            String newEmail = "email@email.com";
            UserDetailsInvalidException exception = assertThrows(UserDetailsInvalidException.class, () -> userService.updateUser(user, newFirstName, newLastName, newEmail));
            assertEquals(UserValidation.EMAIL_IN_USE, exception.getEmailError());
        }
    }

    @Test
    void updateUser_SuccessfulUpdate() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("First")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateName(any(), eq("Last")))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("");
            mockedUserValidation.when(() -> UserValidation.checkRegisterEmail(any(), any()))
                    .thenReturn("");
            String newFirstName = "Jan";
            String newLastName = "Doe";
            String newEmail = "email@email.com";

            userService.updateUser(user, newFirstName, newLastName, newEmail);

            assertEquals(newFirstName, user.getFname());
            assertEquals(newLastName, user.getLname());
            assertEquals(newEmail, user.getEmail());

            verify(userRepository, times(1)).save(user);
        }
    }

    /* Test findUserByEmail */
    @Test
    void findUserByEmail_UserDoesNotExist_ReturnsNull() {
        when(userRepository.findByEmail(any())).thenReturn(new ArrayList<>());
        assertNull(userService.findUserByEmail("email@email.com"));
    }

    @Test
    void findUserByEmail_UserExists_ReturnsUser() {
        when(userRepository.findByEmail(any())).thenReturn(List.of(user));
        assertEquals(user.getId(), userService.findUserByEmail("email@email.com").getId());
    }

    /* Test findUserById */
    @Test
    void findUserById_UserDoesNotExist_ReturnsNull() {
        when(userRepository.findUserById(anyLong())).thenReturn(new ArrayList<>());
        assertNull(userService.findUserById(1));
    }

    @Test
    void findUserById_UserExists_ReturnsUser() {
        when(userRepository.findUserById(anyLong())).thenReturn(List.of(user));
        assertEquals(user.getId(), userService.findUserById(1).getId());
    }
}
