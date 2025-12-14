package nz.ac.canterbury.seng302.homehelper.entity.user;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity class for a user's verification token
 * Note the @link{Entity} annotation required for declaring this as a persistence entity
 * Is linked to a user in a OneToOne relationship
 */
@Entity
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public VerificationToken() {
        // JPA constructor
    }

    /**
     * General setter for a verification token
     *
     * @param token      the token to set
     * @param user       the user to set the token for
     * @param expiryDate the expiry date for the token
     */
    public VerificationToken(String token, User user, LocalDateTime expiryDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
    }

    /**
     * Gets the verification token id
     *
     * @return the verification token id
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the id of the verification token
     *
     * @param id the id to set for the verification token
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the token string
     *
     * @return the string of the token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the token string for the verification token
     *
     * @param token the string to set the token to
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Gets the user this verification token is associated with
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user this verification token is for
     *
     * @param user the user the verification token is for
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets the expiry date of the token
     *
     * @return the expiry date of the token
     */
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the expiry date for the token
     *
     * @param expiryDate the expiry date to set the token to
     */
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * Checks if the token is expired
     *
     * @return a boolean value of if the expiry date is before the current time
     */
    public boolean isTokenExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
