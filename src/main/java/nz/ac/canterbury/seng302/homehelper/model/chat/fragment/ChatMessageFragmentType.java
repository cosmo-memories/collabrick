package nz.ac.canterbury.seng302.homehelper.model.chat.fragment;

/**
 * Defines the types of fragments that can appear in a chat message.
 */
public enum ChatMessageFragmentType {
    /**
     * Represents plain message content.
     **/
    TEXT,

    /**
     * Represents a user mention.
     */
    MENTION,
    /**
     * Represents a clickable link
     */
    LINK
}
