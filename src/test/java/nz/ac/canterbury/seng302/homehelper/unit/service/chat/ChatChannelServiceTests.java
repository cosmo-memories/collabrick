package nz.ac.canterbury.seng302.homehelper.unit.service.chat;

import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.user.UserNotFoundException;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatChannelNotFoundException;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatUserAlreadyMemberException;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatChannelRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BrickAiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatChannelServiceTests {

    private @Mock ChatChannelRepository chatChannelRepository;
    private @Mock UserRepository userRepository;
    private @Mock Renovation renovation;
    private @Mock User user;
    private @Mock User brickAi;
    private @Mock BrickAiService brickAiService;

    private @InjectMocks ChatChannelService channelService;

    // findById

    @Test
    void testFindById_GivenChannelDoesNotExist_ThenReturnEmptyOptional() {
        long channelId = 10L;
        when(chatChannelRepository.findById(channelId)).thenReturn(Optional.empty());

        assertTrue(channelService.findById(channelId).isEmpty());
    }

    @Test
    void testFindById_GivenChannelDoesExist_ThenReturnEmptyOptional() {
        long channelId = 10L;
        ChatChannel channel = mock();
        when(chatChannelRepository.findById(channelId)).thenReturn(Optional.of(channel));

        Optional<ChatChannel> optionalChatChannel = channelService.findById(channelId);
        assertTrue(optionalChatChannel.isPresent());
        assertEquals(channel, optionalChatChannel.get());
    }

    // isUserMemberOfChannel

    @Test
    void testIsUserMemberOfChannel_GivenUserIsMemberOfExistingChannel_ReturnTrue() {
        long channelId = 10L;
        ChatChannel channel = mock();
        when(chatChannelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(channel.getMembers()).thenReturn(List.of(user));

        assertTrue(channelService.isUserMemberOfChannel(user, channelId));
    }

    @Test
    void testIsUserMemberOfChannel_GivenUserIsNotMemberOfExistingChannel_ReturnTrue() {
        long channelId = 10L;
        ChatChannel channel = mock();
        when(chatChannelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(channel.getMembers()).thenReturn(List.of());

        assertFalse(channelService.isUserMemberOfChannel(user, channelId));
    }

    @Test
    void testIsUserMemberOfChannel_GivenChannelDoesNotExist_ReturnTrue() {
        long channelId = 10L;
        when(chatChannelRepository.findById(channelId)).thenReturn(Optional.empty());

        assertFalse(channelService.isUserMemberOfChannel(user, channelId));
    }

    // createChannel

    @Test
    void testCreateChannel_GivenValidName_ShouldCreateAndSaveAndReturnNewChannel() {
        String channelName = "off-topic";
        when(renovation.getOwner()).thenReturn(user);
        when(chatChannelRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(brickAiService.getAiUser()).thenReturn(brickAi);

        ChatChannel channel = channelService.createChannel(renovation, channelName);
        assertEquals(channelName, channel.getName());
        assertEquals(renovation, channel.getRenovation());
        verify(chatChannelRepository, times(2)).save(same(channel));
    }

    @Test
    void testCreateChannel_GivenValidName_ShouldAddRenovationOwnerAsMemberOfCreatedChannel() {
        String channelName = "off-topic";
        when(renovation.getOwner()).thenReturn(user);
        when(chatChannelRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(brickAiService.getAiUser()).thenReturn(brickAi);

        ChatChannel channel = channelService.createChannel(renovation, channelName);
        assertEquals(channelName, channel.getName());
        assertEquals(renovation, channel.getRenovation());
        verify(chatChannelRepository, times(2)).save(same(channel));

        List<User> members = channel.getMembers();
        assertEquals(2, members.size());
        assertEquals(user, members.get(1));
    }

    // addMemberToChatChannel
    @Test
    void testAddMemberToChatChannel_GivenInvalidUserId_ShouldThrowUserNotFoundException() {
        long channelId = 42L;
        long userId = 999L;
        when(userRepository.findUserById(userId)).thenReturn(List.of());

        assertThrows(UserNotFoundException.class, () -> {
            channelService.addMemberToChatChannel(channelId, userId);
        });
        verify(chatChannelRepository, never()).findById(anyLong());
        verify(chatChannelRepository, never()).save(any());
    }

    @Test
    void testAddMemberToChatChannel_GivenInvalidChannelId_ShouldThrowChatChannelNotFoundException() {
        long channelId = 123L;
        long userId = 456L;
        when(userRepository.findUserById(userId)).thenReturn(List.of(user));
        when(chatChannelRepository.findById(channelId)).thenReturn(Optional.empty());

        assertThrows(ChatChannelNotFoundException.class, () -> channelService.addMemberToChatChannel(channelId, userId));
        verify(chatChannelRepository, never()).save(any());
    }

    @Test
    void testAddMemberToChatChannel_GivenUserNotInChannel_ShouldAddUserAndSave() throws NoResourceFoundException {
        long channelId = 1L;
        long userId = 2L;
        User user2 = mock(User.class);
        ChatChannel channel = mock(ChatChannel.class);
        when(userRepository.findUserById(userId)).thenReturn(List.of(user2));
        when(chatChannelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(channel.getMembers()).thenReturn(mock(List.class));
        when(chatChannelRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        channel = channelService.addMemberToChatChannel(channelId, userId);
        verify(channel.getMembers()).add(user2);
        verify(chatChannelRepository).save(same(channel));
    }

    @Test
    void testAddMemberToChatChannel_GivenUserAlreadyInChannel_ShouldThrowException() {
        long channelId = 1L;
        long userId = 2L;
        User user2 = mock(User.class);
        ChatChannel channel = mock(ChatChannel.class);
        List<User> members = mock(List.class);
        when(user2.getId()).thenReturn(userId);
        when(userRepository.findUserById(userId)).thenReturn(List.of(user2));
        when(chatChannelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(channel.getMembers()).thenReturn(members);
        when(members.contains(user2)).thenReturn(true);

        assertThrows(ChatUserAlreadyMemberException.class,
                () -> channelService.addMemberToChatChannel(channelId, userId));
        verify(channel.getMembers(), never()).add(any());
        verify(chatChannelRepository, never()).save(any());
    }

    @Test
    void removeMemberFromChatChannel_RemoveMember_MemberRemoved() {
        long userId1 = 1L;
        User user1 = mock(User.class);
        long userId2 = 2L;
        User user2 = mock(User.class);
        Renovation renovation = mock(Renovation.class);
        ChatChannel channel = new ChatChannel("general", renovation);
        channel.addMember(user1);
        channel.addMember(user2);
        renovation.getChatChannels().add(channel);

        channelService.removeMemberFromChatChannel(channel, user1);
        verify(chatChannelRepository, times(1)).save(any());
        assertEquals(user2, channel.getMembers().getFirst());
    }

}
