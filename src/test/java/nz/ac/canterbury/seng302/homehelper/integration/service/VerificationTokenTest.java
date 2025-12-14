package nz.ac.canterbury.seng302.homehelper.integration.service;


import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.entity.user.VerificationToken;
import nz.ac.canterbury.seng302.homehelper.repository.auth.VerificationTokenRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.auth.VerificationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class VerificationTokenTest {

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenService verificationTokenService;

    private LocalDateTime now;
    private VerificationToken verificationToken;
    private VerificationToken expiredVerificationToken;
    private User user;
    private User otherUser;

    @BeforeEach
    public void setUp() {
        now = LocalDateTime.now();

        // User with valid token
        user = new User("Jane", "Doe", "test@tst.com", "Abc123!!", "Abc123!!");
        user = userRepository.save(user);

        verificationToken = new VerificationToken("012345", user, now.plusMinutes(10));

        // User with expired token
        otherUser = new User("Doe", "Jane", "test@testagain.com", "123Abc!!", "123Abc!!");
        otherUser = userRepository.save(otherUser);

        expiredVerificationToken = new VerificationToken("54321", otherUser, now.minusMinutes(10));
    }

    @Test
    public void savingNewVerificationToken_ValidVerificationToken_TokenSaved() {
        VerificationToken insertedVerificationToken = verificationTokenRepository.save(verificationToken);

        assertNotNull(insertedVerificationToken);
        assertEquals("012345", insertedVerificationToken.getToken());
        assertEquals(now.plusMinutes(10), insertedVerificationToken.getExpiryDate());
    }

    @Test
    public void findingTokensByExpiryDate_ExpiredTokenExists_NonEmptyListReturned() {
        // No need to save the valid token
        verificationTokenRepository.save(verificationToken);

        VerificationToken insertedExpiredVerificationToken = verificationTokenRepository.save(expiredVerificationToken);

        List<VerificationToken> expiredTokens = verificationTokenRepository.findByExpiryDateBefore(now);

        // Checks that only the expired token is found and not the valid one
        assertEquals(1, expiredTokens.size());

        VerificationToken foundToken = expiredTokens.get(0);
        assertEquals(insertedExpiredVerificationToken.getId(), foundToken.getId());
    }

    @Test
    public void deleteTokenByUser_ValidTokenAndUser_TokenDeleted() {
        verificationTokenRepository.save(verificationToken);
        verificationTokenRepository.deleteByUser(user);
        assertFalse(verificationTokenRepository.findByUser(user).isPresent());
    }

    @Test
    public void cleanExpiredVerificationToken_ExpiredToken_TokenRemoved() {
        verificationTokenRepository.save(expiredVerificationToken);
        verificationTokenService.cleanExpiredTokens();
        assertFalse(verificationTokenRepository.findByUser(otherUser).isPresent());
    }

}
