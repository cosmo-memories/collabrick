package nz.ac.canterbury.seng302.homehelper.service.chat;

import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMention;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMessage;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatChannelNotFoundException;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatChannelUnauthorisedException;
import nz.ac.canterbury.seng302.homehelper.exceptions.user.UserNotFoundException;
import nz.ac.canterbury.seng302.homehelper.model.chat.IncomingMention;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatChannelRepository;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatMessageRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.validation.chat.ChatValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for managing renovation chat messages.
 */
@Service
public class ChatMessageService {

    private static final int CHAT_MESSAGE_BATCH_SIZE = 25;
    private final ChatChannelRepository channelRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Constructs the ChatMessageService with the required repositories for managing messages.
     *
     * @param channelRepository Repository for chat channels
     * @param messageRepository Repository for chat messages
     * @param userRepository    Repository for users
     */
    @Autowired
    public ChatMessageService(ChatChannelRepository channelRepository, ChatMessageRepository messageRepository, UserRepository userRepository, UserService userService) {
        this.channelRepository = channelRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * Persists a chat message in a given channel from a specified sender, if sender is authorized.
     *
     * @param channelId the ID of the chat channel
     * @param senderId  the ID of the user sending the message
     * @param content   the message content
     * @return the saved ChatMessage object
     * @throws ChatChannelUnauthorisedException if the user is not part of the channel
     * @throws ChatChannelNotFoundException     if the channel was not found.
     * @throws UserNotFoundException            if the user was not found.
     */
    @Transactional
    public ChatMessage saveMessage(long channelId, long senderId, String content, List<IncomingMention> mentions) {
        ChatValidation.validateChatMessage(content);
        if (!channelRepository.isUserMemberOfChannel(channelId, senderId)) {
            throw new ChatChannelUnauthorisedException("User with ID " + senderId + " is not authorized to send messages in channel with ID channel " + channelId);
        }

        ChatChannel chatChannel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChatChannelNotFoundException("Channel with ID " + channelId + " not found"));
        User sender = userRepository.findUserById(senderId).stream()
                .findFirst()
                .orElseThrow(UserNotFoundException::new);
        ChatMessage chatMessage = new ChatMessage(content, Instant.now(), chatChannel, sender);

        // add each valid mention to the chat message
        mentions.stream()
                .filter(mention ->
                        mention.startPosition() < content.length() &&
                                content.charAt(mention.startPosition()) == '@')
                // Validate that the mention is authorised
                .filter( mention -> validateMention(sender, mention.userId(), channelId))
                .forEach(mention -> {
                    User mentionedUser = userRepository.findUserById(mention.userId()).getFirst();
                    ChatMention chatMention = new ChatMention(
                            chatMessage,
                            mentionedUser,
                            mention.startPosition(),
                            mention.endPosition());
                    chatMessage.getMentions().add(chatMention);
                });

        return messageRepository.save(chatMessage);
    }

    /**
     * Retrieves the most recent messages for a given chat channel using the default batch size.
     *
     * @param channelId the ID of the chat channel
     * @return list of recent ChatMessage objects
     */
    public List<ChatMessage> getLatestMessage(long channelId) {
        return getLatestMessage(channelId, CHAT_MESSAGE_BATCH_SIZE);
    }

    /**
     * Retrieves a limited number of recent messages for a given chat channel.
     *
     * @param channelId the ID of the chat channel
     * @param limit     the number of messages to retrieve
     * @return list of recent ChatMessage objects
     */
    public List<ChatMessage> getLatestMessage(long channelId, int limit) {
        return messageRepository.findLatestMessages(channelId, limit);
    }

    /**
     * Retrieves messages preceding a specified timestamp and ID using the default batch size.
     *
     * @param channelId            the ID of the chat channel
     * @param lastMessageTimestamp the reference timestamp
     * @param lastMessageId        the reference message ID
     * @return list of older ChatMessage objects
     */
    public List<ChatMessage> getPreviousMessages(long channelId, Instant lastMessageTimestamp, long lastMessageId) {
        return getPreviousMessages(channelId, lastMessageTimestamp, lastMessageId, CHAT_MESSAGE_BATCH_SIZE);
    }

