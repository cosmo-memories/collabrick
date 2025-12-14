package nz.ac.canterbury.seng302.homehelper.repository.chat;

import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing and querying ChatChannel entities from the database.
 */
@Repository
public interface ChatChannelRepository extends CrudRepository<ChatChannel, Long> {

    /**
     * Retrieves a list of chat channels associated with a renovation.
     *
     * @param renovation the renovation to retrieve channels from.
     * @return a list of chat channels associated with a renovation.
     */
    List<ChatChannel> findByRenovation(Renovation renovation);

    /**
     * Retrieves an optional of a chat channel associated with a renovation and a given name
     *
     * @param name       the name of the chat channel
     * @param renovation the renovation to retrieve the channel from
     * @return an optional of a chat channel
     */
    @Query("SELECT c FROM ChatChannel c WHERE LOWER(c.name) = LOWER(:name) and c.renovation = :renovation")
    Optional<ChatChannel> findByNameAndRenovation(String name, Renovation renovation);

    /**
     * Checks if a user is a member of a chat channel by querying the join table.
     *
     * @param channelId the ID of the chat channel
     * @param userId    the ID of the user
     * @return true if the user is a member of the channel, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM ChatChannel c JOIN c.members m WHERE c.id = :channelId AND m.id = :userId")
    boolean isUserMemberOfChannel(
            @Param("channelId") long channelId,
            @Param("userId") long userId
    );

    /**
     * Finds a list of chat channels based on a user and a renovation
     *
     * @param user       the logged-in user
     * @param renovation the renovation to find all channels from
     * @return a list of channels that match the user and renovation
     */
    @Query("""
                SELECT c
                FROM ChatChannel c
                WHERE c.renovation = :renovation
                  AND (:user MEMBER OF c.members)
            """)
    List<ChatChannel> findByUserAndRenovation(
            @Param("user") User user,
            @Param("renovation") Renovation renovation
    );

    /**
     * Finds the AI chat channel for the given user on the given renovation, if it exists.
     * @param renovation        Renovation
     * @param user              User
     * @return                  AI chat channel
     */
    @Query("SELECT c FROM ChatChannel c WHERE (c.renovation = :renovation) AND (:user MEMBER OF c.members) AND (c.name = 'brickAI')")
    Optional<ChatChannel> findAiChannel(Renovation renovation, User user);
}
