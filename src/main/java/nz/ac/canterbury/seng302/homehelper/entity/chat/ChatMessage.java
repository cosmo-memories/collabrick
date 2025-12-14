package nz.ac.canterbury.seng302.homehelper.entity.chat;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a message sent within a chat channel.
 */
@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 2048)
    private String content;

    @Column(nullable = false)
    private Instant timestamp;

    @ManyToOne
    @JoinColumn(name = "channel_id")
    private ChatChannel channel;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMention> mentions = new ArrayList<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatLink> links = new ArrayList<>();

    /**
     * Required no-args constructor for JPA.
     */
    protected ChatMessage() {
    }

    /**
     * Constructs a new ChatMessage with required fields for persisting.
     *
     * @param content   the content of the message
     * @param timestamp the time the message was sent
     * @param channel   the channel this message belongs to
     * @param sender    the user who sent the message
     */
    public ChatMessage(String content, Instant timestamp, ChatChannel channel, User sender) {
        this.content = content;
        this.timestamp = timestamp;
        this.channel = channel;
        this.sender = sender;
    }

    /**
     * Constructs a new ChatMessage with all fields.
     *
     * @param id        the message ID
     * @param content   the content of the message
     * @param timestamp the time the message was sent
     * @param channel   the channel this message belongs to
     * @param sender    the user who sent the message
     */
    public ChatMessage(long id, String content, Instant timestamp, ChatChannel channel, User sender) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.channel = channel;
        this.sender = sender;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public ChatChannel getChannel() {
        return channel;
    }

    public User getSender() {
        return sender;
    }

    public List<ChatMention> getMentions() {
        return mentions;
    }

    public List<ChatLink> getLinks() { return links; }

}
