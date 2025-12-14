package nz.ac.canterbury.seng302.homehelper.exceptions.renovation;

public class TaskDetailsExceptions extends RuntimeException {
    private final String nameErrorMessage;
    private final String descriptionErrorMessage;
    private final String dueDateErrorMessage;

    public TaskDetailsExceptions(String nameErrorMessage, String descriptionErrorMessage, String dueDateErrorMessage) {
        this.nameErrorMessage = nameErrorMessage;
        this.descriptionErrorMessage = descriptionErrorMessage;
        this.dueDateErrorMessage = dueDateErrorMessage;
    }

    /**
     * Gets the name of the error message
     *
     * @return the name of the error message
     */
    public String getNameErrorMessage() {
        return nameErrorMessage;
    }

    /**
     * Gets the description of the error message
     *
     * @return the description of the error message
     */
    public String getDescriptionErrorMessage() {
        return descriptionErrorMessage;
    }

    /**
     * Gets the due date error message
     *
     * @return the due date error message
     */
    public String getDueDateErrorMessage() {
        return dueDateErrorMessage;
    }
}
