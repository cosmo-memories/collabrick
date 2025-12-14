package nz.ac.canterbury.seng302.homehelper.integration.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.entity.user.VerificationToken;
import nz.ac.canterbury.seng302.homehelper.repository.auth.VerificationTokenRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.auth.VerificationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@SpringBootTest
@Transactional
public class VerificationEmailServiceTest {

    @MockBean
    private EmailService emailService;

    @Autowired
    private VerificationTokenService verificationTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        // User with valid token
        user = new User("Jane", "Doe", "test@tst.com", "Abc123!!", "Abc123!!");
        user = userRepository.save(user);
    }

    @Test
    public void generateVerificationCode_methodCalled_returnSixDigitCode() {
        String code = verificationTokenService.generateVerificationEmailCode();
        assertNotNull(code);
        assertEquals(6, code.length());
    }

    @Test
    public void sendVerificationEmail_validParameters_sendEmailWithVerificationCode() throws Exception {
        verificationTokenService.sendVerificationEmail(user);
        verify(emailService, times(1)).sendRegistrationVerificationMail(eq(user), any());
    }

    @Test
    public void verifyVerificationCode_validToken_returnTrue() {
        String validCode = "012345";
        LocalDateTime validTime = LocalDateTime.now().plusMinutes(10);
        VerificationToken validToken = new VerificationToken(validCode, user, validTime);
        verificationTokenRepository.save(validToken);

        assertTrue(verificationTokenService.verifyUserByToken(validCode));
    }

    @Test
    public void verifyVerificationCode_invalidInputtedCode_returnFalse() {
        // The validCode is what the user would receive in the email
        String validCode = "012345";
        LocalDateTime validTime = LocalDateTime.now().plusMinutes(10);
        VerificationToken validToken = new VerificationToken(validCode, user, validTime);
        verificationTokenRepository.save(validToken);

        // The "000000" is an incorrect code that is entered by the user and doesn't match the validCode
        assertFalse(verificationTokenService.verifyUserByToken("000000"));
    }

    @Test
    public void verifyVerificationCode_expiredToken_returnFalseAndTokenRemovedFromRepository() {
        String expiredCode = "012345";
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(10);
        VerificationToken expiredToken = new VerificationToken(expiredCode, user, expiredTime);
        verificationTokenRepository.save(expiredToken);

        assertFalse(verificationTokenService.verifyUserByToken(expiredCode));

        // Checks if the token is removed from the repository
        Optional<VerificationToken> foundExpiredToken = verificationTokenRepository.findByUser(user);
        assertTrue(foundExpiredToken.isEmpty());
    }

    @Test
    public void verifyVerificationCode_noTokenExists_returnFalse() {
        assertFalse(verificationTokenService.verifyUserByToken("012345"));
    }
}