    /**
     * Retrieves a batch of messages older than the specified timestamp and ID for pagination.
     *
     * @param channelId            the ID of the chat channel
     * @param lastMessageTimestamp the reference timestamp
     * @param lastMessageId        the reference message ID
     * @param limit                the number of messages to retrieve
     * @return list of older ChatMessage objects
     */
    public List<ChatMessage> getPreviousMessages(long channelId, Instant lastMessageTimestamp, long lastMessageId, int limit) {
        return messageRepository.findPreviousMessages(channelId, lastMessageTimestamp, lastMessageId, PageRequest.of(0, limit));
    }

    /**
     * Retrieves messages proceeding a specified timestamp and ID using the default batch size.
     *
     * @param channelId            the ID of the chat channel
     * @param recentMessageTimestamp the reference timestamp
     * @param recentMessageId        the reference message ID
     * @return list of older ChatMessage objects
     */
    public List<ChatMessage> getNextMessages(long channelId, Instant recentMessageTimestamp, long recentMessageId) {
        return getNextMessages(channelId, recentMessageTimestamp, recentMessageId, CHAT_MESSAGE_BATCH_SIZE);
    }

    /**
     * Retrieves a batch of messages more recent than the specified timestamp and ID for pagination.
     *
     * @param channelId            the ID of the chat channel
     * @param recentMessageTimestamp the reference timestamp
     * @param recentMessageId        the reference message ID
     * @param limit                the number of messages to retrieve
     * @return list of older ChatMessage objects
     */
    public List<ChatMessage> getNextMessages(long channelId, Instant recentMessageTimestamp, long recentMessageId, int limit) {
        return messageRepository.findNextMessages(channelId, recentMessageTimestamp, recentMessageId, PageRequest.of(0, limit));
    }



    /**
     * Retrieves a window of chat messages around a given timestamp within a specific channel.
     * This method fetches the 10 most recent messages before the provided timestamp,
     * as well as the 10 earliest messages after (and including) the timestamp. The two sets are then
     * merged into a single list and returned.
     *
     * @param channelId         the ID of the chat channel to fetch messages from
     * @param messageTimeStamp  the timestamp around which to fetch messages
     * @return a list of chat messages surrounding the given timestamp within the specified channel
     */
    public List<ChatMessage> getMessagesAroundTimeStamp(long channelId, Instant messageTimeStamp) {
        List<ChatMessage> messages = new ArrayList<>();
        List<ChatMessage> messagesAfterTime = messageRepository.findTop5ByChannelIdAndTimestampGreaterThanOrderByTimestampAsc(channelId, messageTimeStamp);
        List<ChatMessage> messagesBeforeAndEqualTime;
        if (messagesAfterTime.size() < 5) {
            messagesBeforeAndEqualTime = messageRepository.findTop10ByChannelIdAndTimestampLessThanEqualOrderByTimestampDesc(channelId, messageTimeStamp);
        } else {
            messagesBeforeAndEqualTime = messageRepository.findTop6ByChannelIdAndTimestampLessThanEqualOrderByTimestampDesc(channelId, messageTimeStamp);
        }
        messages.addAll( messagesAfterTime.reversed());
        messages.addAll(messagesBeforeAndEqualTime);
        return messages;
    }

    /**
     * Validates that a mention is authorised to be sent
     * Checks if the receiver is a recognised user within the system and checks
     * that both the sender and the receiver are part of the channel from which the mention
     * was sent
     *
     * @param sender the user who sent the mention
     * @param receiverId the user who will receive the mention
     * @param channelId the channel where the mention was sent
     */
    private boolean validateMention(User sender, long receiverId, long channelId) {
        return userRepository.findUserById(receiverId).stream().findFirst().isPresent() &&
                channelRepository.isUserMemberOfChannel(channelId, sender.getId()) &&
                channelRepository.isUserMemberOfChannel(channelId, receiverId);
    }

    /**
     * Finds a list of latest messages up to a limit for a specific chat channel excluding a user
     * @param channelId the ID of the channel to find the messages from
     * @param user the user to exclude
     * @param limit the limit of messages to find
     * @return a list of messages for a specific chat channel excluding a user
     */
    public List<ChatMessage> getLatestMessagesExcludingUser(long channelId, User user, int limit) {
        return messageRepository.findLatestMessagesExcludingUser(channelId, user, limit);
    }
}
