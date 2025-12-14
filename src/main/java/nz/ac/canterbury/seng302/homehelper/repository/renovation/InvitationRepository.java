package nz.ac.canterbury.seng302.homehelper.repository.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends CrudRepository<Invitation, UUID> {


    /**
     * Finds an invitation by its id
     *
     * @param id the id of the invitation
     * @return An optional containing the invitation if found, otherwise empty.
     */
    @Query("SELECT i from Invitation i WHERE i.id = :id")
    Optional<Invitation> findByInvitationId(UUID id);


    /**
     * Finds an invitation by its renovation and user
     *
     * @param renovation the associated renovation
     * @param user       the associated user
     * @return An optional containing the invitation if found, otherwise empty.
     * Invitations between a user and renovation are unique so no need for a list.
     */
    @Query("Select i FROM Invitation i WHERE i.renovation = :renovation and i.user = :user")
    Optional<Invitation> findByRenovationAndUser(Renovation renovation, User user);

    /**
     * Finds an invitation by its renovation and user's email
     *
     * @param renovation the associated renovation
     * @param email      the email associated to the user being invited
     * @return A list containing the invitation if found, otherwise empty.
     * Invitations between a user and renovation are unique so no need for a list.
     */
    @Query("Select i FROM Invitation i LEFT JOIN i.user u WHERE i.renovation = :renovation and (i.user.email = :email or i.email = :email)")
    List<Invitation> findByRenovationAndEmail(Renovation renovation, String email);

    /**
     * Returns a list of Invitations associated with the specified Renovation.
     *
     * @param reno Renovation
     * @return List of Invitations
     */
    @Query("SELECT i FROM Invitation i WHERE i.renovation = :reno")
    List<Invitation> findByRenovation(Renovation reno);

    /**
     * Returns a list of Invitations associated with the specified User.
     *
     * @param user User
     * @return List of Invitations
     */
    @Query("SELECT i FROM Invitation i WHERE i.user = :user")
    List<Invitation> findByUser(User user);

    /**
     * Returns a list of Invitations associated with the specified email address.
     * These invitations may or may not have been sent to registered Users.
     *
     * @param email Email address string
     * @return List of Invitations
     */
    @Query("SELECT i FROM Invitation i LEFT JOIN i.user u WHERE i.email = :email OR (u.email = :email)")
    List<Invitation> findByEmail(String email);

    /**
     * Retrieves a list of invitations for a given renovation owner where the email associated
     * with the invitation matches the provided query string (case-insensitive).
     * If the invitation is linked to a registered user, the user's email is used for matching.
     * Otherwise, the fallback email field on the invitation entity is used.
     *
     * @param owner the owner of the renovation for which invitations are being searched
     * @param query the email query string to match against the user or invitation email
     * @return a list of invitations matching the email query under the specified renovation owner
     */
    @Query("""
                SELECT i FROM Invitation i
                LEFT JOIN i.user u
                WHERE i.renovation.owner = :owner
                  AND LOWER(COALESCE(u.email, i.email)) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    List<Invitation> findByRenovationOwner(User owner, String query);


    /**
     * Gets all expired invitations
     *
     * @param time the current time
     * @return a list of expired invitations
     */
    @Query("SELECT i FROM Invitation i WHERE i.expiryDate < :time ")
    List<Invitation> findByExpiryDateBefore(LocalDateTime time);


    /**
     * Retrieves a list of invitations belonging to a user where they have been marked as accepted pending
     * registration.
     *
     * @param email the email to find accepted pending registration invitations
     * @return A list of invitations
     */
    List<Invitation> findByEmailAndAcceptedPendingRegistrationIsTrue(String email);


    /**
     * Deletes invitation by a renovation and user
     * @param renovation the renovation
     * @param email the users email
     */
    void deleteByRenovationAndEmail(Renovation renovation, String email);
}

