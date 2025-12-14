package nz.ac.canterbury.seng302.homehelper.exceptions.token;

/**
 * Exception thrown when a token has expired.
 */
public class TokenExpiredException extends RuntimeException {
    /**
     * Constructs a new TokenExpiredException with the specified detail message.
     *
     * @param message the detail message
     */
    public TokenExpiredException(String message) {
        super(message);
    }
}
