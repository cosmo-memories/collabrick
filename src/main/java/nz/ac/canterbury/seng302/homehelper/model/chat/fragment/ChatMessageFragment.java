package nz.ac.canterbury.seng302.homehelper.model.chat.fragment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Represents a fragment of a chat message.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChatMessageFragmentText.class, name = "TEXT"),
        @JsonSubTypes.Type(value = ChatMessageFragmentMention.class, name = "MENTION"),
        @JsonSubTypes.Type(value = ChatMessageFragmentLink.class, name="LINK")
})
public interface ChatMessageFragment {

    /**
     * Returns the type of this fragment.
     *
     * @return the fragment type
     */
    @JsonIgnore
    ChatMessageFragmentType getType();

    /**
     * Returns the text of the fragment to display.
     *
     * @return the text to display
     */
    String getText();
}
