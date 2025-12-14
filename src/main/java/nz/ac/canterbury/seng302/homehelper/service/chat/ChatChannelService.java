package nz.ac.canterbury.seng302.homehelper.service.chat;

import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatChannelDetailsException;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatChannelNotFoundException;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatUserAlreadyMemberException;
import nz.ac.canterbury.seng302.homehelper.exceptions.user.UserNotFoundException;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatChannelRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BrickAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static nz.ac.canterbury.seng302.homehelper.validation.chat.ChatChannelValidation.validateChannelName;


/**
 * Service responsible for managing renovation chat channels.
 */
@Service
public class ChatChannelService {

    private final ChatChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final BrickAiService brickAiService;

    /**
     * Constructs the ChatChannelService with the required repositories for managing channels.
     *
     * @param channelRepository Repository for chat channels
     * @param userRepository    Repository for users
     */
    @Autowired
    public ChatChannelService(ChatChannelRepository channelRepository, UserRepository userRepository, BrickAiService brickAiService) {
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
        this.brickAiService = brickAiService;
    }

    /**
     * Retrieves a chat channel by ID.
     *
     * @param channelId the unique ID of the chat channel to retrieve
     * @return an Optional containing the ChatChannel if found,
     * or an empty Optional if no channel exists with the given ID
     */
    public Optional<ChatChannel> findById(long channelId) {
        return channelRepository.findById(channelId);
    }

    /**
     * Checks if user is a member of the channel
     *
     * @param user      User to check for
     * @param channelId Channel in question
     * @return True is user is member of channel
     */
    public boolean isUserMemberOfChannel(User user, long channelId) {
        return channelRepository.findById(channelId)
                .filter(channel -> channel.getMembers().contains(user))
                .isPresent();
    }

    /**
     * Creates a new chat channel associated with a renovation and a name. Adds the renovation owner as a member to
     * the chat channel.
     *
     * @param renovation the renovation to associate with the channel
     * @param name       the name of the chat channel
     * @return the saved ChatChannel instance
     */
    public ChatChannel createChannel(Renovation renovation, String name) {
        validateChannel(name, renovation);
        ChatChannel chatChannel = new ChatChannel(name, renovation);
        try {
            User brickAi = brickAiService.getAiUser();
            addMemberToChatChannel(chatChannel, brickAi);
        } catch (IllegalStateException ignored) {
        }

        return addMemberToChatChannel(chatChannel, renovation.getOwner());
    }

    /**
     * Find a channel by the name of the channel and the renovation it belongs to
     *
     * @param renovation The renovation the channel belongs to
     * @param name       The name of the channel we are searching for
     * @return A ChatChannel from the renovation and with the name specified if found
     */
    public Optional<ChatChannel> findByRenovationAndName(Renovation renovation, String name) {
        return channelRepository.findByNameAndRenovation(name, renovation);
    }

    /**
     * Validates a channel to be created
     *
     * @param channelName The name of the channel to add
     * @param renovation  The renovation to add the channel to
     * @throws ChatChannelDetailsException The error to be thrown if the name is invalid
     */
    private void validateChannel(String channelName, Renovation renovation) throws ChatChannelDetailsException {
        String channelNameError = validateChannelName(channelName, channelRepository, renovation);
        if (!channelNameError.isEmpty()) {
            throw new ChatChannelDetailsException(channelNameError);
        }
    }

    /**
     * Adds a user to a chat channel's member list.
     *
     * @param channelId the chat channel's ID
     * @param userId    the user's ID to add
     * @return the updated ChatChannel instance
     * @throws UserNotFoundException          if the user is not found
     * @throws ChatChannelNotFoundException   if the channel is not found
     * @throws ChatUserAlreadyMemberException if the user is already a member
     */
    public ChatChannel addMemberToChatChannel(long channelId, long userId) {
        User user = userRepository.findUserById(userId)
                .stream()
                .findFirst()
                .orElseThrow(UserNotFoundException::new);
        ChatChannel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChatChannelNotFoundException("Channel with ID " + channelId + " not found"));
        return addMemberToChatChannel(channel, user);
    }

    /**
     * Gets the chat channels based on a renovation and logged-in user
     *
     * @param user       the logged-in user
     * @param renovation the relevant renovation
     * @return a list of chat channels matching the user and renovation
     */
    public List<ChatChannel> getChannelByRenovationAndUser(User user, Renovation renovation) {
        List<ChatChannel> channels = channelRepository.findByUserAndRenovation(user, renovation);

        return channels.stream()
                .sorted(Comparator
                        .comparing((ChatChannel channel) -> !channel.getName().equals("brickAI"))
                        .thenComparing(ChatChannel::getId))
                .collect(Collectors.toList());
    }

    /**
     * Find a chat channel by id
     *
     * @param channelId id of the channel
     * @return A chat channel with the provided id
     */
    public Optional<ChatChannel> getChannelById(long channelId) {
        return channelRepository.findById(channelId);
    }


    /**
     * Adds a user to a chat channel's member list.
     *
     * @param channel the chat channel
     * @param user    the user to add
     * @return the updated ChatChannel instance
     * @throws ChatUserAlreadyMemberException if the user is already a member
     */
    private ChatChannel addMemberToChatChannel(ChatChannel channel, User user) {
        if (Objects.equals(channel.getName(), "brickAI")) {
            throw new ChatUserAlreadyMemberException("User with ID " + user.getId() + " is already a member of an AI chat channel for renovation ID " + channel.getRenovation().getId());
        }
        if (channel.getMembers().contains(user)) {
            throw new ChatUserAlreadyMemberException("User with ID " + user.getId() + " is already member of channel with ID " + channel.getId());
        }
        channel.getMembers().add(user);
        return channelRepository.save(channel);
    }

    /**
     * Removes a member from a chat channel
     *
     * @param channel channel to remove a user from
     * @param user user to remove a member from channel
     */
    public void removeMemberFromChatChannel(ChatChannel channel, User user) {
        channel.getMembers().remove(user);
        channelRepository.save(channel);
    }

}
