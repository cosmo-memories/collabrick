package nz.ac.canterbury.seng302.homehelper.exceptions.chat;

/**
 * Thrown when a user attempts to access a chat channel without the required permissions.
 */
public class ChatChannelUnauthorisedException extends ChatException {

    /**
     * Constructs a new ChatChannelUnauthorisedException with the specified detail message.
     *
     * @param message The detail message.
     */
    public ChatChannelUnauthorisedException(String message) {
        super(message);
    }
}
