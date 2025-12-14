package nz.ac.canterbury.seng302.homehelper.entity.renovation;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.key.RenovationMemberKey;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole;

/**
 * Entity representing a user's membership in a renovation project.
 */
@Entity
public class RenovationMember {

    /**
     * Composite key consisting of renovation ID and user ID.
     */
    @EmbeddedId
    private RenovationMemberKey id;

    /**
     * Reference to the associated Renovation entity.
     */
    @ManyToOne
    @MapsId("renovationId")
    @JoinColumn(name = "renovation_id")
    private Renovation renovation;

    /**
     * Reference to the associated User entity.
     */
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Role of the user in the reference (e.g., OWNER, MEMBER)
     */
    @Column(nullable = false)
    private RenovationMemberRole role;

    /**
     * Constructs a new RenovationMember entity with the given renovation, user, and role.
     *
     * @param renovation The renovation the user is a member of.
     * @param user       The user who is a member.
     * @param role       The user's role in the renovation.
     */
    public RenovationMember(Renovation renovation, User user, RenovationMemberRole role) {
        this.id = new RenovationMemberKey(renovation.getId(), user.getId());
        this.renovation = renovation;
        this.user = user;
        this.role = role;
    }

    /**
     * Default constructor required by JPA.
     */
    public RenovationMember() {

    }

    public RenovationMemberKey getId() {
        return id;
    }

    public Renovation getRenovation() {
        return renovation;
    }

    public User getUser() {
        return user;
    }

    public RenovationMemberRole getRole() {
        return role;
    }

    public void setRole(RenovationMemberRole role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "RenovationMember{" +
                ", renovation=" + renovation.getName() +
                ", user=" + user.getFname() + " " + user.getLname() +
                ", role=" + role +
                '}';
    }
}
