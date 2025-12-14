package nz.ac.canterbury.seng302.homehelper.model.chat.fragment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A chat message fragment that represents plain text.
 * This fragment stores a portion of the message that is not a mention.
 */
public class ChatMessageFragmentText implements ChatMessageFragment {

    private final String text;

    /**
     * Creates a text fragment with the given content.
     *
     * @param text the text content of the fragment
     */
    @JsonCreator
    public ChatMessageFragmentText(@JsonProperty("text") String text) {
        this.text = text;
    }

    /**
     * Returns the type of this fragment, which is TEXT.
     *
     * @return the fragment type
     */
    @Override
    public ChatMessageFragmentType getType() {
        return ChatMessageFragmentType.TEXT;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}
