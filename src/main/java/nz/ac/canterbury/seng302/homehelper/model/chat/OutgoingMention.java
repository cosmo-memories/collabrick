package nz.ac.canterbury.seng302.homehelper.model.chat;

import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationDto;
import nz.ac.canterbury.seng302.homehelper.model.user.PublicUserDetails;

import java.time.Instant;

/**
 * Outgoing mention dto to send relevant mention details to the client with the required information
 *
 * @param renovationDetails renovation name and id
 * @param channelId channel id where the chat with the mention was sent
 * @param channelName name of the chat channel where the chat with the mention was sent
 * @param sender user details of the person who sent the chat with the mention
 * @param messageContent string containing the message content
 * @param timestamp time that the message was sent
 */
public record OutgoingMention(RenovationDto renovationDetails, long channelId, String channelName, PublicUserDetails sender, String messageContent, Instant timestamp) {

    public long renovationId() {
        return renovationDetails.id();
    }
    public String renovationName() {
        return renovationDetails.name();
    }
}
