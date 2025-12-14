package nz.ac.canterbury.seng302.homehelper.entity.renovation;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.key.RecentlyAccessedRenovationKey;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity class representing the table for a user's recently accessed renovations
 */
@Entity
public class RecentlyAccessedRenovation {

    @EmbeddedId
    private RecentlyAccessedRenovationKey id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @ManyToOne
    @MapsId("renovationId")
    @JoinColumn(name="renovation_id", nullable=false)
    private Renovation renovation;

    private LocalDateTime timeAccessed;


    /**
     * JPA constructor
     */
    protected RecentlyAccessedRenovation() {

    }

    /**
     * Creates a new RecentlyAccessedRenovation object
     *
     * @param user       The user that accessed the renovation
     * @param renovation The renovation that the user accessed
     */
    public RecentlyAccessedRenovation(User user, Renovation renovation) {
        this.id = new RecentlyAccessedRenovationKey(user.getId(), renovation.getId());
        this.user = user;
        this.renovation = renovation;
        this.timeAccessed = LocalDateTime.now();
    }

    /**
     * Updates the time for a RecentlyAccessedRenovation to now
     */
    public void updateTimeAccessed() {
        this.timeAccessed = LocalDateTime.now();
    }

    public LocalDateTime getTimeAccessed() {
        return timeAccessed;
    }

    public void setTimeAccessed(LocalDateTime time) {
        this.timeAccessed = time;
    }

    public Renovation getRenovation() {
        return renovation;
    }

    public String getRelativeTime() {
        Duration duration = Duration.between(getTimeAccessed(), LocalDateTime.now());
        if (duration.toMinutes() < 60) {
            return duration.toMinutes() + " minutes ago";
        } else if (duration.toHours() < 24) {
            return duration.toHours() + " hours ago";
        } else {
            return duration.toDays() + " days ago";
        }
    }
}
