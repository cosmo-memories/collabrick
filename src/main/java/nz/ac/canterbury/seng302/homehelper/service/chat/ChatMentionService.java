package nz.ac.canterbury.seng302.homehelper.service.chat;

import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMention;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMessage;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.chat.OutgoingMention;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragment;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentMention;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentText;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationDto;
import nz.ac.canterbury.seng302.homehelper.model.user.PublicUserDetails;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatChannelRepository;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatMentionRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Service responsible for parsing ChatMessage content and extracting structured fragments, including plain text
 * and user mentions for easy frontend processing.
 */
@Service
public class ChatMentionService {

    private final ChatMentionRepository chatMentionRepository;
    private final RenovationService renovationService;
    private final ChatChannelService chatChannelService;

    @Autowired
    public ChatMentionService(ChatMentionRepository chatMentionRepository,  RenovationService renovationService,
                              ChatChannelService chatChannelService) {
        this.chatMentionRepository = chatMentionRepository;
        this.renovationService = renovationService;
        this.chatChannelService = chatChannelService;
    }

    /**
     * Gets all unseen mentions for a user, including the related message and channel.
     *
     * @param userId the ID of the user
     * @return a list of unseen {@link ChatMention}, possibly empty
     */
    public List<ChatMention> getUnseenMentions(Long userId) {
        return chatMentionRepository.findUnseenMentionsWithMessageAndChannel(userId);
    }

    /**
     * Marks all mentions as seen for a user in the given channel.
     *
     * @param userId    the ID of the user
     * @param channelId the ID of the chat channel
     */
    public void markMentionsAsSeen(Long userId, Long channelId) {
        chatMentionRepository.markMentionsAsSeenForUserInChannel(userId, channelId);
    }


    /**
     * Create the outgoing mention object
     *
     * @param renovationId id of renovation the mention came from
     * @param channelId id of channel the mention came from
     * @param sender id of sender who sent the message
     * @param messageContent content of the message
     * @return OutgoingMention live outgoing mention object
     */
    public OutgoingMention createOutgoingMention(long renovationId, long channelId, User sender, String messageContent, Instant timestamp) {
        Renovation renovation = renovationService.getRenovation(renovationId).orElseThrow();
        RenovationDto renovationDto = new RenovationDto(renovation.getId(), renovation.getName());
        PublicUserDetails senderDto = new PublicUserDetails(sender);
        String channelName = chatChannelService.getChannelById(channelId).orElseThrow().getName();
        return new OutgoingMention(renovationDto, channelId, channelName, senderDto, messageContent, timestamp);
    }

    public List<OutgoingMention> getOutgoingMentions(Long userId) {
        List<ChatMention> mentions = getUnseenMentions(userId);

        return mentions.stream().map(mention -> {
            Renovation renovation = mention.getChatMessage().getChannel().getRenovation();
            ChatChannel channel = mention.getChatMessage().getChannel();
            User sender = mention.getChatMessage().getSender();
            return createOutgoingMention(renovation.getId(), channel.getId(), sender, mention.getMessage().getContent(), mention.getMessage().getTimestamp());
        }).toList();
    }
}
