package nz.ac.canterbury.seng302.homehelper.entity.user;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a forgotten password token for resetting user passwords. Each token is associated with a specific
 * user and has an expiry date.
 */
@Entity
public class ForgottenPasswordToken {

    /**
     * Unique identifier for the forgotten password token, automatically generated as a UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The user associated with this forgotten password token.
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The expiry date and time of the forgotten password token.
     */
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    /**
     * Default constructor required by JPA.
     */
    public ForgottenPasswordToken() {

    }

    /**
     * Constructs a ForgottenPasswordToken with a specified user and expiry date.
     *
     * @param user       The user associated with the token.
     * @param expiryDate The date and time when the token expires.
     */
    public ForgottenPasswordToken(User user, LocalDateTime expiryDate) {
        this.user = user;
        this.expiryDate = expiryDate;
    }

    /**
     * Gets the unique identifier of the token.
     *
     * @return The UUID of the token.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the token.
     *
     * @param id The UUID to set.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Gets the user associated with the token.
     *
     * @return The user linked to the token.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user associated with the token.
     *
     * @param user The user to associate with this token.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets the expiry date of the token.
     *
     * @return The expiry date and time.
     */
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the expiry date of the token.
     *
     * @param expiryDate The expiry date and time to set.
     */
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
}
