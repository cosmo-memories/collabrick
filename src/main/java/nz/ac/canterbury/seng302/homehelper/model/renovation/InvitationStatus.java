package nz.ac.canterbury.seng302.homehelper.model.renovation;

/**
 * Enum to represent the different invitation statuses
 */
public enum InvitationStatus {
    PENDING("Pending"),

    ACCEPTED("Accepted"),

    DECLINED("Declined"),

    EXPIRED("Expired");

    private final String displayName;

    /**
     * Constructor for Invitation Status
     *
     * @param displayName the human-readable display name for the status
     */
    InvitationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
