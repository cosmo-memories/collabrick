package nz.ac.canterbury.seng302.homehelper.exceptions.renovation;

/**
 * Exception for errors caused by unauthorized actions
 */
public class InvalidPermissionsException extends RuntimeException {
    public InvalidPermissionsException(String message) {
        super(message);
    }
}
