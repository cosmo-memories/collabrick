package nz.ac.canterbury.seng302.homehelper.validation.chat;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatChannelRepository;

import static nz.ac.canterbury.seng302.homehelper.validation.Validation.isNameValid;

/**
 * A utility class for validating channel names.
 */
public class ChatChannelValidation {
    public static final String CHANNEL_NAME_EMPTY_MESSAGE = "Channel name cannot be empty";
    public static final String INVALID_CHANNEL_NAME_MESSAGE = "Channel name must only include letters, numbers, spaces, " +
            "dots, hyphens, or apostrophes, and must contain at least one letter or number";
    public static final String CHANNEL_NAME_TOO_LONG_MESSAGE = "Channel name must be 64 characters or less";
    public static final String CHANNEL_NAME_ALREADY_EXISTS_MESSAGE = "A channel with this name already exists";
    public static final int CHANNEL_NAME_MAX_LENGTH = 64;
    public static final String CHANNEL_NAME_BRICK_AI = "Channel name cannot be \"brickAI\"";

    /**
     * Returns an error message if the channel name is not valid.
     *
     * @param channelName           name of the channel to be validated
     * @param chatChannelRepository the repository for chat channels
     * @return empty string if valid name, otherwise the relevant error message
     */
    public static String validateChannelName(String channelName, ChatChannelRepository chatChannelRepository, Renovation renovation) {
        if (channelName.trim().isEmpty()) {
            return CHANNEL_NAME_EMPTY_MESSAGE;
        }

        if (!isNameValid(channelName)) {
            return INVALID_CHANNEL_NAME_MESSAGE;
        }

        if (channelName.length() > CHANNEL_NAME_MAX_LENGTH) {
            return CHANNEL_NAME_TOO_LONG_MESSAGE;
        }

        if (chatChannelRepository.findByNameAndRenovation(channelName, renovation).isPresent()) {
            return CHANNEL_NAME_ALREADY_EXISTS_MESSAGE;
        }

        if (channelName.equals("brickAI")) {
            return CHANNEL_NAME_BRICK_AI;
        }

        return "";
    }


}
