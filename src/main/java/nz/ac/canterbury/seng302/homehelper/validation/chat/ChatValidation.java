package nz.ac.canterbury.seng302.homehelper.validation.chat;

import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatMessageException;
import nz.ac.canterbury.seng302.homehelper.utility.StringUtils;

/**
 * Utility class for validating chat messages.
 */
public class ChatValidation {

    public static final int CHAT_MESSAGE_MAX_LENGTH = 2048;
    public static final String CHAT_MESSAGE_TOO_LONG = "Message must be " + CHAT_MESSAGE_MAX_LENGTH + " characters or less";
    public static final String CHAT_MESSAGE_BLANK = "Message cannot be blank";

    /**
     * Validates a chat message according to their length. Takes into a count emojis which are grapheme clusters.
     *
     * @param message the chat message to validate
     * @throws ChatMessageException if the message is blank or exceeds the allowed length
     */
    public static void validateChatMessage(String message) {
        message = message.trim();
        if (message.isEmpty()) {
            throw new ChatMessageException(CHAT_MESSAGE_BLANK);
        }

        // takes into a count emojis or characters that are two or more bytes
        int length = StringUtils.countGraphemeClusters(message);
        if (length > CHAT_MESSAGE_MAX_LENGTH) {
            throw new ChatMessageException(CHAT_MESSAGE_TOO_LONG);
        }
    }
}
