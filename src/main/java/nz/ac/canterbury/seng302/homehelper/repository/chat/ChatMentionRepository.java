package nz.ac.canterbury.seng302.homehelper.repository.chat;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMentionRepository extends JpaRepository<ChatMention, Long> {

    @Query("""
        select cm
        from ChatMention cm
        join fetch cm.message m
        join fetch m.channel ch
        where cm.mentionedUser.id = :userId
          and cm.seen = false
        order by m.timestamp desc
    """)
    List<ChatMention> findUnseenMentionsWithMessageAndChannel(Long userId);

    /**
     * Marks all mentions as seen for a specific user in a specific channel
     *
     * @param userId    the ID of the mentioned user
     * @param channelId the ID of the channel
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatMention cm " +
            "SET cm.seen = true " +
            "WHERE cm.mentionedUser.id = :userId " +
            "AND cm.message.channel.id = :channelId " +
            "AND cm.seen = false")
    void markMentionsAsSeenForUserInChannel(Long userId, Long channelId);
}
