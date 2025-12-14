package nz.ac.canterbury.seng302.homehelper.repository.auth;

import nz.ac.canterbury.seng302.homehelper.entity.user.ForgottenPasswordToken;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing forgotten password tokens in the database.
 * This interface provides methods for retrieving and deleting forgotten password tokens.
 */
@Repository
public interface ForgottenPasswordTokenRepository extends CrudRepository<ForgottenPasswordToken, UUID> {

    /**
     * Retrieves a forgotten password token associated with a given user.
     *
     * @param user The user whose forgotten password token is to be retrieved.
     * @return An Optional containing the forgotten password token if found, otherwise empty.
     */
    @Query("SELECT lpt FROM ForgottenPasswordToken lpt WHERE lpt.user = :user")
    Optional<ForgottenPasswordToken> findByUser(@Param("user") User user);

    /**
     * Deletes all forgotten password tokens that have expired.
     * This method removes any tokens where the expiry date is in the past.
     */
    @Modifying
    @Query("DELETE FROM ForgottenPasswordToken pt WHERE pt.expiryDate <= CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}
