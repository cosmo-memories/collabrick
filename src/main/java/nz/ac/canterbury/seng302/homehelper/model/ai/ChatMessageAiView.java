package nz.ac.canterbury.seng302.homehelper.model.ai;

import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMessage;
import nz.ac.canterbury.seng302.homehelper.utility.DateUtils;

/**
 * A lightweight DTO representing a chat message.
 *
 * @param sender The name of the user who sent the message.
 * @param content The content of the message.
 * @param timestamp The timestamp the message was sent.
 */
public record ChatMessageAiView(
        String sender,
        String content,
        String timestamp
) {
    public ChatMessageAiView(ChatMessage chatMessage) {
        this(
                chatMessage.getSender().getFullName(),
                chatMessage.getContent(),
                DateUtils.formatDateForAi(chatMessage.getTimestamp())
        );
    }
}
