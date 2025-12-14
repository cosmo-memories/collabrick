package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.validation.user.UserValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

/**
 * Unit tests for the UserValidation class
 */
public class UserValidationTests {
    static Stream<String> validNames() {
        return Stream.of(
                "Dominic",
                "José",
                "O'Connor",
                "Nguyễn Thị",
                "Mary-Sophia",
                "Mary Sophia",
                "Mary-",
                "Dominic'",
                "'Mary",
                "-Dominic",
                "Mary--Sophia",
                "O''Malley",
                "Léa-Marie"
        );
    }

    static Stream<String> invalidNames() {
        return Stream.of(
                "%Hugo**",
                "Mary_Sophia",
                "Hugo15",
                "$",
                "-",
                "'",
                "--",
                "''",
                "-'-",
                "😊",
                "Anna😊"
        );
    }

    static Stream<String> emptyNames() {
        return Stream.of(
                "",
                " ",
                "       ",
                "\t",
                "\n"
        );
    }

    @ParameterizedTest
    @MethodSource("validNames")
    public void validateName_ValidFirstName_ReturnsEmptyString(String firstName) {
        Assertions.assertEquals("", UserValidation.validateName(firstName, "First"));
    }

    @ParameterizedTest
    @MethodSource("emptyNames")
    void validateName_EmptyFirstName_ReturnsErrorMessage(String firstName) {
        Assertions.assertEquals("First" + UserValidation.NAME_EMPTY, UserValidation.validateName(firstName, "First"));
    }


    @ParameterizedTest
    @MethodSource("invalidNames")
    void validateName_InvalidFirstName_ReturnsErrorMessage(String firstName) {
        Assertions.assertEquals("First" + UserValidation.NAME_INVALID_CHARACTERS, UserValidation.validateName(firstName, "First"));
    }

    @Test
    void validateName_FirstNameOver64Characters_ReturnsErrorMessage() {
        String firstName = "Jean-Baptiste-Christophe-Alexandre-Francois-Nicolas-Sebastien-Pierre-Guillaume";
        Assertions.assertEquals("First" + UserValidation.NAME_TOO_LONG, UserValidation.validateName(firstName, "First"));

    }

    /* Tests for validateName, specifically for last names  */
    @ParameterizedTest
    @MethodSource("validNames")
    public void validateName_ValidLastName_ReturnsEmptyString(String lastName) {
        Assertions.assertEquals("", UserValidation.validateName(lastName, "Last"));
    }

    @ParameterizedTest
    @MethodSource("emptyNames")
    void validateName_EmptyLastName_ReturnsEmptyString(String lastName) {
        Assertions.assertEquals("", UserValidation.validateName(lastName, "Last"));
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    void validateName_InvalidLastNames_ReturnsErrorMessage(String lastName) {
        Assertions.assertEquals("Last" + UserValidation.NAME_INVALID_CHARACTERS, UserValidation.validateName(lastName, "Last"));
    }

    @Test
    void validateName_LastNameOver64Characters_ReturnsErrorMessage() {
        String lastName = "Hubertson-Vanderberg-Steinmeier-Fitzgerald-McLaughlington-Devereaux-Sinclair";
        Assertions.assertEquals("Last" + UserValidation.NAME_TOO_LONG, UserValidation.validateName(lastName, "Last"));
    }

    /* Tests for validateEmailFormat */
    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.EmailTestData#invalidEmails")
    void validateEmailFormat_invalidEmail_ReturnErrorMessage(String email) {
        Assertions.assertEquals(UserValidation.EMAIL_INVALID_FORMAT, UserValidation.validateEmailFormat(email));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.EmailTestData#validEmails")
    void validateEmailFormat_validEmail_ReturnsEmptyString(String email) {
        Assertions.assertEquals("", UserValidation.validateEmailFormat(email));
    }

    /* Tests for checkLoginEmail */
    @Test
    void checkLoginEmail_EmailExists_ReturnsEmptyString() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(List.of(new User("Jane", "Doe", "email@email.com")));
        Assertions.assertEquals("", UserValidation.checkLoginEmail(userRepository, "email@email.com"));
    }

    @Test
    void checkLoginEmail_EmailDoesNotExist_ReturnsErrorMessage() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(new ArrayList<>());
        Assertions.assertEquals(UserValidation.EMAIL_UNKNOWN_OR_PASSWORD_INVALID, UserValidation.checkLoginEmail(userRepository, "email@email.com"));
    }

    /* Test checkRegisterEmail */

    @Test
    void checkRegisterEmail_EmailAlreadyInUse_ReturnErrorMessage() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(List.of(new User("Jane", "Doe", "email@email.com")));
        Assertions.assertEquals(UserValidation.EMAIL_IN_USE, UserValidation.checkRegisterEmail(userRepository, "email@email.com"));
    }

    @Test
    void checkRegisterEmail_EmailAvailable_ReturnsEmptyString() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(new ArrayList<>());
        Assertions.assertEquals("", UserValidation.checkRegisterEmail(userRepository, "email@email.com"));
    }

    /* Tests for validatePasswordStrength */
    @Test
    void validatePasswordStrength_PasswordStrong_ReturnsEmptyString() {
        String password = "Abc123!!";
        Assertions.assertEquals("", UserValidation.validatePasswordStrength(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Ab13!",
            "abc123!!",
            "ABC123!!",
            "Abcd1234",
            "Abc@#$%^",

    })
    void validatePasswordStrength_PasswordWeak_ReturnsErrorMessage(String password) {
        Assertions.assertEquals(UserValidation.PASSWORD_LOW_STRENGTH, UserValidation.validatePasswordStrength(password));
    }


    /* Test for validatePasswordMatch */
    @Test
    void validatePasswordMatch_PasswordAndRetypedPasswordMatch_ReturnsEmptyString() {
        String password = "Abc123!!";
        String retypedPassword = "Abc123!!";
        Assertions.assertEquals("", UserValidation.validatePasswordMatch(password, retypedPassword));
    }

    @Test
    void validatePasswordMatch_PasswordAndRetypedPasswordDontMatch_ReturnsEmptyString() {
        String password = "Abc123!!";
        String retypedPassword = "abc123!!";
        Assertions.assertEquals(UserValidation.PASSWORD_RETYPE_NO_MATCH, UserValidation.validatePasswordMatch(password, retypedPassword));
    }

}