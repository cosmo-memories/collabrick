package nz.ac.canterbury.seng302.homehelper.controller.chat;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMessage;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.chat.OutgoingMessage;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragment;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatFragmentService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatMentionService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BrickAiService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatMessageService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;

@RestController
public class ChatHistoryRestController {

    private final ChatChannelService chatChannelService;
    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final BrickAiService brickAiService;
    private final ChatFragmentService chatFragmentService;

    @Autowired
    public ChatHistoryRestController(ChatChannelService chatChannelService, ChatMessageService chatMessageService, UserService userService, BrickAiService brickAiService, ChatFragmentService chatFragmentService) {
        this.chatChannelService = chatChannelService;
        this.chatMessageService = chatMessageService;
        this.userService = userService;
        this.brickAiService = brickAiService;
        this.chatFragmentService = chatFragmentService;
    }

    /**
     * Retrieves the full chat history for the specified chat channel.
     *
     * @param channelId the unique identifier of the chat channel whose history is to be retrieved
     * @param request   the {@link HttpServletRequest} containing the user's authentication/session details
     * @return a list of {@link OutgoingMessage} objects representing the channel's message history
     * @throws NoResourceFoundException if the channel does not exist or the user is not a member
     */
    @GetMapping("/chat/{channelId}/history")
    public List<OutgoingMessage> getChatHistory(@PathVariable long channelId, HttpServletRequest request) throws NoResourceFoundException {
        User user = UserUtil.getUserFromHttpServletRequest(userService, request);
        if (!chatChannelService.isUserMemberOfChannel(user, channelId)) {
            throw new NoResourceFoundException(HttpMethod.GET, "Channel not found");
        }
        return chatMessageService.getLatestMessage(channelId)
                .stream()
                .map(message -> {
                    List<ChatMessageFragment> fragments = chatFragmentService.extractFragmentsFromMessage(message);
                    return new OutgoingMessage(message, fragments, brickAiService.isAiUser(message.getSender()));
                })
                .toList();
    }

    /**
     * Retrieves the previous batch of chat history sent before the provided last message id and timestamp for the
     * specified chat channel
     *
     * @param channelId            the unique identifier of the chat channel whose history is to be retrieved
     * @param lastMessageId        the message id of which to fetch messages before
     * @param lastMessageTimestamp the message timestamp of which to fetch messages before
     * @param request              the HttpServletRequest containing the user's authentication/session details
     * @return a list of OutgoingMessage objects representing the channel's message history
     */
    @GetMapping("/chat/{channelId}/previous")
    public List<OutgoingMessage> getPreviousChatHistory(
            @PathVariable long channelId,
            @RequestParam long lastMessageId,
            @RequestParam Instant lastMessageTimestamp,
            HttpServletRequest request) throws NoResourceFoundException {
        User user = UserUtil.getUserFromHttpServletRequest(userService, request);
        if (!chatChannelService.isUserMemberOfChannel(user, channelId)) {
            throw new NoResourceFoundException(HttpMethod.GET, "Channel not found");
        }

        return chatMessageService.getPreviousMessages(channelId, lastMessageTimestamp, lastMessageId)
                .stream()
                .map(message -> {
                    List<ChatMessageFragment> fragments = chatFragmentService.extractFragmentsFromMessage(message);
                    return new OutgoingMessage(message, fragments, brickAiService.isAiUser(message.getSender()));
                })
                .toList();
    }

    /**
     * Retrieves the next batch of chat history sent before the provided last message id and timestamp for the
     * specified chat channel
     *
     * @param channelId            the unique identifier of the chat channel whose history is to be retrieved
     * @param recentMessageId        the message id of which to fetch messages before
     * @param recentMessageTimestamp the message timestamp of which to fetch messages before
     * @param request              the HttpServletRequest containing the user's authentication/session details
     * @return a list of OutgoingMessage objects representing the channel's message history
     */
    @GetMapping("/chat/{channelId}/next")
    public List<OutgoingMessage> getNextChatHistory(
            @PathVariable long channelId,
            @RequestParam long recentMessageId,
            @RequestParam Instant recentMessageTimestamp,
            HttpServletRequest request) throws NoResourceFoundException {
        User user = UserUtil.getUserFromHttpServletRequest(userService, request);
        if (!chatChannelService.isUserMemberOfChannel(user, channelId)) {
            throw new NoResourceFoundException(HttpMethod.GET, "Channel not found");
        }
        return chatMessageService.getNextMessages(channelId, recentMessageTimestamp, recentMessageId)
                .stream()
                .map(message -> {
                    List<ChatMessageFragment> fragments = chatFragmentService.extractFragmentsFromMessage(message);
                    return new OutgoingMessage(message, fragments, brickAiService.isAiUser(message.getSender()));
                })
                .toList();
    }


    /**
     * Retrieves the batch of chat history around a timestamp, will scroll
     *
     * @param channelId            the unique identifier of the chat channel whose history is to be retrieved
     * @param mentionTime the message timestamp of which to fetch messages around
     * @param request              the HttpServletRequest containing the user's authentication/session details
     * @return a list of OutgoingMessage objects representing the channel's message history
     */
    @GetMapping("/chat/{channelId}/showMention")
    public List<OutgoingMessage> getMessagesAtTime(@PathVariable long channelId,
                                                   @RequestParam(name="mentionTime") Instant mentionTime,
                                                   HttpServletRequest request
                                                   ) throws NoResourceFoundException {
        User user = UserUtil.getUserFromHttpServletRequest(userService, request);
        if (!chatChannelService.isUserMemberOfChannel(user, channelId)) {
            throw new NoResourceFoundException(HttpMethod.GET, "Channel not found");
        }

        List< ChatMessage> test = chatMessageService.getMessagesAroundTimeStamp(channelId, mentionTime);
         return test.stream()
                 .map(message -> {
                     List<ChatMessageFragment> fragments = chatFragmentService.extractFragmentsFromMessage(message);
                     return new OutgoingMessage(message, fragments, brickAiService.isAiUser(message.getSender()));
                 })
                 .toList();

    }
}
