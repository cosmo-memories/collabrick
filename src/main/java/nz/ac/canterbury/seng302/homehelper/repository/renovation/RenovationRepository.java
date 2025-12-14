package nz.ac.canterbury.seng302.homehelper.repository.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Renovation repository accessor using Spring's @link{CrudRepository}.
 */
public interface RenovationRepository extends PagingAndSortingRepository<Renovation, Long>, CrudRepository<Renovation, Long> {
    List<Renovation> findAll();

    /**
     * Finds a renovation by its exact name (case-insensitive).
     *
     * @param name The name of the renovation.
     * @return An Optional containing the renovation if found, otherwise empty.
     */
    @Query("SELECT r FROM Renovation r WHERE LOWER(r.name) = LOWER(:name)")
    Optional<Renovation> findByName(String name);

    /**
     * Finds a renovation by its id
     *
     * @param id the id of the renovation
     * @return an optional of the renovation
     */
    @Query("SELECT r from Renovation  r WHERE r.id = :id")
    Optional<Renovation> findById(long id);

    /**
     * Finds all renovations that the provided user owns.
     *
     * @param owner the user to find owned renovations.
     * @return a list of renovations that belong to the given user
     */
    @Query("SELECT r from Renovation r where r.owner = :owner")
    Page<Renovation> findByUserId(User owner, Pageable pageable);

    /**
     * Finds a renovation by its ID and owner.
     *
     * @param owner        the user that should own the renovation.
     * @param renovationId the id of the renovations.
     * @return a renovation or null
     */
    @Query("SELECT r from Renovation r where r.id = :renovationId and r.owner = :owner")
    List<Renovation> findByRenovationIdAndUser(long renovationId, User owner);

    /**
     * Finds the renovation by its name and owner.
     *
     * @param name  the name of the renovation.
     * @param owner the user that should own the renovation.
     * @return an optional of renovations which have the requested name and user.
     */
    @Query("SELECT r FROM Renovation r WHERE LOWER(r.name) = LOWER(:name) and r.owner = :owner")
    List<Renovation> findByNameAndUser(String name, User owner);

    /**
     * Retrieves a paginated list of public Renovation entities that match the given search criteria.
     * If the query is empty, all public renovations are considered.
     * If tags are provided, only renovations that contain all the specified tags are returned.
     *
     * @param query    the search string to match against the renovation's name or description.
     *                 If empty, this filter is ignored.
     * @param tags     a list of tag names to filter renovations by. If empty, this filter is ignored.
     * @param pageable the pagination and sorting information.
     * @return a Page of public Renovation entities matching the criteria, sorted by creation timestamp descending.
     */
    @Query("""
            SELECT r
            FROM Renovation r
            WHERE r.isPublic = true
              AND (
                  :query = '' OR
                  LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
                  LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%'))
              )
              AND (
                  :#{#tags.isEmpty()} = true OR
                  (SELECT COUNT(DISTINCT t.tag)
                   FROM Tag t
                   WHERE t.renovation = r AND LOWER(t.tag) IN :tags) = :#{#tags.size()}
              )
            ORDER BY r.createdTimestamp DESC
            """)
    Page<Renovation> findPublic(
            String query,
            List<String> tags,
            Pageable pageable
    );

    /**
     * Retrieves a paginated list of public Renovation entities that are created by a specific user and match
     * the given search criteria.
     * If the query is empty, all public renovations are considered.
     * If tags are provided, only renovations that contain all the specified tags are returned.
     *
     * @param owner    the User who owns the renovations.
     * @param query    the search string to match against the renovation's name or description.
     *                 If empty, this filter is ignored.
     * @param tags     a list of tag names to filter renovations by. If empty, this filter is ignored.
     * @param pageable the pagination and sorting information.
     * @return a Page of Renovation entities owned by the user and matching the criteria, sorted by creation
     * timestamp descending.
     */
    @Query("""
            SELECT r
            FROM Renovation r
            WHERE 
                (r.owner = :owner OR EXISTS (
                    SELECT 1
                    FROM RenovationMember rm
                    WHERE rm MEMBER OF r.members
                      AND rm.user = :owner
                ))
                AND (
                    :query = '' OR
                    LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
                    LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%'))
                )
                AND (
                    :#{#tags.isEmpty()} = true OR
                    (SELECT COUNT(DISTINCT t.tag)
                     FROM Tag t
                     WHERE t.renovation = r AND LOWER(t.tag) IN :tags)
                     = :#{#tags.size()}
                )
            ORDER BY r.createdTimestamp DESC
            """)
    Page<Renovation> findByUser(User owner, String query, List<String> tags, Pageable pageable);

    /**
     * Finds all renovations owned by the given user.
     *
     * @param user     the user who owns the renovations
     * @param pageable pagination information
     * @return a paginated list of renovations owned by the user, ordered by creation timestamp (most recent first)
     */
    @Query("select r from Renovation r where r.owner = :user  ORDER BY r.createdTimestamp DESC ")
    Page<Renovation> findUserRenovations(@Param("user") User user, Pageable pageable);

    /**
     * Finds all renovations where the given user is a member but not the owner.
     *
     * @param user     the user who is a member of the renovations
     * @param pageable pagination information
     * @return a paginated list of renovations the user is a member of (excluding those they own),
     * ordered by creation timestamp (most recent first)
     */
    @Query("SELECT r FROM Renovation r WHERE r.owner != :user and EXISTS (SELECT 1 FROM RenovationMember rm WHERE rm MEMBER OF r.members AND rm.user = :user) ORDER BY r.createdTimestamp DESC")
    Page<Renovation> findByUserWhereUserIsMember(@Param("user") User user, Pageable pageable);

    /**
     * Finds all renovations where the given user is either the owner or a member.
     *
     * @param user     the user to check ownership or membership against
     * @param pageable pagination information
     * @return a paginated list of renovations where the user is either the owner or a member,
     * ordered by creation timestamp (most recent first)
     */
    @Query("""
                SELECT r
                FROM Renovation r
                WHERE (r.owner = :user OR EXISTS (
                SELECT 1 FROM RenovationMember rm WHERE rm MEMBER OF r.members AND rm.user = :user ))
                ORDER BY r.createdTimestamp DESC
            """)
    Page<Renovation> findByUserOrMembership(
            @Param("user") User user,
            Pageable pageable);

    /**
     * Get the total number of renovations the given user is associated with.
     * @param user      User
     * @return          Integer
     */
    @Query("""
                SELECT COUNT(r)
                FROM Renovation r
                WHERE (r.owner = :user OR EXISTS (
                SELECT 1 FROM RenovationMember rm WHERE rm MEMBER OF r.members AND rm.user = :user ))
           """)
    int sumForUser(User user);
}
