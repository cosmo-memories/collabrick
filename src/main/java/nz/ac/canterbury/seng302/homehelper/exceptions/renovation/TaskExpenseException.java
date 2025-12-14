package nz.ac.canterbury.seng302.homehelper.exceptions.renovation;

/**
 * Exception thrown when there are validation errors in a task expense form.
 * Contains specific error messages for the name, expense amount, and date fields.
 */
public class TaskExpenseException extends RuntimeException {

    /**
     * Error message related to the expense name field.
     */
    private final String nameErrorMessage;

    /**
     * Error message related to the expense amount field.
     */
    private final String expenseErrorMessage;

    /**
     * Error message related to the expense date field.
     */
    private final String dateErrorMessage;

    /**
     * Constructs a new TaskExpenseException with specific error messages.
     *
     * @param nameErrorMessage    error message for the name field
     * @param expenseErrorMessage error message for the expense amount field
     * @param dateErrorMessage    error message for the date field
     */
    public TaskExpenseException(String nameErrorMessage, String expenseErrorMessage, String dateErrorMessage) {
        this.nameErrorMessage = nameErrorMessage;
        this.expenseErrorMessage = expenseErrorMessage;
        this.dateErrorMessage = dateErrorMessage;
    }

    /**
     * Gets the error message related to the expense name.
     *
     * @return the name error message
     */
    public String getNameErrorMessage() {
        return nameErrorMessage;
    }

    /**
     * Gets the error message related to the expense amount.
     *
     * @return the expense amount error message
     */
    public String getExpenseErrorMessage() {
        return expenseErrorMessage;
    }

    /**
     * Gets the error message related to the expense date.
     *
     * @return the date error message
     */
    public String getDateErrorMessage() {
        return dateErrorMessage;
    }
}
