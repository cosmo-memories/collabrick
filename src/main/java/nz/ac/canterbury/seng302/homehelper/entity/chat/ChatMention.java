package nz.ac.canterbury.seng302.homehelper.entity.chat;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;

import java.util.List;

/**
 * Represents a mention within a chat channel message
 */
@Entity
public class ChatMention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "message_id")
    private ChatMessage message;

    @ManyToOne
    @JoinColumn(name = "mentioned_user_id")
    private User mentionedUser;

    @Column
    private int startPosition;

    @Column
    private int endPosition;

    @Column
    private boolean seen = false;

    /**
     * Required no-args constructor for JPA.
     */
    protected ChatMention() {
    }

    /**
     * Constructs a new ChatMention with required fields for persisting.
     *
     * @param message the chat message associated with the mention.
     * @param mentionedUser the user that was mentioned in the message.
     * @param startPosition the start position of the first character of the mention in the message
     * @param endPosition the end position of the last character of the mention in the message
     */
    public ChatMention(ChatMessage message, User mentionedUser, int startPosition, int endPosition) {
        this.message = message;
        this.mentionedUser = mentionedUser;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public long getId() {
        return id;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public User getMentionedUser() {
        return mentionedUser;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public ChatMessage getChatMessage() { return message; }

    public boolean isSeen() { return seen; }

    public void setSeen(boolean seen) { this.seen = seen; }

    @Override
    public String toString() {
        return "ChatMention{" +
                "id=" + id +
                ", message=" + message.toString() +
                ", mentionedUser=" + mentionedUser.toString() +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                '}';
    }
}
