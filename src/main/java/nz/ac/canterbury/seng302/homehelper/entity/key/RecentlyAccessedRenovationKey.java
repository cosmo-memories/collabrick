package nz.ac.canterbury.seng302.homehelper.entity.key;

import jakarta.persistence.Embeddable;

@Embeddable
public class RecentlyAccessedRenovationKey {
    private long renovationId;
    private long userId;

    /**
     * JPA constructor
     */
    protected RecentlyAccessedRenovationKey() {

    }

    /**
     * Constructs a key with the given renovation ID and user ID.
     *
     * @param renovationId The ID of the renovation.
     * @param userId       The ID of the user.
     */
    public RecentlyAccessedRenovationKey(long renovationId, long userId) {
        this.renovationId = renovationId;
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public long getRenovationId() {
        return renovationId;
    }
}
