package nz.ac.canterbury.seng302.homehelper.exceptions.renovation;

/**
 * Exception thrown when handling and saving tags to renovations
 */
public class TagException extends Exception {

    private final String message;

    /**
     * Constructor
     *
     * @param message Error message
     */
    public TagException(String message) {
        this.message = message;
    }

    /**
     * Get error message
     *
     * @return Error message
     */
    public String getMessage() {
        return message;
    }

}
