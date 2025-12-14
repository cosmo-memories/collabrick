package nz.ac.canterbury.seng302.homehelper.repository.auth;

import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.entity.user.VerificationToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * VerificationToken repository accessor using Spring's @link{CrudRepository}
 */
public interface VerificationTokenRepository extends CrudRepository<VerificationToken, Long> {
    /**
     * Gets all expired tokens
     *
     * @param time the current time
     * @return a list of expired verification tokens
     */
    @Query("SELECT vt FROM VerificationToken vt WHERE vt.expiryDate < :time ")
    List<VerificationToken> findByExpiryDateBefore(LocalDateTime time);

    /**
     * Finds a verification token by user
     *
     * @param user the user whose token is being found
     * @return an optional containing the token if found
     */
    @Query("SELECT vt FROM VerificationToken vt WHERE vt.user = :user")
    Optional<VerificationToken> findByUser(@Param("user") User user);

    /**
     * Finds a verification token by the token
     *
     * @param token the token
     * @return an optional containing the verification token if found
     */
    @Query("SELECT vt FROM VerificationToken vt WHERE vt.token = :token")
    Optional<VerificationToken> findByToken(String token);

    /**
     * Deletes a token given a user
     *
     * @param user the user linked to the token
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.user = :user")
    void deleteByUser(@Param("user") User user);

}
