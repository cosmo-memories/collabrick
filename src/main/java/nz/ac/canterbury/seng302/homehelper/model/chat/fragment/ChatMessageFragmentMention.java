package nz.ac.canterbury.seng302.homehelper.model.chat.fragment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A chat message fragment that represents a user mention.
 * Used to easily render a mention in a chat message as "@username" in the chat UI.
 */
public class ChatMessageFragmentMention implements ChatMessageFragment {

    private final long mentionedUserId;
    private final String mentionedUserName;

    /**
     * Creates a mention fragment for the given user.
     *
     * @param mentionedUserId   the ID of the mentioned user
     * @param mentionedUserName the full name of the mentioned user
     */
    @JsonCreator
    public ChatMessageFragmentMention(
            @JsonProperty("mentionedUserId") long mentionedUserId,
            @JsonProperty("mentionedUserName") String mentionedUserName
    ) {
        this.mentionedUserId = mentionedUserId;
        this.mentionedUserName = mentionedUserName;
    }

    /**
     * Returns the type of this fragment, which is MENTION.
     *
     * @return the fragment type
     */
    @Override
    public ChatMessageFragmentType getType() {
        return ChatMessageFragmentType.MENTION;
    }

    public long getMentionedUserId() {
        return mentionedUserId;
    }

    public String getMentionedUserName() {
        return mentionedUserName;
    }

    @Override
    public String getText() {
        return "@" + mentionedUserName;
    }
}
