package nz.ac.canterbury.seng302.homehelper.model.chat.ai;

/**
 * Enum representing the different types of AI responses.
 * Used for polymorphic deserialization and internal dispatching.
 */
public enum AiResponseType {

    /**
     * A plain message response
     */
    MESSAGE,

    /**
     * A structured task creation response
     */
    TASK_CREATION,

    /**
     * A prompt indicating that more chat context is required
     */
    REQUIRE_CHAT_CONTEXT,
}
