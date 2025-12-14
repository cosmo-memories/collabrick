package nz.ac.canterbury.seng302.homehelper.entity.key;

import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * Embeddable composite key for RenovationMembership.
 * Combines renovationId and userId as a composite primary key.
 */
@Embeddable
public class RenovationMemberKey {
    private long renovationId;
    private long userId;

    /**
     * Default constructor required by JPA.
     */
    public RenovationMemberKey() {

    }

    /**
     * Constructs a key with the given renovation ID and user ID.
     *
     * @param renovationId The ID of the renovation.
     * @param userId       The ID of the user.
     */
    public RenovationMemberKey(long renovationId, long userId) {
        this.renovationId = renovationId;
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public long getRenovationId() {
        return renovationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RenovationMemberKey key)) return false;
        return Objects.equals(renovationId, key.getRenovationId()) &&
                Objects.equals(userId, key.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(renovationId, userId);
    }
}
