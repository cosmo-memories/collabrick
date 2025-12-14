package nz.ac.canterbury.seng302.homehelper.integration.controller.auth;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.user.ForgottenPasswordToken;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.auth.ForgottenPasswordTokenRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.auth.ForgottenPasswordService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.validation.user.UserValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ResetPasswordIntegrationTests {

    private static final String VALID_EMAIL = "test@gmail.com";
    private static final String VALID_PASSWORD = "Abc123!!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ForgottenPasswordService forgottenPasswordService;

    @Autowired
    private ForgottenPasswordTokenRepository forgottenPasswordTokenRepository;

    @MockBean // prevent the email actually sending
    private EmailService emailService;

    /* forgotten-password */
    @Test
    public void testForgottenPassword_WhenRequested_ThenShowsForgottenPasswordView() throws Exception {
        mockMvc.perform(get("/forgotten-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/forgottenPasswordPage"))
                .andExpect(model().attribute("email", ""));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {
            "invalid-email",
            "invalid@email",
            "invalid@email.",
            "@invalid.com",
            "invalid@.com",
            "invalid@email..com"
    })
    public void testForgottenPassword_WhenSubmitInvalidEmail_ThenShowsForgottenPasswordViewWithEmailErrorMessage(String input) throws Exception {
        mockMvc.perform(post("/forgotten-password")
                        .with(csrf())
                        .param("email", input))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/forgottenPasswordPage"))
                .andExpect(model().attribute("emailErrorMessage", UserValidation.EMAIL_INVALID_FORMAT));
    }

    @Test
    public void testForgottenPassword_WhenSubmitValidEmailAssociatedWithUser_ThenShowsForgottenPasswordViewWithEmailSuccessMessage() throws Exception {
        // setup user
        userService.addUser(new User("John", "Smith", VALID_EMAIL, VALID_PASSWORD, VALID_PASSWORD));
        mockMvc.perform(post("/forgotten-password").with(csrf()).param("email", VALID_EMAIL))
                .andExpect(view().name("pages/auth/forgottenPasswordPage"))
                .andExpect(model().attribute("emailSentMessage", "An email was sent to the address if it was recognised"));
    }

    @Test
    public void testForgottenPassword_WhenSubmitValidEmailNotAssociatedWithUser_ThenShowsForgottenPasswordViewWithEmailSuccessMessage() throws Exception {
        // assert a user with this email does not exist
        assertNull(userService.findUserByEmail(VALID_EMAIL));
        mockMvc.perform(post("/forgotten-password").with(csrf()).param("email", VALID_EMAIL))
                .andExpect(view().name("pages/auth/forgottenPasswordPage"))
                .andExpect(model().attribute("emailSentMessage", "An email was sent to the address if it was recognised"));
    }

    /* reset-password */
    @Test
    public void testResetPassword_WhenNoToken_ThenRedirectsToLoginWithErrorMessage() throws Exception {
        mockMvc.perform(get("/reset-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("tokenErrorMessage", "Reset password link is invalid"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "invalid-token",
            "12345",
            "abc-def-ghi",
            "urn:uuid:"
    })
    public void testResetPassword_WhenNonUUIDToken_ThenRedirectsToLoginWithResetPasswordLinkInvalidErrorMessage(String input) throws Exception {
        mockMvc.perform(get("/reset-password")
                        .param("token", input))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("tokenErrorMessage", "Reset password link is invalid"));
    }

    @Test
    public void testResetPassword_WheValidExpiredToken_ThenRedirectsToLoginWithResetPasswordLinkExpiredErrorMessage() throws Exception {
        // setup user and valid reset password token
        User user = userService.addUser(new User("John", "Smith", VALID_EMAIL, VALID_PASSWORD, VALID_PASSWORD));
        ForgottenPasswordToken forgottenPasswordToken = forgottenPasswordService.createForgottenPasswordToken(user).orElseThrow();
        forgottenPasswordToken.setExpiryDate(forgottenPasswordToken.getExpiryDate().minusHours(1));

        mockMvc.perform(get("/reset-password")
                        .param("token", forgottenPasswordToken.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("tokenErrorMessage", "Reset password link has expired"));
    }

    @Test
    public void testResetPassword_WhenValidToken_ThenShowsResetPasswordView() throws Exception {
        // setup user and valid reset password token
        User user = userService.addUser(new User("John", "Smith", VALID_EMAIL, VALID_PASSWORD, VALID_PASSWORD));
        ForgottenPasswordToken forgottenPasswordToken = forgottenPasswordService.createForgottenPasswordToken(user).orElseThrow();
        forgottenPasswordTokenRepository.save(forgottenPasswordToken);

        mockMvc.perform(get("/reset-password")
                        .param("token", forgottenPasswordToken.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/resetPasswordPage"))
                .andExpect(model().attribute("token", forgottenPasswordToken.getId().toString()))
                .andExpect(model().attributeDoesNotExist("newPassword"))
                .andExpect(model().attributeDoesNotExist("confirmedNewPassword"));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {
            "Short1!",
            "lowercase123!",
            "UPPERCASE123!",
            "NoNumberPass!",
            "NoSpecialChar123",
    })
    public void testResetPassword_WhenSubmitValidTokenAndInvalidNewPassword_ThenShowsResetPasswordViewWithNewPasswordError(String input) throws Exception {
        // setup user and valid reset password token
        User user = userService.addUser(new User("John", "Smith", VALID_EMAIL, VALID_PASSWORD, VALID_PASSWORD));
        ForgottenPasswordToken forgottenPasswordToken = forgottenPasswordService.createForgottenPasswordToken(user).orElseThrow();
        forgottenPasswordTokenRepository.save(forgottenPasswordToken);

        mockMvc.perform(post("/reset-password")
                        .with(csrf())
                        .param("token", forgottenPasswordToken.getId().toString())
                        .param("newPassword", input)
                        .param("confirmedNewPassword", input))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/resetPasswordPage"))
                .andExpect(model().attribute("token", forgottenPasswordToken.getId().toString()))
                .andExpect(model().attribute("newPasswordError", UserValidation.PASSWORD_LOW_STRENGTH));
    }

    @Test
    public void testResetPassword_WhenSubmitValidTokenAndMismatchingNewPasswords_ThenShowsResetPasswordViewWithConfirmedNewPasswordError() throws Exception {
        // setup user and valid reset password token
        User user = userService.addUser(new User("John", "Smith", VALID_EMAIL, VALID_PASSWORD, VALID_PASSWORD));
        ForgottenPasswordToken forgottenPasswordToken = forgottenPasswordService.createForgottenPasswordToken(user).orElseThrow();
        forgottenPasswordTokenRepository.save(forgottenPasswordToken);

        mockMvc.perform(post("/reset-password")
                        .with(csrf())
                        .param("token", forgottenPasswordToken.getId().toString())
                        .param("newPassword", "NewPassword123!!")
                        .param("confirmedNewPassword", "NotTheSamePassword123!!"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/resetPasswordPage"))
                .andExpect(model().attribute("token", forgottenPasswordToken.getId().toString()))
                .andExpect(model().attribute("confirmedNewPasswordError", UserValidation.PASSWORD_RETYPE_NO_MATCH));
    }

    @Test
    public void testResetPassword_WhenSubmitValidTokenAndValidNewPasswords_ThenUpdatePasswordAndShowLoginViewWithPasswordResetMessageAndTokenIsExpiredAndUpdatePasswordEmailIsSent() throws Exception {
        // setup user and valid reset password token
        String newPassword = "NewPassword123!!";
        User user = userService.addUser(new User("John", "Smith", VALID_EMAIL, VALID_PASSWORD, VALID_PASSWORD));
        ForgottenPasswordToken forgottenPasswordToken = forgottenPasswordService.createForgottenPasswordToken(user).orElseThrow();
        forgottenPasswordTokenRepository.save(forgottenPasswordToken);

        mockMvc.perform(post("/reset-password")
                        .with(csrf())
                        .param("token", forgottenPasswordToken.getId().toString())
                        .param("newPassword", newPassword)
                        .param("confirmedNewPassword", newPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("successMessage", "Your password has been reset."));

        // check the password was actually updated
        User updatedUser = userService.findUserById(user.getId());
        assertTrue(userService.verifyPassword(newPassword, updatedUser.getPassword()));

        // check the forgotten password token has expired
        assertTrue(forgottenPasswordService.findForgottenPasswordToken(forgottenPasswordToken.getId()).isEmpty());

        verify(emailService, times(1)).sendPasswordUpdatedMail(user);
    }
}
