package nz.ac.canterbury.seng302.homehelper.model.chat;

import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMessage;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragment;
import nz.ac.canterbury.seng302.homehelper.model.user.ChatUserDetails;

import java.time.Instant;
import java.util.List;

/**
 * Represents a message that is being sent from the server to a client in a chat channel.
 */
public record OutgoingMessage(long id, List<ChatMessageFragment> fragments, Instant date, ChatUserDetails user, boolean ai) {

    public OutgoingMessage(ChatMessage chatMessage, List<ChatMessageFragment> fragments, boolean ai) {
        this(chatMessage.getId(),
                fragments,
                chatMessage.getTimestamp(),
                new ChatUserDetails(chatMessage.getSender()),
                ai);
    }
}
