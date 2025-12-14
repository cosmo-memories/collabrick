package nz.ac.canterbury.seng302.homehelper.repository.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.key.RenovationMemberKey;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Renovation member repository accessor using Spring's @link{CrudRepository}
 */
@Repository
public interface RenovationMemberRepository extends JpaRepository<RenovationMember, RenovationMemberKey> {

    /**
     * Finds all RenovationMember entities associated with a given Renovation.
     *
     * @param renovation the renovation to find members for
     * @return a list of RenovationMember entities belonging to the given renovation
     */
    List<RenovationMember> findByRenovation(Renovation renovation);

    /**
     * Finds all RenovationMember entities associated with a given User.
     *
     * @param user the user to find renovation memberships for
     * @return a list of RenovationMember entities where the user is a member
     */
    List<RenovationMember> findByUser(User user);


    /**
     * Finds renovation members who have collaborated with the given user on any renovation,
     * excluding the user themselves.
     * Optionally, filters results by a search query matching either email address or full name (case-insensitive).
     *
     * @param user  the user whose renovation collaborators are to be found
     * @param query the optional search string to filter collaborators by email or full name;
     *              use an empty string to disable filtering
     * @return a list of RenovationMember entries for other users who have shared
     * a renovation with the given user and match the optional query
     */
    @Query("""
            SELECT DISTINCT r2.user
            FROM RenovationMember r1
            JOIN RenovationMember r2 ON r1.renovation = r2.renovation
            WHERE r1.user = :user
              AND r2.user != :user
              AND (
                  :query = '' OR
                  LOWER(r2.user.email) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            """)
    List<User> findCollaboratorsInRenovations(User user, String query);

    @Query("""
        SELECT distinct user
            FROM RenovationMember
            WHERE user = :user
            AND renovation = :renovation
        """)
    List<User> checkMembership(User user, Renovation renovation);

    RenovationMember findByRenovationAndUser(Renovation renovation, User user);
}
