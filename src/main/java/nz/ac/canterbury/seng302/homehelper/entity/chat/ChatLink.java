package nz.ac.canterbury.seng302.homehelper.entity.chat;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;

import java.util.List;

/**
 * Represents a mention within a chat channel message
 */
@Entity
public class ChatLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "message_id")
    private ChatMessage message;

    @Column
    private String text;

    @Column
    private String link;

    @Column
    private int startPosition;

    @Column
    private int endPosition;


    /**
     * Required no-args constructor for JPA.
     */
    protected ChatLink() {
    }


    /**
     * Constructs a new ChatMention with required fields for persisting.
     *
     * @param message the chat message associated with the mention.
     * @param text the text for the link
     * @param link the url to go to when the link is clicked
     * @param startPosition the start position of the first character of the mention in the message
     * @param endPosition the end position of the last character of the mention in the message
     */
    public ChatLink(ChatMessage message, String text, String link, int startPosition, int endPosition) {
        this.message = message;
        this.text = text;
        this.link = link;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public long getId() {
        return id;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public ChatMessage getChatMessage() { return message; }

    public String getText() { return text; }

    public String getLink() { return link; }


}

