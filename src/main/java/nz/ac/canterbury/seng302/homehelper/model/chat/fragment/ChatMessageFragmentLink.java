package nz.ac.canterbury.seng302.homehelper.model.chat.fragment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatMessageFragmentLink implements ChatMessageFragment {

    private final String link;
    private final String text;

    @JsonCreator
    public ChatMessageFragmentLink(@JsonProperty("link") String link, @JsonProperty("text") String text) {
        this.link = link;
        this.text = text;
    }

    @Override
    public ChatMessageFragmentType getType() {
        return ChatMessageFragmentType.LINK;
    }

    @Override
    public String getText() {
        return text;
    }
    public String getLink() {
        return link;
    }
}
