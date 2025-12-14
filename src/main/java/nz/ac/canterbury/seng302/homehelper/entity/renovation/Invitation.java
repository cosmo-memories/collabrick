package nz.ac.canterbury.seng302.homehelper.entity.renovation;


import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.InvitationStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Entity representing an invitation token for inviting users to a renovation. Each token is associated with a specific
 * user and renovation
 */
@Entity
public class Invitation {


    /**
     * Unique identifier for the invitation token, automatically generated as a UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    /**
     * The user being invited to the renovation, if they exist
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Store invitee's email only if they do not have an account already
     */
    @Column
    private String email;

    /**
     * The renovation the user is being invited to
     */
    @ManyToOne
    @JoinColumn(name = "renovation_id", nullable = false)
    private Renovation renovation;


    /**
     * The status of the invitation, either pending, accepted, declined or expired
     */
    @Column
    private InvitationStatus invitationStatus;

    /**
     * The datetime that this invitation will become expired
     */
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    /**
     * Flag indicating if the user has accepted the invite via email but needs to complete the registration process.
     */
    @Column
    private boolean acceptedPendingRegistration;

    @OneToMany(mappedBy = "invitation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LiveUpdate> liveUpdates = new ArrayList<>();

    /**
     * JPA Constructor
     */
    public Invitation() {


    }


    /**
     * Constructs an Invitation with a specified User and Renovation.
     * Use if the invite recipient already has an account.
     *
     * @param user       User being invited
     * @param renovation Renovation the User is invited to
     */
    public Invitation(User user, Renovation renovation) {
        this.user = user;
        this.renovation = renovation;
        this.invitationStatus = InvitationStatus.PENDING;
        this.email = user.getEmail();
        this.expiryDate = LocalDateTime.now().plusWeeks(1);
    }

    /**
     * Constructs an Invitation with a specified email and Renovation.
     * Use if there is no User registered to the invitee's email address.
     *
     * @param email      Invitee's email
     * @param renovation Renovation the recipient is invited to
     */
    public Invitation(String email, Renovation renovation) {
        this.email = email;
        this.renovation = renovation;
        this.invitationStatus = InvitationStatus.PENDING;
        this.expiryDate = LocalDateTime.now().plusWeeks(1);
    }


    /**
     * Gets the unique identifier of the token
     *
     * @return the UUID of the token
     */
    public UUID getId() {
        return id;
    }


    /**
     * Sets the unique identifier of the token
     *
     * @param id the UUID of the token
     */
    public void setId(UUID id) {
        this.id = id;
    }


    /**
     * Gets the user being invited
     *
     * @return the user linked to the token
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user being invited
     *
     * @param user the user to link to the token
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Set email address if recipient does not have an account.
     *
     * @param email Email address string
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get email address if recipient does not have an account.
     *
     * @return Email address string
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the renovation the user is invited to
     *
     * @return the renovation linked to the token
     */
    public Renovation getRenovation() {
        return renovation;
    }


    /**
     * Sets the renovation that the user will be invited to
     *
     * @param renovation the renovation that the user will be invited to
     */
    public void setRenovation(Renovation renovation) {
        this.renovation = renovation;
    }

    public void acceptInvitation() throws Exception {
        if (invitationStatus == InvitationStatus.PENDING) {
            this.invitationStatus = InvitationStatus.ACCEPTED;
        } else {
            throw new Exception("You cannot accept an already resolved invitation");
        }
    }


    /**
     * Changes the invitation state to declined
     */
    public void declineInvitation() throws Exception {
        if (invitationStatus == InvitationStatus.PENDING) {
            this.invitationStatus = InvitationStatus.DECLINED;
        } else {
            throw new Exception("You cannot decline an already resolved invitation");
        }
    }


    /**
     * Changes the invitation state to expired
     */
    public void expireInvitation() {
        this.invitationStatus = InvitationStatus.EXPIRED;
    }

    /**
     * Indicates if the invitation is accepted but requires the registration to be completed before marking the
     * invitation as accepted.
     *
     * @return true if the invitation is pending registration, false otherwise.
     */
    public boolean getAcceptedPendingRegistration() {
        return acceptedPendingRegistration;
    }

    /**
     * Updates the flag indicating if the invitation is accepted pending on completing the registration process.
     *
     * @param acceptedPendingRegistration true if the invitation is pending registration, false otherwise.
     */
    public void setAcceptedPendingRegistration(boolean acceptedPendingRegistration) {
        this.acceptedPendingRegistration = acceptedPendingRegistration;
    }

    /**
     * Gets the current status of an invitation
     *
     * @return invitation status
     */
    public InvitationStatus getInvitationStatus() {
        return invitationStatus;
    }

    /**
     * Sets the expiry datetime of an invitation
     *
     * @param expiryDate the wanted expiry datetime for the invitation
     */
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * Gets the expiry datetime of an invitation
     *
     * @return the expiry datetime for the invitation
     */
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the invitation status
     *
     * @param invitationStatus status to set the invitation to
     */
    public void setInvitationStatus(InvitationStatus invitationStatus) {
        this.invitationStatus = invitationStatus;
    }

    /**
     * @return True if invitation has been resolved (e.g Accepted, Rejected or Expired)
     */
    public boolean isResolved(){
        return invitationStatus != InvitationStatus.PENDING;
    }
}
