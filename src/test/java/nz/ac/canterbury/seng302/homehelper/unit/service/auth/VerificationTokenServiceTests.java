package nz.ac.canterbury.seng302.homehelper.unit.service.auth;

import jakarta.mail.MessagingException;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.entity.user.VerificationToken;
import nz.ac.canterbury.seng302.homehelper.repository.auth.VerificationTokenRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.InvitationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.service.auth.VerificationTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VerificationTokenServiceTests {
    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Mock
    private InvitationService invitationService;

    @InjectMocks
    @Spy
    private VerificationTokenService verificationTokenService;

    /* generateCode() */
    @Test
    void testGenerateCode_ShouldGenerateSixDigitCode() {
        String code = verificationTokenService.generateCode();
        assertEquals(6, code.length());
    }

    @Test
    void testGenerateCode_shouldContainOnlyDigits() {
        String code = verificationTokenService.generateCode();
        assertTrue(code.matches("\\d{6}"));
    }

    /* generateVerificationEmailCode() */
    @Test
    void testGenerateVerificationEmailCode_TokenIsNotUsed_ShouldGenerateUnusedSixDigitCode() {
        // simulate the token being unused
        when(verificationTokenRepository.findByToken(any())).thenReturn(Optional.empty());
        String code = verificationTokenService.generateVerificationEmailCode();
        assertTrue(code.matches("\\d{6}"));
    }

    @Test
    void testGenerateVerificationEmailCode_FirstTokenIsUsed_ShouldGenerateUnusedSixDigitCode() {
        // simulate the token 123456 being used
        String usedCode = "123456";
        String unusedCode = "654321";
        VerificationToken mockVerificationToken = mock(VerificationToken.class);
        when(verificationTokenRepository.findByToken(usedCode)).thenReturn(Optional.of(mockVerificationToken));
        when(verificationTokenRepository.findByToken(unusedCode)).thenReturn(Optional.empty());
        when(verificationTokenService.generateCode()).thenReturn(usedCode, unusedCode);

        // test that the unused code 654321 is chosen
        String code = verificationTokenService.generateVerificationEmailCode();
        assertEquals(unusedCode, code);
    }

    @Test
    void testGenerateVerificationEmailCode_MultipleCollisions_ShouldGenerateUnusedCode() {
        // simulate three used codes and then one unused
        String usedCode1 = "111111";
        String usedCode2 = "222222";
        String usedCode3 = "333333";
        String unusedCode = "444444";
        VerificationToken mockToken = mock(VerificationToken.class);
        when(verificationTokenRepository.findByToken(usedCode1)).thenReturn(Optional.of(mockToken));
        when(verificationTokenRepository.findByToken(usedCode2)).thenReturn(Optional.of(mockToken));
        when(verificationTokenRepository.findByToken(usedCode3)).thenReturn(Optional.of(mockToken));
        when(verificationTokenRepository.findByToken(unusedCode)).thenReturn(Optional.empty());
        when(verificationTokenService.generateCode()).thenReturn(usedCode1, usedCode2, usedCode3, unusedCode);

        // test that the unused code 444444 is chosen
        String result = verificationTokenService.generateVerificationEmailCode();
        assertEquals(unusedCode, result);
    }

    /* sendVerificationEmail */
    @Test
    void testSendVerificationEmail_ShouldDeleteExistingToken() throws MessagingException {
        User userMock = mock(User.class);
        verificationTokenService.sendVerificationEmail(userMock);
        verify(verificationTokenRepository, times(1)).deleteByUser(userMock);
    }

    @Test
    void testSendVerificationEmail_ShouldSaveNewVerificationToken() throws MessagingException {
        String generatedCode = "123456";
        User userMock = mock(User.class);
        when(verificationTokenService.generateVerificationEmailCode()).thenReturn(generatedCode);

        ArgumentCaptor<VerificationToken> verificationTokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verificationTokenService.sendVerificationEmail(userMock);
        verify(verificationTokenRepository, times(1)).save(verificationTokenCaptor.capture());  // Verify save is called with a VerificationToken

        VerificationToken savedToken = verificationTokenCaptor.getValue();
        assertNotNull(savedToken);
        assertEquals(generatedCode, savedToken.getToken());
        assertEquals(userMock, savedToken.getUser());
    }

    @Test
    void testSendVerificationEmail_ShouldSendVerificationEmail() throws MessagingException {
        String generatedCode = "123456";
        User userMock = mock(User.class);
        when(verificationTokenService.generateVerificationEmailCode()).thenReturn(generatedCode);

        ArgumentCaptor<VerificationToken> verificationTokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verificationTokenService.sendVerificationEmail(userMock);
        verify(emailService, times(1)).sendRegistrationVerificationMail(eq(userMock), verificationTokenCaptor.capture());  // Verify email sending

        VerificationToken capturedToken = verificationTokenCaptor.getValue();
        assertEquals(generatedCode, capturedToken.getToken());
    }

    @Test
    void testSendVerificationEmail_ShouldSetExpiryDate() throws MessagingException {
        String generatedCode = "123456";
        User userMock = mock(User.class);
        when(verificationTokenService.generateVerificationEmailCode()).thenReturn(generatedCode);

        ArgumentCaptor<VerificationToken> verificationTokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verificationTokenService.sendVerificationEmail(userMock);
        verify(verificationTokenRepository, times(1)).save(verificationTokenCaptor.capture());

        // check within a second range to account for different machine speeds
        VerificationToken savedToken = verificationTokenCaptor.getValue();
        LocalDateTime expectedExpiryDate = LocalDateTime.now().plusMinutes(10);
        assertTrue(savedToken.getExpiryDate().isAfter(expectedExpiryDate.minusSeconds(1)) &&
                savedToken.getExpiryDate().isBefore(expectedExpiryDate.plusSeconds(1)));
    }

    /* verifyUserByToken() */
    @Test
    void testVerifyUserByToken_TokenUnused_ShouldReturnFalse() {
        String unusedCode = "123456";
        when(verificationTokenRepository.findByToken(unusedCode)).thenReturn(Optional.empty());
        assertFalse(verificationTokenService.verifyUserByToken(unusedCode));
    }

    @Test
    void testVerifyUserByToken_TokenExpired_ShouldReturnFalse() {
        String usedCode = "123456";
        VerificationToken mockToken = mock(VerificationToken.class);
        when(verificationTokenRepository.findByToken(usedCode)).thenReturn(Optional.of(mockToken));
        when(mockToken.isTokenExpired()).thenReturn(true);

        assertFalse(verificationTokenService.verifyUserByToken(usedCode));
    }

    @Test
    void testVerifyUserByToken_TokenExpired_ShouldDeleteToken() {
        String usedCode = "123456";
        VerificationToken mockToken = mock(VerificationToken.class);
        when(verificationTokenRepository.findByToken(usedCode)).thenReturn(Optional.of(mockToken));
        when(mockToken.isTokenExpired()).thenReturn(true);

        verificationTokenService.verifyUserByToken(usedCode);
        verify(verificationTokenRepository, times(1)).delete(mockToken);
    }

    @Test
    void testVerifyUserByToken_TokenValid_ShouldReturnTrue() {
        String usedCode = "123456";
        VerificationToken mockToken = mock(VerificationToken.class);
        User mockUser = mock(User.class);
        when(verificationTokenRepository.findByToken(usedCode)).thenReturn(Optional.of(mockToken));
        when(mockToken.isTokenExpired()).thenReturn(false);
        when(mockToken.getUser()).thenReturn(mockUser);

        assertTrue(verificationTokenService.verifyUserByToken(usedCode));
    }

    @Test
    void testVerifyUserByToken_TokenValid_ShouldDeleteToken() {
        String usedCode = "123456";
        VerificationToken mockToken = mock(VerificationToken.class);
        User mockUser = mock(User.class);
        when(verificationTokenRepository.findByToken(usedCode)).thenReturn(Optional.of(mockToken));
        when(mockToken.isTokenExpired()).thenReturn(false);
        when(mockToken.getUser()).thenReturn(mockUser);

        verificationTokenService.verifyUserByToken(usedCode);
        verify(verificationTokenRepository, times(1)).delete(mockToken);
    }

    @Test
    void testVerifyUserByToken_TokenValid_ShouldActivateUser() {
        String usedCode = "123456";
        VerificationToken mockToken = mock(VerificationToken.class);
        User mockUser = mock(User.class);
        when(verificationTokenRepository.findByToken(usedCode)).thenReturn(Optional.of(mockToken));
        when(mockToken.isTokenExpired()).thenReturn(false);
        when(mockToken.getUser()).thenReturn(mockUser);

        verificationTokenService.verifyUserByToken(usedCode);
        verify(mockUser, times(1)).setActivated(true);
        verify(userService, times(1)).saveUser(mockUser);
    }

    /* cleanExpiredTokens() */
    @Test
    void testCleanExpiredTokens_ExpiredToken_ShouldDeleteToken() {
        VerificationToken expiredTokenMock = mock(VerificationToken.class);
        User userMock = mock(User.class);
        when(expiredTokenMock.getUser()).thenReturn(userMock);
        when(verificationTokenRepository.findByExpiryDateBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredTokenMock));

        verificationTokenService.cleanExpiredTokens();
        verify(verificationTokenRepository, times(1)).delete(expiredTokenMock);
    }

    @Test
    void testCleanExpiredTokens_ExpiredTokenWithInactiveUser_ShouldDeleteUser() {
        VerificationToken expiredTokenMock = mock(VerificationToken.class);
        User userMock = mock(User.class);
        when(expiredTokenMock.getUser()).thenReturn(userMock);
        when(userMock.isActivated()).thenReturn(false);
        when(verificationTokenRepository.findByExpiryDateBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredTokenMock));

        verificationTokenService.cleanExpiredTokens();
        verify(verificationTokenRepository, times(1)).delete(expiredTokenMock);
        verify(userService, times(1)).deleteUser(userMock);
    }


    @Test
    void testCleanExpiredTokens_ExpiredTokenWithActiveUser_ShouldNotDeleteUser() {
        VerificationToken expiredTokenMock = mock(VerificationToken.class);
        User userMock = mock(User.class);
        when(expiredTokenMock.getUser()).thenReturn(userMock);
        when(userMock.isActivated()).thenReturn(true);
        when(verificationTokenRepository.findByExpiryDateBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredTokenMock));

        verificationTokenService.cleanExpiredTokens();
        verify(verificationTokenRepository, times(1)).delete(expiredTokenMock);
        verify(userService, never()).deleteUser(userMock);
    }

    @Test
    void testCleanExpiredTokens_NoExpiredTokens_ShouldNotDeleteAnything() {
        when(verificationTokenRepository.findByExpiryDateBefore(any(LocalDateTime.class)))
                .thenReturn(List.of());

        verificationTokenService.cleanExpiredTokens();
        verify(verificationTokenRepository, never()).delete(any());
        verify(userService, never()).deleteUser(any());
    }

}
