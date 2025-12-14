package nz.ac.canterbury.seng302.homehelper.model.chat.ai;

/**
 * Represents a response indicating that the AI requires more context
 * before proceeding with generating a message.
 * <pre>
 * {
 *     "type": "REQUIRE_CHAT_CONTEXT",
 * }
 * </pre>
 */
public class AiResponseRequireChatContext implements AiResponse {

    @Override
    public AiResponseType getType() {
        return AiResponseType.REQUIRE_CHAT_CONTEXT;
    }
}
