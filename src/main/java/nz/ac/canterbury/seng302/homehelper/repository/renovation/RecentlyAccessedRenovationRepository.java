package nz.ac.canterbury.seng302.homehelper.repository.renovation;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RecentlyAccessedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentlyAccessedRenovationRepository extends CrudRepository<RecentlyAccessedRenovation, Long> {

    /**
     * Finds all recently accessed renovation objects by renovation id
     * @param renovationId the id of the renovation
     * @return a list of all recently accessed renovations for a renovation
     */
    List<RecentlyAccessedRenovation>findAllRecentlyAccessedRenovationsByRenovationId(Long renovationId);

    /**
     * Check if a user has accessed a given renovation before
     *
     * @param renovation The renovation to check
     * @param user       The user to check
     * @return           An optional containing a RecentlyAccessedRenovation if a user has accessed the given renovation
     *                   before, otherwise empty.
     */
    @Query("SELECT r FROM RecentlyAccessedRenovation r WHERE r.renovation = :renovation AND r.user = :user")
    Optional<RecentlyAccessedRenovation>findByRenovationAndUser(Renovation renovation, User user);

    /**
     * Gets last 3 recently accessed renovations by a user
     *
     * @param user recently accessed renovation of user
     * @return List containing RecentlyAccessedRenovations, can be empty
     */
    List<RecentlyAccessedRenovation> findTop3ByUserOrderByTimeAccessedDesc(User user);


    /**
     * Removes all accesses of a renovation from non-members
     *
     * @param renovationId the id of the renovation that has been set to private
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RecentlyAccessedRenovation  r " +
            "WHERE r.renovation.id = :renovationId " +
            "AND NOT EXISTS (" +
            "SELECT 1 FROM RenovationMember m " +
            "WHERE m.renovation.id = r.renovation.id " +
            "AND m.user.id = r.user.id)")
    void deleteNonMembersFromPrivateRenovation(long renovationId);

    /**
     * Removes all recently accessed renovation entries for a given renovation
     *
     * @param renovationId the id of the renovation that is having its entries deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RecentlyAccessedRenovation  r " +
            "WHERE r.renovation.id = :renovationId ")
    void deleteAllRenovationAccessEntriesForRenovation(long renovationId);

    /**
     * Removes a recently accessed renovation entry for a user from a private renovation
     * @param renovationId the ID of the renovation
     * @param userId       the ID of the user
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RecentlyAccessedRenovation r " +
            "WHERE r.renovation.id = :renovationId " +
            "AND r.user.id = :userId " +
            "AND r.renovation.isPublic != true")
    void deleteUserRenovationAccessEntryForPrivateRenovation(long renovationId, long userId);
}
