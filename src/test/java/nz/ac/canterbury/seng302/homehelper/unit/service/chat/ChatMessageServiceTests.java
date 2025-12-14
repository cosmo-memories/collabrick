package nz.ac.canterbury.seng302.homehelper.unit.service.chat;

import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMessage;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.user.UserNotFoundException;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatChannelNotFoundException;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatChannelUnauthorisedException;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatMessageException;
import nz.ac.canterbury.seng302.homehelper.model.chat.IncomingMention;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatChannelRepository;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatMessageRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatMessageService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTests {

    private @Mock ChatChannelRepository chatChannelRepository;
    private @Mock ChatMessageRepository chatMessageRepository;
    private @Mock UserRepository userRepository;


    private @InjectMocks ChatMessageService chatMessageService;

    // saveMessage

    @Test
    void testSaveMessage_GivenValidData_ShouldSaveMessage() {
        long channelId = 1L;
        long senderId = 2L;
        String content = "Hello there!";
        User sender = mock(User.class);
        ChatChannel channel = mock(ChatChannel.class);
        when(chatChannelRepository.isUserMemberOfChannel(channelId, senderId)).thenReturn(true);
        when(chatChannelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(userRepository.findUserById(senderId)).thenReturn(List.of(sender));
        when(chatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage result = chatMessageService.saveMessage(channelId, senderId, content, List.of());
        assertEquals(content, result.getContent());
        assertEquals(channel, result.getChannel());
        assertEquals(sender, result.getSender());
        assertNotNull(result.getTimestamp());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void testSaveMessage_GivenUserNotInChannel_ShouldThrowUnauthorisedException() {
        when(chatChannelRepository.isUserMemberOfChannel(1L, 2L)).thenReturn(false);

        assertThrows(ChatChannelUnauthorisedException.class,
                () -> chatMessageService.saveMessage(1L, 2L, "test", List.of()));
        verify(chatChannelRepository, never()).findById(anyLong());
        verify(chatChannelRepository, never()).save(any());
    }

    @Test
    void testSaveMessage_GivenMissingChannel_ShouldThrowChannelNotFoundException() {
        when(chatChannelRepository.isUserMemberOfChannel(1L, 2L)).thenReturn(true);
        when(chatChannelRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ChatChannelNotFoundException.class,
                () -> chatMessageService.saveMessage(1L, 2L, "test", List.of()));
    }

    @Test
    void testSaveMessage_GivenMissingUser_ShouldThrowUserNotFoundException() {
        ChatChannel channel = mock(ChatChannel.class);
        when(chatChannelRepository.isUserMemberOfChannel(1L, 2L)).thenReturn(true);
        when(chatChannelRepository.findById(1L)).thenReturn(Optional.of(channel));
        when(userRepository.findUserById(2L)).thenReturn(List.of());

        assertThrows(UserNotFoundException.class,
                () -> chatMessageService.saveMessage(1L, 2L, "test", List.of()));
    }

    @Test
    void testSaveMessage_OneMentionValidMention_MentionSaved() {
        long channelId = 1L;
        long senderId = 2L;
        long mentionedUserId = 6L;
        String content = "Hi @Sam Smith";
        User sender = mock(User.class);
        User mentionedUser = mock(User.class);
        ChatChannel channel = mock(ChatChannel.class);

        when(sender.getId()).thenReturn(senderId);
        when(mentionedUser.getId()).thenReturn(mentionedUserId);
        when(chatChannelRepository.isUserMemberOfChannel(channelId, senderId)).thenReturn(true);
        when(chatChannelRepository.isUserMemberOfChannel(channelId, mentionedUserId)).thenReturn(true);
        when(chatChannelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(userRepository.findUserById(senderId)).thenReturn(List.of(sender));
        when(userRepository.findUserById(mentionedUserId)).thenReturn(List.of(mentionedUser));
        when(chatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        IncomingMention incomingMention = new IncomingMention(mentionedUserId, 3, 12);
        ChatMessage result = chatMessageService.saveMessage(channelId, senderId, content, List.of(incomingMention));
        assertEquals(content, result.getContent());
        assertEquals(channel, result.getChannel());
        assertEquals(sender, result.getSender());
        assertNotNull(result.getTimestamp());
        assertEquals(1, result.getMentions().size());
        assertEquals(6L, result.getMentions().getFirst().getMentionedUser().getId());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }
    @Test
    void testSaveMessage_MultipleValidMentions_MentionsSaved() {
        long channelId = 1L;
        long senderId = 2L;
        long mentionedUserId1 = 6L;
        long mentionedUserId2 = 7L;
        String content = "Hi @Sam Smith @Bpb Smith";
        User sender = mock(User.class);
        User mentionedUser1 = mock(User.class);
        User mentionedUser2 = mock(User.class);
        ChatChannel channel = mock(ChatChannel.class);

        when(sender.getId()).thenReturn(senderId);
        when(mentionedUser1.getId()).thenReturn(mentionedUserId1);
        when(mentionedUser2.getId()).thenReturn(mentionedUserId2);
        when(chatChannelRepository.isUserMemberOfChannel(channelId, senderId)).thenReturn(true);
        when(chatChannelRepository.isUserMemberOfChannel(channelId, mentionedUserId1)).thenReturn(true);
        when(chatChannelRepository.isUserMemberOfChannel(channelId, mentionedUserId2)).thenReturn(true);
        when(chatChannelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(userRepository.findUserById(senderId)).thenReturn(List.of(sender));
        when(userRepository.findUserById(mentionedUserId1)).thenReturn(List.of(mentionedUser1));
        when(userRepository.findUserById(mentionedUserId2)).thenReturn(List.of(mentionedUser2));
        when(chatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        IncomingMention incomingMention1 = new IncomingMention(mentionedUserId1, 3, 12);
        IncomingMention incomingMention2 = new IncomingMention(mentionedUserId2, 14, 23);
        ChatMessage result = chatMessageService.saveMessage(channelId, senderId, content, List.of(incomingMention1, incomingMention2));
        assertEquals(content, result.getContent());
        assertEquals(channel, result.getChannel());
        assertEquals(sender, result.getSender());
        assertNotNull(result.getTimestamp());
        assertEquals(2, result.getMentions().size());
        assertEquals(6L, result.getMentions().getFirst().getMentionedUser().getId());
        assertEquals(7L, result.getMentions().getLast().getMentionedUser().getId());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void testSaveMessage_MentionedUserDoesNotExist_MentionBypassed() {
        long channelId = 1L;
        long senderId = 2L;
        long mentionedUserId = 6L;
        String content = "Hi @Sam Smith";
        User sender = mock(User.class);
        ChatChannel channel = mock(ChatChannel.class);

        when(chatChannelRepository.isUserMemberOfChannel(channelId, senderId)).thenReturn(true);
        when(chatChannelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(userRepository.findUserById(senderId)).thenReturn(List.of(sender));
        when(userRepository.findUserById(mentionedUserId)).thenReturn(List.of());
        when(chatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        IncomingMention incomingMention = new IncomingMention(mentionedUserId, 3, 12);
        ChatMessage result = chatMessageService.saveMessage(channelId, senderId, content, List.of(incomingMention));
        assertEquals(content, result.getContent());
        assertEquals(channel, result.getChannel());
        assertEquals(sender, result.getSender());
        assertNotNull(result.getTimestamp());
        assertEquals(0, result.getMentions().size());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void testSaveMessage_MentionedUserNotInChannel_MentionBypassed() {
        long channelId = 1L;
        long senderId = 2L;
        long mentionedUserId = 6L;
        String content = "Hi @Sam Smith";
        User sender = mock(User.class);
        User mentionedUser = mock(User.class);
        ChatChannel channel = mock(ChatChannel.class);

        when(sender.getId()).thenReturn(senderId);
        when(chatChannelRepository.isUserMemberOfChannel(channelId, senderId)).thenReturn(true);
        when(chatChannelRepository.isUserMemberOfChannel(channelId, mentionedUserId)).thenReturn(false);
        when(chatChannelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(userRepository.findUserById(senderId)).thenReturn(List.of(sender));
        when(userRepository.findUserById(mentionedUserId)).thenReturn(List.of(mentionedUser));
        when(chatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        IncomingMention incomingMention = new IncomingMention(mentionedUserId, 3, 12);
        ChatMessage result = chatMessageService.saveMessage(channelId, senderId, content, List.of(incomingMention));
        assertEquals(content, result.getContent());
        assertEquals(channel, result.getChannel());
        assertEquals(sender, result.getSender());
        assertNotNull(result.getTimestamp());
        assertEquals(0, result.getMentions().size());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void testSaveMessage_OneValidOneInvalidMention_InvalidMentionBypassed() {
        long channelId = 1L;
        long senderId = 2L;
        long mentionedUserId1 = 6L;
        long mentionedUserId2 = 7L;
        String content = "Hi @Sam Smith @Bob Smith";
        User sender = mock(User.class);
        User mentionedUser1 = mock(User.class);
        User mentionedUser2 = mock(User.class);
        ChatChannel channel = mock(ChatChannel.class);

        when(sender.getId()).thenReturn(senderId);
        when(mentionedUser2.getId()).thenReturn(mentionedUserId2);
        when(chatChannelRepository.isUserMemberOfChannel(channelId, senderId)).thenReturn(true);
        when(chatChannelRepository.isUserMemberOfChannel(channelId, mentionedUserId1)).thenReturn(false);
        when(chatChannelRepository.isUserMemberOfChannel(channelId, mentionedUserId2)).thenReturn(true);
        when(chatChannelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(userRepository.findUserById(senderId)).thenReturn(List.of(sender));
        when(userRepository.findUserById(mentionedUserId1)).thenReturn(List.of(mentionedUser1));
        when(userRepository.findUserById(mentionedUserId2)).thenReturn(List.of(mentionedUser2));
        when(chatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        IncomingMention incomingMention1 = new IncomingMention(mentionedUserId1, 3, 12);
        IncomingMention incomingMention2 = new IncomingMention(mentionedUserId2, 14, 23);
        ChatMessage result = chatMessageService.saveMessage(channelId, senderId, content, List.of(incomingMention1, incomingMention2));
        assertEquals(content, result.getContent());
        assertEquals(channel, result.getChannel());
        assertEquals(sender, result.getSender());
        assertNotNull(result.getTimestamp());
        assertEquals(1, result.getMentions().size());
        assertEquals(7L, result.getMentions().getFirst().getMentionedUser().getId());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }



    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "\t",
            "\n",
    })
    void testSaveMessage_GivenBlankMessage_ShouldThrowChatMessageException(String content) {
        long channelId = 1L;
        long senderId = 2L;
        assertThrows(ChatMessageException.class,
                () -> chatMessageService.saveMessage(channelId, senderId, content, List.of()));
    }

    @Test
    void testSaveMessage_GivenTooLongMessage_ShouldThrowChatMessageException() {
        long channelId = 1L;
        long senderId = 2L;
        String content = "a".repeat(2049);
        assertThrows(ChatMessageException.class,
                () -> chatMessageService.saveMessage(channelId, senderId, content, List.of()));
    }

    // getPreviousMessages

    @Test
    void testGetLatestMessage_WithDefaultLimit_ShouldDelegateToRepoWithDefaultLimit() {
        long channelId = 5;
        List<ChatMessage> expected = List.of(mock(ChatMessage.class));
        when(chatMessageRepository.findLatestMessages(channelId, 25)).thenReturn(expected);

        List<ChatMessage> result = chatMessageService.getLatestMessage(channelId);
        assertEquals(expected, result);
    }

    @Test
    void testGetLatestMessage_WithCustomLimit_ShouldDelegateToRepo() {
        long channelId = 5;
        int limit = 10;
        List<ChatMessage> expected = List.of(mock(ChatMessage.class), mock(ChatMessage.class));
        when(chatMessageRepository.findLatestMessages(channelId, limit)).thenReturn(expected);

        List<ChatMessage> result = chatMessageService.getLatestMessage(channelId, limit);
        assertEquals(expected, result);
    }

    // getPreviousMessages

    @Test
    void testGetPreviousMessages_WithDefaultLimit_ShouldCallWithDefaultSize() {
        long channelId = 3;
        Instant time = Instant.parse("2025-08-04T23:07:12.099561800Z");
        long lastId = 100;
        List<ChatMessage> expected = List.of(mock(ChatMessage.class));
        when(chatMessageRepository.findPreviousMessages(eq(channelId), any(Instant.class), eq(lastId), any(PageRequest.class)))
                .thenReturn(expected);

        List<ChatMessage> result = chatMessageService.getPreviousMessages(channelId, time, lastId);
        assertEquals(expected, result);
    }

    @Test
    void testGetPreviousMessages_WithCustomLimit_ShouldUseCorrectPageRequest() {
        long channelId = 3;
        Instant time = Instant.now();
        long lastId = 200;
        int limit = 7;
        List<ChatMessage> expected = List.of(mock(ChatMessage.class), mock(ChatMessage.class));
        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        when(chatMessageRepository.findPreviousMessages(eq(channelId), eq(time), eq(lastId), pageRequestCaptor.capture()))
                .thenReturn(expected);

        List<ChatMessage> result = chatMessageService.getPreviousMessages(channelId, time, lastId, limit);
        assertEquals(expected, result);
        assertEquals(limit, pageRequestCaptor.getValue().getPageSize());
        assertEquals(0, pageRequestCaptor.getValue().getPageNumber()); // should always be page 0
    }

    @Test
    void testGetMessagesAroundTimeStamp_WithOlderMessage_ShouldReturnEvenMessages() {
        long channelId = 2;
        List<ChatMessage> recentMessages = createSequentialMessages(6,11);
        List<ChatMessage> olderMessages = createSequentialMessages(1,6);
        ChatMessage middleMessage = recentMessages.getFirst();

        when(chatMessageRepository.findTop5ByChannelIdAndTimestampGreaterThanOrderByTimestampAsc(channelId, middleMessage.getTimestamp())).thenReturn(olderMessages.reversed());
        when(chatMessageRepository.findTop6ByChannelIdAndTimestampLessThanEqualOrderByTimestampDesc(channelId, middleMessage.getTimestamp())).thenReturn(recentMessages);

        List<ChatMessage> result = chatMessageService.getMessagesAroundTimeStamp(channelId, middleMessage.getTimestamp());
        List<ChatMessage> expected = new ArrayList<>();
        expected.addAll(olderMessages);   // before+equal
        expected.addAll(recentMessages);  // after
        assertEquals(expected,result);
    }

    @Test
    void testGetMessagesAroundTimeStamp_WithRecentMessage_ShouldReturnSkewedResult() {
        long channelId = 1;

        // messages after timestamp (newer) - fewer than 5 to trigger 'if'
        List<ChatMessage> messagesAfter = createSequentialMessages(6, 8); // 3 messages
        // messages before or equal timestamp
        List<ChatMessage> messagesBefore = createSequentialMessages(1, 10);
        ChatMessage middleMessage = messagesAfter.get(0);

        // Mock repository
        when(chatMessageRepository.findTop5ByChannelIdAndTimestampGreaterThanOrderByTimestampAsc(channelId, middleMessage.getTimestamp()))
                .thenReturn(messagesAfter); // less than 5
        when(chatMessageRepository.findTop10ByChannelIdAndTimestampLessThanEqualOrderByTimestampDesc(channelId, middleMessage.getTimestamp()))
                .thenReturn(messagesBefore);
        // Call service
        List<ChatMessage> result = chatMessageService.getMessagesAroundTimeStamp(channelId, middleMessage.getTimestamp());

        // Expected: reversed messagesAfter + messagesBefore
        List<ChatMessage> expected = new ArrayList<>();
        List<ChatMessage> reversedAfter = new ArrayList<>(messagesAfter);
        Collections.reverse(reversedAfter);
        expected.addAll(reversedAfter);
        expected.addAll(messagesBefore);

        assertEquals(expected, result);

        verify(chatMessageRepository, times(1))
                .findTop5ByChannelIdAndTimestampGreaterThanOrderByTimestampAsc(channelId, middleMessage.getTimestamp());
        verify(chatMessageRepository, times(1))
                .findTop10ByChannelIdAndTimestampLessThanEqualOrderByTimestampDesc(channelId, middleMessage.getTimestamp());
        verify(chatMessageRepository, never())
                .findTop6ByChannelIdAndTimestampLessThanEqualOrderByTimestampDesc(anyLong(), any());
    }


    private List<ChatMessage> createSequentialMessages(int start, int end) {
        Instant baseTime = Instant.now();
        User sender = mock(User.class);
        ChatChannel channel = mock(ChatChannel.class);
        return IntStream.rangeClosed(start, end)
                .mapToObj(i -> new ChatMessage(
                        "Message " + i,
                        baseTime.plus(i, ChronoUnit.SECONDS), // ensures unique timestamps
                        channel,
                        sender
                ))
                .toList();
    }


}
