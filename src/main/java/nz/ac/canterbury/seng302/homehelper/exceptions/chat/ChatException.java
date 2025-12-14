package nz.ac.canterbury.seng302.homehelper.exceptions.chat;

/**
 * Base exception for all chat-related errors.
 */
public class ChatException extends RuntimeException {

    /**
     * Constructs a new ChatException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception.
     */
    public ChatException(String message) {
        super(message);
    }
}
