package nz.ac.canterbury.seng302.homehelper.model.renovation;

/**
 * Represents the role of a member within a renovation project.
 */
public enum RenovationMemberRole {

    /**
     * The owner of the renovation project.
     */
    OWNER("Owner"),

    /**
     * A standard member of the renovation project.
     */
    MEMBER("Member");

    private final String displayName;

    RenovationMemberRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
