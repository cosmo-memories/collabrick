package nz.ac.canterbury.seng302.homehelper.exceptions.chat;

/**
 * Thrown when a user sends an invalid chat message.
 */
public class ChatMessageException extends ChatException {

    /**
     * Constructs a new ChatMessageException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception.
     */
    public ChatMessageException(String message) {
        super(message);
    }
}
