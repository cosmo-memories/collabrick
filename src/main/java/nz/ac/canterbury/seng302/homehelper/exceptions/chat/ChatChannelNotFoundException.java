package nz.ac.canterbury.seng302.homehelper.exceptions.chat;

/**
 * Thrown when a requested chat channel cannot be found.
 */
public class ChatChannelNotFoundException extends ChatException {

    /**
     * Constructs a new ChatChannelNotFoundException with the specified detail message.
     *
     * @param message The detail message.
     */
    public ChatChannelNotFoundException(String message) {
        super(message);
    }
}
