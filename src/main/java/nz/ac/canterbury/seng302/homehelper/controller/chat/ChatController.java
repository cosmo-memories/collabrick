package nz.ac.canterbury.seng302.homehelper.controller.chat;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMention;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMessage;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.*;
import nz.ac.canterbury.seng302.homehelper.model.chat.IncomingMention;
import nz.ac.canterbury.seng302.homehelper.model.chat.IncomingMessage;
import nz.ac.canterbury.seng302.homehelper.model.chat.OutgoingMention;
import nz.ac.canterbury.seng302.homehelper.model.chat.OutgoingMessage;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragment;
import nz.ac.canterbury.seng302.homehelper.service.chat.*;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static nz.ac.canterbury.seng302.homehelper.utility.UserUtil.getUserFromHttpServletRequest;

/**
 * Controller for handling chat-related interactions and incoming WebSocket messages via STOMP.
 */
@Controller
public class ChatController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final ChatMessageService chatMessageService;
    private final ChatChannelService chatChannelService;
    private final ChatAiService chatAiService;
    private final RenovationService renovationService;
    private final ChatFragmentService chatFragmentService;
    private final ChatMentionService chatMentionService;

    /**
     * Constructs a new ChatController.
     *
     * @param messagingTemplate  The messaging template.
     * @param userService        The user service.
     * @param chatMessageService The chat message service.
     * @param chatChannelService The chat channel service.
     * @param chatFragmentService The chat fragment service.
     * @param chatAiService The chat AI service
     * @param renovationService  The renovation service.
     */
    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate, UserService userService, ChatMessageService chatMessageService, ChatChannelService chatChannelService, ChatFragmentService chatFragmentService, ChatAiService chatAiService, RenovationService renovationService, ChatMentionService chatMentionService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.chatMessageService = chatMessageService;
        this.chatChannelService = chatChannelService;
        this.chatFragmentService = chatFragmentService;
        this.chatAiService = chatAiService;
        this.renovationService = renovationService;
        this.chatMentionService = chatMentionService;
    }


    /**
     * Handles incoming chat messages sent over WebSocket.
     * Messages are expected to be sent to the /app/chat STOMP endpoint.
     * They are then forwarded to the topic /topic/renovation/{renovationId}/channel/{channelId}, where clients can subscribe.
     *
     * @param message        The incoming chat message sent by a client.
     * @param headerAccessor the accessor to get the headers sent with a message
     */
    @MessageMapping("/chat")
    public void sendMessage(IncomingMessage message, SimpMessageHeaderAccessor headerAccessor) {
        logger.debug("Incoming message");
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return;
        }
        Object userId = sessionAttributes.get("userId");
        if (userId == null) {
            return;
        }
        User user = userService.findUserById((long) userId);
        if (user == null) {
            return;
        }

        try {
            List<IncomingMention> mentions = message.mentions();
            ChatMessage savedMessage = chatMessageService.saveMessage(message.channelId(), user.getId(), message.content(), mentions);
            List<ChatMessageFragment> fragments = chatFragmentService.extractFragmentsFromMessage(savedMessage);
            OutgoingMessage outgoingMessage = new OutgoingMessage(savedMessage, fragments, false);
            String destination = "/topic/renovation/" + message.renovationId() + "/channel/" + message.channelId();

            // sending the users message to the channel members
            messagingTemplate.convertAndSend(destination, outgoingMessage);

            // handle the ai response
            chatAiService.handleAiResponse(savedMessage)
                    .thenAccept(optional ->
                            optional.ifPresent(aiMessage ->
                                    messagingTemplate.convertAndSend(destination, aiMessage)))
                    .exceptionally(ex -> {
                        logger.error("Error in AI response", ex);
                        return null;
                    });
            // Send mention notifications
            List<ChatMention> validMentions = savedMessage.getMentions();
            if (!validMentions.isEmpty()) {
                OutgoingMention outgoingMention = chatMentionService.createOutgoingMention(message.renovationId(), message.channelId(), user, message.content(), Instant.now());
                // Publish mentions (removing duplicates so you don't get 2 notifications if you are mentioned twice in the same message
                for (long mentionId : validMentions.stream().map(mention -> mention.getMentionedUser().getId()).distinct().toList()) {
                    messagingTemplate.convertAndSend("/topic/mention/" + mentionId, outgoingMention);
                    logger.info("Mentioned: " + mentionId);
                }
            }
        } catch (ChatMessageException | ChatChannelUnauthorisedException | ChatChannelNotFoundException e) {
            logger.warn("Failed to send chat message", e);
        }
    }

    /**
     * POST endpoint to create a new chat channel.
     *
     * @param id          Renovation ID
     * @param channelName New channel name
     * @param request     HttpServletRequest
     * @return New channel page
     */
    @PostMapping("renovation/{id}/chat/new")
    public String createChannel(@PathVariable(name = "id") long id,
                                @RequestParam(required = false) String channelName,
                                HttpServletRequest request) {
        Renovation renovation = renovationService.getRenovation(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Renovation with the provided ID was not found"));
        try {
            ChatChannel newChannel = chatChannelService.createChannel(renovation, channelName);
            return "redirect:/renovation/" + id + "/chat/" + newChannel.getId();
        } catch (ChatChannelDetailsException e) {
            logger.info(e.getMessage());
            return "redirect:" + request.getHeader("Referer");
        }
    }


    /**
     * POST endpoint to create a new chat channel.
     *
     * @param renovationId Renovation ID
     * @param channelId    Channel Id
     * @param userIds      Ids' of users to be invited
     * @return Chat page
     */
    @PostMapping("renovation/{renovationId}/chat/{channelId}/add")
    public String addMembersToChannel(@PathVariable(name = "renovationId") long renovationId,
                                      @PathVariable(name = "channelId") long channelId,
                                      @RequestParam(required = false) List<Long> userIds,
                                      HttpServletRequest request) {


        try {
            Renovation renovation = renovationService.getRenovation(renovationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                            "Renovation with the provided ID was not found"));
            User user = getUserFromHttpServletRequest(userService, request);
            if (!chatChannelService.isUserMemberOfChannel(user, channelId)) {
                throw new ChatChannelUnauthorisedException("User with id " + user.getId() + " is not a member of channel " + channelId +
                        " so they are not able to add members");
            }
            for (Long userId : userIds) {
                if (!renovation.isMember(userService.findUserById(userId))) {
                    // User is not a member of renovation so don't add them to channels
                    // But a normal user should never be able to do this anyway
                    return "redirect:/notFound";
                } else {
                    // Add user to channel
                    chatChannelService.addMemberToChatChannel(channelId, userId);
                }
            }
        } catch (ChatUserAlreadyMemberException e) {
            // User is already a member of this channel
            // A normal user should never be able to reach this
            logger.info(e.getMessage());
        }

        return "redirect:/renovation/" + renovationId + "/chat/" + channelId;
    }


}
