package nz.ac.canterbury.seng302.homehelper.model.renovation;

/**
 * Represents the category of an expense that can be assigned to a task.
 * Used to classify and organize different types of costs in a renovation.
 */
public enum ExpenseCategory {

    /**
     * General expenses that don't fit into a specific category.
     */
    MISCELLANEOUS("Miscellaneous"),

    /**
     * Costs related to materials used in the task.
     */
    MATERIAL("Material"),

    /**
     * Costs for labor or human effort.
     */
    LABOUR("Labour"),

    /**
     * Costs for renting or purchasing equipment.
     */
    EQUIPMENT("Equipment"),

    /**
     * Costs for professional or contractor services.
     */
    PROFESSIONAL_SERVICES("Professional Services"),

    /**
     * Fees associated with obtaining permits.
     */
    PERMIT("Permit"),

    /**
     * Costs for cleaning up after a task is completed.
     */
    CLEANUP("Cleanup"),

    /**
     * Expenses related to delivery of goods or services.
     */
    DELIVERY("Delivery");

    /**
     * The human-readable display name of the category.
     */
    private final String displayName;

    /**
     * Constructor for ExpenseCategory.
     *
     * @param displayName the human-readable display name for the category
     */
    ExpenseCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name for the category.
     *
     * @return the display name of the category
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the ExpenseCategory enum constant that matches the given display name.
     * Returns MISCELLANEOUS by default if no match is found.
     *
     * @param displayName the display name to match
     * @return the matching ExpenseCategory, or MISCELLANEOUS if none matches
     */
    public static ExpenseCategory fromDisplayName(String displayName) {
        for (ExpenseCategory category : ExpenseCategory.values()) {
            if (category.getDisplayName().equals(displayName)) {
                return category;
            }
        }
        return MISCELLANEOUS;
    }
}
