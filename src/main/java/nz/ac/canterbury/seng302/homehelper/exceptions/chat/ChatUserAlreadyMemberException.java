package nz.ac.canterbury.seng302.homehelper.exceptions.chat;

/**
 * Thrown when an attempt is made to add a user to a chat channel they are already a member of.
 */
public class ChatUserAlreadyMemberException extends ChatException {

    /**
     * Constructs a new {@code ChatUserAlreadyMemberException} with the specified detail message.
     *
     * @param message The detail message.
     */
    public ChatUserAlreadyMemberException(String message) {
        super(message);
    }
}
