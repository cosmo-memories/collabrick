package nz.ac.canterbury.seng302.homehelper.entity.chat;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a chat channel within a renovation.
 */
@Entity
public class ChatChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "renovation_id")
    private Renovation renovation;

    @ManyToMany
    @JoinTable(
            name = "chat_channel_member",
            joinColumns = @JoinColumn(name = "channel_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private List<User> members = new ArrayList<>();

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * Required no-args constructor for JPA.
     */
    protected ChatChannel() {
    }

    /**
     * Constructs a new ChatChannel with required fields for persisting.
     *
     * @param name       the name of the channel
     * @param renovation renovation this channel belongs to
     */
    public ChatChannel(String name, Renovation renovation) {
        this.name = name;
        this.renovation = renovation;
    }

    /**
     * Constructs a new ChatChannel with all fields.
     *
     * @param id         the channel ID
     * @param name       the name of the channel
     * @param renovation renovation this channel belongs to
     * @param members    list of users in the channel
     * @param messages   list of messages in the channel
     */
    public ChatChannel(long id, String name, Renovation renovation, List<User> members, List<ChatMessage> messages) {
        this.id = id;
        this.name = name;
        this.renovation = renovation;
        this.members = members;
        this.messages = messages;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Renovation getRenovation() {
        return renovation;
    }

    public List<User> getMembers() {
        return members;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void addMember(User user) {
        members.add(user);
    }

    public void setId(long id) { this.id = id; }
}
