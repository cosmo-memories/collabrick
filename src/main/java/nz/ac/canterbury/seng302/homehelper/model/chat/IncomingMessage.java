package nz.ac.canterbury.seng302.homehelper.model.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a message that is received from a client in a chat channel.
 */
public record IncomingMessage(String content, Long channelId, Long renovationId, List<IncomingMention> mentions) {

     /**
     * Constructs an IncomingMessage with the specified content and a list of mentions.
     *
     * @param content The content of the incoming message.
     * @param channelId the id of the channel
     * @param renovationId the id of the renovation
     * @param mentions the list of mentions inside the chat message
     */
    @JsonCreator
    public IncomingMessage(
            @JsonProperty("content") String content,
            @JsonProperty("channelId") Long channelId,
            @JsonProperty("renovationId") Long renovationId,
            @JsonProperty("mentions") List<IncomingMention> mentions
    ) {
        this.content = content;
        this.channelId = channelId;
        this.renovationId = renovationId;
        this.mentions = mentions;
    }

    /**
     * Constructs an IncomingMessage with the specified content.
     *
     * @param content The content of the incoming message.
     * @param channelId the id of the channel
     * @param renovationId the id of the renovation
     */
    public IncomingMessage(String content, Long channelId, Long renovationId) {
        this(content, channelId, renovationId, List.of());
    }
}
