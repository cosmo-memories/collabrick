package nz.ac.canterbury.seng302.homehelper.model.user;

import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.InvitationStatus;

import java.util.Objects;

/**
 * Represents the public details of a user that can be shared or displayed without revealing
 * sensitive information. Also contains the users relationship to a specific renovation, including if they are a member
 * of a given renovation or if they have a pending invitation.
 */
public class PublicUserDetailsRenovation extends PublicUserDetails {
    private final boolean member;
    private boolean invited;
    private final long renovationId;
    private InvitationStatus invitationStatus;

    /**
     * Constructs a new PublicUserDetailsRenovation from a User entity.
     *
     * @param user    the user entity whose public details are being represented
     * @param member  true if the user is currently a member of the renovation
     * @param invited true if the user has been invited to the renovation
     */
    public PublicUserDetailsRenovation(User user, boolean member, boolean invited, long renovationId) {
        super(user);
        this.member = member;
        this.invited = invited;
        this.renovationId = renovationId;
    }

    public PublicUserDetailsRenovation(User user, long renovationId, InvitationStatus invitationStatus) {
        this(user, false, true, renovationId);
        this.invitationStatus = invitationStatus;
    }

    /**
     * Constructs a new RenovationUserSummary with provided user details.
     *
     * @param id        the user's ID
     * @param firstName the user's first name
     * @param lastName  the user's last name
     * @param email     the user's email
     * @param member    true if the user is a renovation member
     * @param invited   true if the user has been invited
     */
    public PublicUserDetailsRenovation(long id, String firstName, String lastName, String email, String image, boolean member, boolean invited, long renovationId) {
        super(id, firstName, lastName, email, image);
        this.member = member;
        this.invited = invited;
        this.renovationId = renovationId;
    }

    public PublicUserDetailsRenovation(long id, String firstName, String lastName, String email, String image, long renovationId, InvitationStatus invitationStatus) {
        this(id, firstName, lastName, email, image, false, true, renovationId);
        this.invitationStatus = invitationStatus;
    }

    public boolean isMember() {
        return member;
    }

    public boolean isInvited() {
        return invited;
    }

    public void setInvited(boolean invited) {
        this.invited = invited;
    }

    public long getRenovationId() {
        return renovationId;
    }

    public InvitationStatus getInvitationStatus() {
        return invitationStatus;
    }

    public void setInvitationStatus(InvitationStatus invitationStatus) {
        this.invitationStatus = invitationStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PublicUserDetailsRenovation that)) return false;
        if (!super.equals(o)) return false;
        return member == that.member && invited == that.invited;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), member, invited);
    }

    @Override
    public String toString() {
        return "PublicUserDetailsRenovation{" +
                "isMember=" + member +
                ", isInvited=" + invited +
                "} " + super.toString();
    }
}
