package nz.ac.canterbury.seng302.homehelper.model.activity;

/**
 * Enum representing different types of activity feed updates.
 */
public enum ActivityType {

    // Budget has been edited
    BUDGET_EDITED("Budget Edited"),

    // Expense has been added
    EXPENSE_ADDED("Expense Added"),

    // Invite has been accepted or declined
    INVITE_ACCEPTED("Invitation Accepted"),
    INVITE_DECLINED("Invitation Declined"),

    // Task has been added or edited
    TASK_ADDED("Task Added"),
    TASK_EDITED("Task Edited"),

    // Task has been changed from the given state (new state can be retrieved from Task object itself)
    TASK_CHANGED_FROM_NOT_STARTED("Task State Changed From Not Started"),
    TASK_CHANGED_FROM_IN_PROGRESS("Task State Changed From In Progress"),
    TASK_CHANGED_FROM_BLOCKED("Task State Changed From Blocked"),
    TASK_CHANGED_FROM_COMPLETED("Task State Changed From Completed"),
    TASK_CHANGED_FROM_CANCELLED("Task State Changed From Cancelled");

    private final String displayName;

    /**
     * Constructor
     */
    ActivityType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get display name for activity type.
     * @return      Display name as string
     */
    public String getDisplayName() {
        return displayName;
    }

}
