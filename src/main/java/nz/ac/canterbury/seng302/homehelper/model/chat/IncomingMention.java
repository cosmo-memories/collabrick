package nz.ac.canterbury.seng302.homehelper.model.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Represents a mention of a user within an incoming chat message.
 *
 * @param userId        the ID of the user that was mentioned
 * @param startPosition the starting index of the mention in the message text which should be the '@' character
 * @param endPosition   the end index of the mention in the message text
 */
public record IncomingMention(
        @JsonProperty("userId") long userId,
        @JsonProperty("startPosition") int startPosition,
        @JsonProperty("endPosition") int endPosition
) {

    @JsonCreator
    public IncomingMention {
    }
}
