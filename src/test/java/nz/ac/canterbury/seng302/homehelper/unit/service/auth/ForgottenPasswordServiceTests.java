package nz.ac.canterbury.seng302.homehelper.unit.service.auth;

import nz.ac.canterbury.seng302.homehelper.entity.user.ForgottenPasswordToken;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.token.TokenExpiredException;
import nz.ac.canterbury.seng302.homehelper.exceptions.token.TokenInvalidException;
import nz.ac.canterbury.seng302.homehelper.repository.auth.ForgottenPasswordTokenRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.auth.ForgottenPasswordService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.validation.user.UserValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgottenPasswordServiceTests {

    private final String tokenString = "f1942f97-dc04-45e3-9c2f-2bb452bd7021";
    private final UUID token = UUID.fromString(tokenString);
    @Mock
    private EmailService emailService;
    @Mock
    private UserService userService;
    @Mock
    private ForgottenPasswordTokenRepository forgottenPasswordTokenRepository;
    @InjectMocks
    private ForgottenPasswordService forgottenPasswordService;
    private User testUser;
    private ForgottenPasswordToken validToken;
    private ForgottenPasswordToken expiredToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");

        validToken = new ForgottenPasswordToken(testUser, LocalDateTime.now().plusMinutes(10));
        expiredToken = new ForgottenPasswordToken(testUser, LocalDateTime.now().minusMinutes(10));
        validToken.setId(token);
        expiredToken.setId(token);
    }

    // createForgottenPasswordToken(User)
    @Test
    void testCreateForgottenPasswordToken_WhenNoExistingToken_ThenTokenIsCreatedAndSaved() {
        when(forgottenPasswordTokenRepository.findByUser(testUser))
                .thenReturn(Optional.empty());
        when(forgottenPasswordTokenRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<ForgottenPasswordToken> optionalForgottenPasswordToken = forgottenPasswordService.createForgottenPasswordToken(testUser);
        assertTrue(optionalForgottenPasswordToken.isPresent());
        verify(forgottenPasswordTokenRepository, times(1)).save(optionalForgottenPasswordToken.get());
    }

    @Test
    void testCreateForgottenPasswordToken_WhenExistingValidToken_ThenNoNewTokenCreated() {
        when(forgottenPasswordTokenRepository.findByUser(testUser))
                .thenReturn(Optional.of(validToken));

        Optional<ForgottenPasswordToken> optionalForgottenPasswordToken = forgottenPasswordService.createForgottenPasswordToken(testUser);
        assertTrue(optionalForgottenPasswordToken.isEmpty());
        verify(forgottenPasswordTokenRepository, times(0)).save(any());
    }

    @Test
    void testCreateForgottenPasswordToken_WhenExistingExpiredToken_ThenDeleteExpiredTokenAndNewTokenCreatedAndSaved() {
        when(forgottenPasswordTokenRepository.findByUser(testUser))
                .thenReturn(Optional.of(expiredToken));
        when(forgottenPasswordTokenRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<ForgottenPasswordToken> optionalForgottenPasswordToken = forgottenPasswordService.createForgottenPasswordToken(testUser);
        assertTrue(optionalForgottenPasswordToken.isPresent());
        verify(forgottenPasswordTokenRepository, times(1)).delete(expiredToken);
        verify(forgottenPasswordTokenRepository, times(1)).save(optionalForgottenPasswordToken.get());
    }

    // findForgottenPasswordToken(UUID)
    @Test
    void testFindForgottenPasswordTokenByUUID_WhenNoTokenExists_ThenReturnEmptyOptional() {
        when(forgottenPasswordTokenRepository.findById(token))
                .thenReturn(Optional.empty());

        assertEquals(Optional.empty(), forgottenPasswordService.findForgottenPasswordToken(token));
    }

    @Test
    void testFindForgottenPasswordTokenByUUID_WhenTokenExpired_ThenRemoveAndReturnEmptyOptional() {
        when(forgottenPasswordTokenRepository.findById(token))
                .thenReturn(Optional.of(expiredToken));

        assertEquals(Optional.empty(), forgottenPasswordService.findForgottenPasswordToken(token));
    }

    @Test
    void testFindForgottenPasswordTokenByUUID_WhenValidTokenExists_ThenReturnOptionalWithToken() {
        when(forgottenPasswordTokenRepository.findById(token))
                .thenReturn(Optional.of(validToken));

        assertEquals(Optional.of(validToken), forgottenPasswordService.findForgottenPasswordToken(token));
    }

    // findForgottenPasswordToken(User)
    @Test
    void testFindForgottenPasswordTokenByUser_WhenNoTokenExists_ThenReturnEmptyOptional() {
        when(forgottenPasswordTokenRepository.findByUser(testUser))
                .thenReturn(Optional.empty());

        assertEquals(Optional.empty(), forgottenPasswordService.findForgottenPasswordToken(testUser));
    }

    @Test
    void testFindForgottenPasswordTokenByUser_WhenTokenExpired_ThenRemoveAndReturnEmptyOptional() {
        when(forgottenPasswordTokenRepository.findByUser(testUser))
                .thenReturn(Optional.of(expiredToken));

        assertEquals(Optional.empty(), forgottenPasswordService.findForgottenPasswordToken(testUser));
        verify(forgottenPasswordTokenRepository, times(1)).delete(expiredToken);
    }

    @Test
    void testFindForgottenPasswordTokenByUser_WhenValidTokenExists_ThenReturnOptionalWithToken() {
        when(forgottenPasswordTokenRepository.findByUser(testUser))
                .thenReturn(Optional.of(validToken));

        assertEquals(Optional.of(validToken), forgottenPasswordService.findForgottenPasswordToken(testUser));
        verify(forgottenPasswordTokenRepository, times(0)).delete(expiredToken);
    }

    // findForgottenPasswordToken(String)
    @Test
    void testFindForgottenPasswordToken_WhenTokenIsNull_ThenThrowTokenInvalidException() {
        String token = null;
        assertThrows(TokenInvalidException.class, () -> forgottenPasswordService.findForgottenPasswordToken(token));
    }

    @Test
    void testFindForgottenPasswordToken_WhenTokenIsNonUUID_ThenThrowTokenInvalidException() {
        String token = "ABC123";
        assertThrows(TokenInvalidException.class, () -> forgottenPasswordService.findForgottenPasswordToken(token));
    }

    @Test
    void testFindForgottenPasswordToken_WhenTokenExpired_ThenThrowTokenExpiredException() {
        when(forgottenPasswordTokenRepository.findById(token))
                .thenReturn(Optional.of(expiredToken));

        assertThrows(TokenExpiredException.class, () -> forgottenPasswordService.findForgottenPasswordToken(tokenString));
    }

    @Test
    void testFindForgottenPasswordToken_WhenValidTokenExists_ThenReturnToken() {
        when(forgottenPasswordTokenRepository.findById(token))
                .thenReturn(Optional.of(validToken));

        assertEquals(validToken, forgottenPasswordService.findForgottenPasswordToken(tokenString));
    }

    @Test
    void testSendResetPasswordEmail_WhenEmailIsInValid_ThenThrowIllegalArgumentException() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("Invalid Email");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> forgottenPasswordService.sendResetPasswordEmail("test@test"));
            assertEquals("Invalid Email", exception.getMessage());
        }
    }

    @Test
    void testSendResetPasswordEmail_WhenEmailIsNotAssociatedWithUser_ThenForgottenPasswordTokenIsNotCreatedAndEmailIsNotSent() {
        String email = "test@test.test";
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("");
            when(userService.findUserByEmail(email)).thenReturn(null);

            forgottenPasswordService.sendResetPasswordEmail(email);

            verify(forgottenPasswordTokenRepository, never()).save(any());
            verify(emailService, never()).sendHtmlTemplateEmail(any(), any(), any(), anyMap());
        }
    }

    @Test
    void testSendResetPasswordEmail_WhenEmailIsValid_ThenForgottenPasswordTokenIsCreatedAndEmailIsSent() {
        try (MockedStatic<UserValidation> mockedUserValidation = mockStatic(UserValidation.class)) {
            mockedUserValidation.when(() -> UserValidation.validateEmailFormat(any()))
                    .thenReturn("");
            when(userService.findUserByEmail(testUser.getEmail())).thenReturn(testUser);
            when(forgottenPasswordTokenRepository.findByUser(any())).thenReturn(Optional.empty());
            when(forgottenPasswordTokenRepository.save(any())).thenReturn(validToken);

            forgottenPasswordService.sendResetPasswordEmail(testUser.getEmail());

            verify(emailService).sendResetPasswordMail(eq(testUser), any());
        }
    }
}
