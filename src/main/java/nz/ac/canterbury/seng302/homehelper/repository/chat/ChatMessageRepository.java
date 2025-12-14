package nz.ac.canterbury.seng302.homehelper.repository.chat;

import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMessage;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for accessing and querying ChatMessage entities from the database.
 */
@Repository
public interface ChatMessageRepository extends CrudRepository<ChatMessage, Long> {

    /**
     * Retrieves the most recent messages for a given chat channel, up to a specific limit. Messages are ordered by
     * timestamp (newest first) and then by ID (descending) to ensure consistent ordering when timestamps are equal.
     *
     * @param channelId the chat channel ID to retrieve messages from
     * @param limit     the maximum number of messages to return
     * @return a list of the most recent ChatMessage objects, ordered by timestamp (DESC) and ID (DESC)
     */
    @Query("""
            SELECT m FROM ChatMessage m
            WHERE m.channel.id = :channelId
            ORDER BY m.timestamp DESC, m.id DESC
            LIMIT :limit
            """)
    List<ChatMessage> findLatestMessages(
            @Param("channelId") long channelId,
            @Param("limit") int limit
    );

    /**
     * Retrieves the most recent messages for a given chat channel, excluding a specific user, up to a specific limit.
     * Messages are ordered by timestamp (newest first) and then by ID (descending) to ensure consistent order when timestamps are equal.
     *
     * @param channelId the chat channel ID to retrieve messages from
     * @param user the user to exclude
     * @param limit the maximum number of messages to return
     * @return a list of the most recent ChatMessage objects, ordered by timestamp (DESC) and ID (DESC)
     */
    @Query("""
            SELECT m from ChatMessage m
            WHERE m.channel.id = :channelId
            AND NOT m.sender = :user
            AND m.sender.allowBrickAIChatAccess = true
            ORDER BY m.timestamp DESC, m.id DESC
            LIMIT :limit""")
    List<ChatMessage> findLatestMessagesExcludingUser(
            @Param("channelId") long channelId,
            @Param("user") User user,
            @Param("limit") int limit
    );

    /**
     * Retrieves a batch of messages older than the specified timestamp and message ID for a given chat channel, up to
     * a specific limit. Messages are ordered by timestamp (newest first) and then by ID (descending) to ensure
     * consistent ordering when timestamps are equal.
     *
     * @param channelId the chat channel ID to retrieve messages from
     * @param timestamp the timestamp before which messages should be retrieved
     * @param messageId the ID of the message before which messages should be retrieved (used when timestamps are equal)
     * @param pageable  the pageable containing maximum number of messages to return
     * @return a list of ChatMessage objects older than the specified timestamp and ID, ordered by timestamp (DESC)
     * and ID (DESC)
     */
    @Query("""
            SELECT m FROM ChatMessage m
            WHERE m.channel.id = :channelId
            AND (m.timestamp < :timestamp OR (m.timestamp = :timestamp AND m.id < :messageId))
            ORDER BY m.timestamp DESC, m.id DESC
            """)
    List<ChatMessage> findPreviousMessages(
            @Param("channelId") long channelId,
            @Param("timestamp") Instant timestamp,
            @Param("messageId") long messageId,
            Pageable pageable
    );



    /**
     * Retrieves messages sent after a specified timestamp and message ID in a given chat channel.
     * Messages are ordered by timestamp (oldest first) and then by ID (ascending) to ensure consistent ordering
     * when timestamps are equal.
     *
     * @param channelId the chat channel ID to retrieve messages from
     * @param timestamp the reference timestamp; only messages after this timestamp are returned
     * @param messageId the reference message ID; only messages with ID greater than this value are returned
     * @param pageable  the Pageable containing the maximum number of messages to return
     * @return a list of ChatMessage objects sent after the given timestamp and message ID, ordered by timestamp (ASC)
     *         and ID (ASC)
     */
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.channel.id = :channelId
        AND (m.timestamp >= :timestamp AND m.id > :messageId)
        ORDER BY m.timestamp ASC, m.id ASC
        """)
    List<ChatMessage> findNextMessages(
            @Param("channelId") long channelId,
            @Param("timestamp") Instant timestamp,
            @Param("messageId") long messageId,
            Pageable pageable
    );


    /**
     * Retrieves up to 10 chat messages in the given channel that were sent at or before
     * the specified timestamp, ordered from newest to oldest (descending).
     * This method is used to fetch context messages leading up to a point
     * in the conversation.
     *
     * @param channelId the ID of the chat channel
     * @param timestamp the reference timestamp
     * @return a list of up to 50 {@link ChatMessage} objects from the channel, before or at the given timestamp,
     *         ordered by timestamp descending
     */
    List<ChatMessage> findTop6ByChannelIdAndTimestampLessThanEqualOrderByTimestampDesc(
            long channelId,
            Instant timestamp
    );


    List<ChatMessage> findTop10ByChannelIdAndTimestampLessThanEqualOrderByTimestampDesc(
            long channelId,
            Instant timestamp
    );

    /**
     * Retrieves up to 10 chat messages in the given channel that were after
     * the specified timestamp, ordered from oldest to newest (ascending).
     * This method is used to fetch context messages following a point
     * in the conversation.
     *
     * @param channelId the ID of the chat channel
     * @param timestamp the reference timestamp
     * @return a list of up to 50 {@link ChatMessage} objects from the channel, after or at the given timestamp,
     *         ordered by timestamp ascending
     */
    List<ChatMessage> findTop5ByChannelIdAndTimestampGreaterThanOrderByTimestampAsc(
            long channelId,
            Instant timestamp
    );

}