package nz.ac.canterbury.seng302.homehelper.exceptions.chat;

/**
 * Thrown when the inputted details for a chat channel are invalid.
 */
public class ChatChannelDetailsException extends ChatException {

    /**
     * Constructs a new ChatChannelDetailsException with the specified detail message.
     *
     * @param message The detail message.
     */
    public ChatChannelDetailsException(String message) {
        super(message);
    }
}
