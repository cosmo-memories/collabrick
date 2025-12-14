package nz.ac.canterbury.seng302.homehelper.model.chat.ai;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a plain message response from the AI.
 * <pre>
 * {
 *     "type": "MESSAGE",
 *     "content": "The message to show to the user"
 * }
 * </pre>
 */
public class AiResponseMessage implements AiResponse {

    private final String content;

    @JsonCreator
    public AiResponseMessage(
            @JsonProperty("content") String content
    ) {
        this.content = content;
    }

    @Override
    public AiResponseType getType() {
        return AiResponseType.MESSAGE;
    }

    public String getContent() {
        return content;
    }
}
