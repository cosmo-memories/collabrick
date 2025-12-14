package nz.ac.canterbury.seng302.homehelper.entity.renovation;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.InvitationStatus;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Entity class representing a renovation project.
 */
@Entity
public class Renovation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 600)
    private String description;

    @OneToMany(mappedBy = "renovation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<Room> rooms = new ArrayList<>();

    @OneToMany(mappedBy = "renovation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "renovation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "renovation", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<RenovationMember> members = new HashSet<>();

    @OneToMany(mappedBy = "renovation", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Invitation> invitations = new ArrayList<>();

    @OneToOne(mappedBy = "renovation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Budget budget;

    @OneToMany(mappedBy = "renovation", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ChatChannel> chatChannels = new ArrayList<>();

    @Embedded
    private Location location = new Location();

    @Column
    private boolean isPublic = false;

    @Column
    private boolean allowBrickAI = true;

    @Column
    @CreationTimestamp
    private LocalDateTime createdTimestamp;

    @OneToMany(mappedBy = "renovation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecentlyAccessedRenovation> recentlyAccessedRenovations = new ArrayList<>();

    @OneToMany(mappedBy = "renovation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LiveUpdate> liveUpdates = new ArrayList<>();

    /**
     * JPA required no-args constructor
     */
    protected Renovation() {

    }

    /**
     * Creates a new Renovation object.
     *
     * @param name        The name of the renovation.
     * @param description The description of the renovation.
     */
    public Renovation(String name, String description) {
        this.name = name;
        this.description = description;
        location = new Location();
        this.budget = new Budget(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
        this.budget.setRenovation(this);
    }

    /**
     * Gets the ID of the renovation.
     *
     * @return The renovation ID.
     */
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the name of the renovation.
     *
     * @return The renovation name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the name of the renovation.
     *
     * @param name The renovation name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the renovation.
     *
     * @return The renovation description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the renovation
     *
     * @param description The renovation description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the rooms included in this renovation.
     *
     * @return A list of rooms.
     */
    public List<Room> getRooms() {
        return rooms;
    }

    /**
     * Adds a new room to this renovation.
     *
     * @param room The room to add.
     */
    public void addRoom(Room room) {
        if (!rooms.contains(room)) {
            rooms.add(room);
            room.setRenovation(this);
        }
    }

    /**
     * Removes a room from this renovation.
     *
     * @param room The room to remove.
     */
    public void removeRoom(Room room) {
        if (rooms.contains(room)) {
            rooms.remove(room);
            room.setRenovation(null);
        }
    }

    /**
     * Gets the tasks included in this renovation.
     *
     * @return A list of tasks
     */
    public List<Task> getTasks() {
        return tasks;
    }

    /**
     * Adds a new task to this renovation
     *
     * @param task the task to add.
     */
    public void addTask(Task task) {
        if (!tasks.contains(task)) {
            tasks.add(task);
            task.setRenovation(this);
        }
    }

    /**
     * Indicates whether BrickAI is allowed to access this renovation's details.
     *
     * @return {@code true} if BrickAI access is allowed; {@code false} otherwise.
     */
    public boolean isAllowBrickAI() {
        return allowBrickAI;
    }

    /**
     * Sets whether BrickAI is allowed to access this renovation's details.
     *
     * @param allowBrickAi {@code true} to allow BrickAI access; {@code false} to disallow.
     */
    public void setAllowBrickAI(boolean allowBrickAi) {
        this.allowBrickAI = allowBrickAi;
    }


    /**
     * Gets the owner of the renovation.
     *
     * @return the owner of the renovation.
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Sets the owner of the renovation.
     *
     * @param owner the owner of the renovation.
     */
    public void setOwner(User owner) {
        this.owner = owner;
        if (owner != null && !owner.getRenovations().contains(this)) {
            owner.addRenovation(this);
            addMember(owner, RenovationMemberRole.OWNER);
        }
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
            tag.setRenovation(this);
        }
    }

    /**
     * Sets the visibility for this renovation
     *
     * @param isPublic the visibility of this renovation, true for public, false for private
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Gets the visibility of this renovation
     *
     * @return true for public, false for private, may return null value for renovations created before this variable was added, these are assumed private
     */
    public boolean getIsPublic() {
        return isPublic;
    }

    /**
     * Gets the timestamp of when the renovation was created.
     *
     * @return a LocalDateTime of when the renovation was created.
     */
    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    /**
     * Sets the timestamp of when the renovation was created.
     *
     * @param createdTimestamp a LocalDateTime of when the renovation was created.
     */
    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    /**
     * Gets the members associated with this renovation. Includes the owner of the renovation.
     *
     * @return a set of members associated this renovation.
     */
    public Set<RenovationMember> getMembers() {
        return members;
    }

    /**
     * Gets the members associated with this renovation sorted by role and name. Includes the owner of the renovation.
     *
     * @return a sorted list of members associated this renovation.
     */
    public List<RenovationMember> getSortedMembers() {
        return members.stream()
                .sorted(Comparator.comparing((RenovationMember member) -> member.getRole().ordinal())
                        .thenComparing((RenovationMember member) -> member.getUser().getFname() + " " + member.getUser().getLname()))
                .collect(Collectors.toList());
    }

    /**
     * Gets all emails and users who have been invited to the renovation
     *
     * @return invitations
     */
    public List<Invitation> getInvitations() {
        return invitations;
    }

    /**
     * Gets all invitations, sorted so that PENDING invitations come first.
     *
     * @return sorted invitations
     */
    public List<Invitation> getSortedInvitations() {
        return invitations.stream()
                .sorted(Comparator.comparing(
                        (Invitation inv) -> inv.getInvitationStatus() != InvitationStatus.PENDING
                ))
                .toList();
    }

    /**
     * Indicates if the provided user is a member of this renovation. If the user is the owner or a member, it will
     * return true.
     *
     * @param user the user to check membership.
     * @return true if the user is the owner or member of this renovation, false otherwise.
     */
    public boolean isMember(User user) {
        return members.stream().anyMatch(member -> member.getUser().equals(user));
    }

    /**
     * Adds a user to the renovation with a specific role.
     *
     * @param user The user to add.
     * @param role The role of the user.
     */
    public void addMember(User user, RenovationMemberRole role) {
        if (!isMember(user)) {
            RenovationMember member = new RenovationMember(this, user, role);
            members.add(member);
        }
    }

    /**
     * Removes a user from the renovation.
     *
     * @param user The user to remove.
     */
    public void removeMember(User user) {
        members.removeIf(m -> m.getUser().equals(user));
        user.getMemberships().removeIf(m -> m.getRenovation().equals(this));
    }

    /**
     * Gets the list of chat channels associated with our renovation
     *
     * @return list of chat channels
     */
    public List<ChatChannel> getChatChannels() {
        return chatChannels;
    }


    /**
     * Gets this renovation's budget
     *
     * @return the budget of this renovation
     */
    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    /**
     * Retrieve the string representation of a renovation.
     *
     * @return the string representation of a renovation.
     */
    @Override
    public String toString() {
        return "Renovation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", rooms=" + rooms +
                ", tags=" + tags.stream().map(Tag::getTag).toList() +
                ", isPublic=" + isPublic +
                ", createdTimestamp=" + createdTimestamp +
                '}';
    }

    /**
     * Checks if a renovation object is equal to another by comparing the ID's.
     *
     * @param o The object to compare with this renovation instance.
     * @return true if the object is a renovation and has the same ID.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Renovation that = (Renovation) o;
        return id == that.id;
    }

    /**
     * Computes the hash code for this renovation.
     *
     * @return The hash code of this renovation.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
