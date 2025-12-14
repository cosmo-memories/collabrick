package nz.ac.canterbury.seng302.homehelper.exceptions.token;

/**
 * Exception thrown when a provided token is invalid.
 */
public class TokenInvalidException extends RuntimeException {
    /**
     * Constructs a new TokenInvalidException with the specified detail message.
     *
     * @param message the detail message
     */
    public TokenInvalidException(String message) {
        super(message);
    }
}
