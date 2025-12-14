package nz.ac.canterbury.seng302.homehelper.model.chat.ai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Marker interface for all AI-generated responses.
 * Implementations are serialized using Jackson polymorphism with the type field
 * determining the concrete response class.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AiResponseMessage.class, name = "MESSAGE"),
        @JsonSubTypes.Type(value = AiResponseRequireChatContext.class, name = "REQUIRE_CHAT_CONTEXT"),
        @JsonSubTypes.Type(value = AiResponseTaskCreation.class, name = "TASK_CREATION")
})
public interface AiResponse {

    /**
     * Returns the type of this AI response.
     * This is ignored during JSON serialization to avoid redundancy with the polymorphic {@code type} field.
     *
     * @return the response type
     */
    @JsonIgnore
    AiResponseType getType();
}



