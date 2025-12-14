package nz.ac.canterbury.seng302.homehelper.repository.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.key.TagKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Tag Repository accessor; extends CrudRepository
 */
public interface TagRepository extends CrudRepository<Tag, TagKey> {

    /**
     * Find tags that match the supplied renovation ID
     *
     * @param id ID of renovation
     * @return List of matching tags
     */
    @Query("SELECT t FROM Tag t WHERE t.renovation.id = :id")
    List<Tag> findByRenovation(long id);

    /**
     * Find renovations that match the supplied tag text
     *
     * @param tag Tag text string
     * @return List of matching renovations
     */
    @Query("SELECT t FROM Tag t WHERE t.tag = :tag")
    List<Tag> findByTag(String tag);


    /**
     * Find a list of tag strings that match or partially match the search term string
     *
     * @param searchTerm User inputted search string
     * @return List of matching tag names as strings
     */
    @Query("SELECT DISTINCT t.tag from Tag t WHERE LOWER(t.tag) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<String> getDistinctTagsBySearchTerm(String searchTerm);

    /**
     * Retrieve all distinct tag names that contain the given search term,
     * but only from renovations which are either public or owned by the specified user.
     * COALESCE - makes result false if null is returned by db as isPublic can be null in entity class
     *
     * @param searchTerm the substring to match within tag names (case-insensitive)
     * @param userId     the ID of the user; tags from this user's private renovations will also be included
     * @return a list of unique tag strings satisfying the search and visibility criteria
     */
    @Query("""
            SELECT DISTINCT t.tag FROM Tag t WHERE LOWER(t.tag) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            AND (COALESCE(t.renovation.isPublic, FALSE) = TRUE OR t.renovation.owner.id = :userId)""")
    List<String> searchForOwnedAndPublicTags(String searchTerm, Long userId);

    /**
     * Find renovations that match a list of tags and only return them if they are public, and match all the tags and search term
     *
     * @param tags       the tags to match
     * @param tagCount   the amount of tags being handed in
     * @param searchTerm the term to search by
     * @param pageable   to allow the renovations to be returned in a pageable form
     * @return a list of public renovations that match all tags passed in
     */
    // ChatGPT helped write this query
    @Query("SELECT t.renovation FROM Tag t WHERE t.tag IN :tags AND t.renovation.isPublic = true " +
            "AND (:searchTerm IS NULL OR :searchTerm = '' " +
            "OR LOWER(t.renovation.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(t.renovation.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))" +
            "GROUP BY t.renovation HAVING COUNT(DISTINCT t.tag) = :tagCount " +
            "ORDER BY t.renovation.createdTimestamp DESC")
    Page<Renovation> findPublicRenovationByTags(List<String> tags, Integer tagCount, String searchTerm, Pageable pageable);

}
