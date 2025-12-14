package nz.ac.canterbury.seng302.homehelper.model.renovation;

import java.util.Optional;

/**
 * Enumeration representing the different states a task can be in.
 */
public enum TaskState {
    /**
     * The task has not been started yet.
     */
    NOT_STARTED("Not Started", "bg-secondary"),

    /**
     * The task is currently in progress.
     */
    IN_PROGRESS("In Progress", "bg-primary"),

    /**
     * The task is blocked and cannot proceed at the moment.
     */
    BLOCKED("Blocked", "bg-warning"),

    /**
     * The task has been completed successfully.
     */
    COMPLETED("Completed", "bg-success"),

    /**
     * The task has been cancelled and will not be completed.
     */
    CANCELLED("Cancelled", "bg-danger");

    private final String displayName;
    private final String badgeClassName;

    /**
     * Constructs a TaskState with the given display name and bootstrap badge class name.
     *
     * @param displayName    The readable name of the task state.
     * @param badgeClassName The CSS class name for styling this state.
     */
    TaskState(String displayName, String badgeClassName) {
        this.displayName = displayName;
        this.badgeClassName = badgeClassName;
    }

    /**
     * Gets the display of the task state.
     *
     * @return The display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the bootstrap badge class name associated with the task state.
     *
     * @return The badge class name.
     */
    public String getBadgeClassName() {
        return badgeClassName;
    }

    /**
     * Converts a string to a corresponding TaskState enum value.
     * The comparison is case-insensitive.
     *
     * @param state the string representation of the state
     * @return an Optional containing the matching TaskState if valid, otherwise Optional#empty()
     */
    public static Optional<TaskState> fromString(String state) {
        try {
            return Optional.of(TaskState.valueOf(state.toUpperCase().replace(" ", "_")));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
