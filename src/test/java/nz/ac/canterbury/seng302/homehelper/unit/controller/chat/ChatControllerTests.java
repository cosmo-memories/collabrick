package nz.ac.canterbury.seng302.homehelper.unit.controller.chat;


import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.service.chat.*;
import nz.ac.canterbury.seng302.homehelper.controller.chat.ChatController;
import nz.ac.canterbury.seng302.homehelper.controller.renovation.NewIndividualRenovationController;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMention;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMessage;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatChannelNotFoundException;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatChannelUnauthorisedException;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatMessageException;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatUserAlreadyMemberException;
import nz.ac.canterbury.seng302.homehelper.model.chat.IncomingMention;
import nz.ac.canterbury.seng302.homehelper.model.chat.IncomingMessage;
import nz.ac.canterbury.seng302.homehelper.model.chat.OutgoingMention;
import nz.ac.canterbury.seng302.homehelper.model.chat.OutgoingMessage;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragment;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentMention;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentText;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentType;
import nz.ac.canterbury.seng302.homehelper.model.user.PublicUserDetails;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatControllerTests {

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private UserService userService;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private ChatChannelService chatChannelService;

    @Mock
    private ChatMentionService chatMentionService;

    @Mock
    private ChatAiService chatAiService;

    @Mock
    private RenovationService renovationService;


    @Mock
    private ChatFragmentService chatFragmentService;

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private NewIndividualRenovationController getChatController;

    @InjectMocks
    private ChatController chatController;

    @Test
    void getChat_GivenUserIsMemberOfChannelAndChannelBelongsToRenovation_ThenReturnsChatPageView() throws NoResourceFoundException {
        Model model = mock();
        HttpServletRequest request = mock();
        User user = mock();
        ChatChannel chatChannel = mock();
        Renovation renovation = mock(Renovation.class);
        long renovationId = 10L;
        long userId = 20L;
        long channelId = 30L;
        when(user.getId()).thenReturn(userId);
        when(chatChannelService.findById(channelId)).thenReturn(Optional.of(chatChannel));
        when(renovation.getId()).thenReturn(renovationId);
        when(chatChannel.getRenovation()).thenReturn(renovation);
        when(chatChannel.getMembers()).thenReturn(List.of(user));

        try (MockedStatic<UserUtil> userUtil = mockStatic(UserUtil.class)) {
            userUtil.when(() -> UserUtil.getUserFromHttpServletRequest(any(), eq(request))).thenReturn(user);

            String result = getChatController.getChat(channelId, user, renovation, model, null);
            verify(model).addAttribute("userId", userId);
            verify(model).addAttribute("renovationId", renovationId);
            verify(model).addAttribute("channel", chatChannel);
            verify(model).addAttribute("contentType", "chat");
            assertEquals("renovation/layout", result);
        }
    }

    @Test
    void getChat_GivenChatChannelDoesNotExist_ThenThrowsNoResourceFoundException() {
        Model model = mock();
        HttpServletRequest request = mock();
        User user = mock();
        Renovation renovation = mock(Renovation.class);
        long channelId = 30L;
        when(chatChannelService.findById(channelId)).thenReturn(Optional.empty());

        try (MockedStatic<UserUtil> userUtil = mockStatic(UserUtil.class)) {
            userUtil.when(() -> UserUtil.getUserFromHttpServletRequest(any(), eq(request))).thenReturn(user);

            assertThrows(NoResourceFoundException.class, () -> getChatController.getChat(channelId, user, renovation, model, null));
        }
    }

    @Test
    void getChat_GivenRenovationDoesNotExist_ThenThrowsNoResourceFoundException() {
        Model model = mock();
        HttpServletRequest request = mock();
        User user = mock();
        ChatChannel chatChannel = mock();
        Renovation renovation = mock(Renovation.class);
        long renovationId = 10L;
        long channelId = 30L;
        when(chatChannelService.findById(channelId)).thenReturn(Optional.of(chatChannel));

        try (MockedStatic<UserUtil> userUtil = mockStatic(UserUtil.class)) {
            userUtil.when(() -> UserUtil.getUserFromHttpServletRequest(any(), eq(request))).thenReturn(user);

            assertThrows(NoResourceFoundException.class, () -> getChatController.getChat(channelId, user, renovation, model, null));
        }
    }

    @Test
    void getChat_GivenChannelDoesNotBelongToRenovation_ThenThrowsNoResourceFoundException() {
        Model model = mock();
        HttpServletRequest request = mock();
        User user = mock();
        ChatChannel chatChannel = mock();
        Renovation chatChannelRenovation = mock(Renovation.class);
        Renovation otherRenovation = mock(Renovation.class);
        long renovationId = 10L;
        long channelId = 30L;
        when(chatChannelService.findById(channelId)).thenReturn(Optional.of(chatChannel));
        when(chatChannel.getRenovation()).thenReturn(chatChannelRenovation);

        try (MockedStatic<UserUtil> userUtil = mockStatic(UserUtil.class)) {
            userUtil.when(() -> UserUtil.getUserFromHttpServletRequest(any(), eq(request))).thenReturn(user);

            assertThrows(NoResourceFoundException.class, () -> getChatController.getChat(channelId, user, otherRenovation, model, null));
        }
    }

    @Test
    void getChat_GivenUserIsNotMemberOfChannel_ThenThrowsNoResourceFoundException() {
        Model model = mock();
        HttpServletRequest request = mock();
        User user = mock();
        ChatChannel chatChannel = mock();
        Renovation renovation = mock(Renovation.class);
        long renovationId = 10L;
        long channelId = 30L;
        when(chatChannelService.findById(channelId)).thenReturn(Optional.of(chatChannel));
        when(chatChannel.getRenovation()).thenReturn(renovation);
        when(chatChannel.getMembers()).thenReturn(List.of());

        try (MockedStatic<UserUtil> userUtil = mockStatic(UserUtil.class)) {
            userUtil.when(() -> UserUtil.getUserFromHttpServletRequest(any(), eq(request))).thenReturn(user);
            assertThrows(NoResourceFoundException.class, () -> getChatController.getChat(channelId, user, renovation, model, null));
        }
    }

    // Only testing for functioning mentions here
    @Test
    void sendMessage_validMention_MentionIsSavedAndSent() {
        SimpMessageHeaderAccessor headerAccessor = mock();
        User user = mock();
        User mentionedUser = mock();
        ChatMessage savedMessage = mock();
        Renovation renovation = mock(Renovation.class);
        ChatChannel chatChannel = mock();
        Long renovationId = 3L;
        long channelId = 2L;
        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("userId", 1L));
        when(user.getId()).thenReturn(1L);
        when(user.getFname()).thenReturn("Caleb");
        when(user.getLname()).thenReturn("Bradshaw");
        when(user.getImage()).thenReturn("little-caleb-cooper.png");
        when(userService.findUserById(1L)).thenReturn(user);
        when(mentionedUser.getId()).thenReturn(2L);
        IncomingMention incomingMention = new IncomingMention(2L, 3, 12);
        when(chatMessageService.saveMessage(channelId, 1L, "Hello @Sam Smith", List.of(incomingMention))).thenReturn(savedMessage);
        when(chatFragmentService.extractFragmentsFromMessage(savedMessage))
                .thenReturn(List.of(new ChatMessageFragmentText("Hello "), new ChatMessageFragmentMention(2L, "Sam Smith")));

        OutgoingMention mockOutgoingMention = mock(OutgoingMention.class);
        when(mockOutgoingMention.renovationId()).thenReturn(renovationId);
        when(mockOutgoingMention.channelId()).thenReturn(channelId);

        PublicUserDetails senderDetails = mock(PublicUserDetails.class);
        when(senderDetails.getId()).thenReturn(1L);
        when(mockOutgoingMention.sender()).thenReturn(senderDetails);

        when(savedMessage.getSender()).thenReturn(user);
        when(savedMessage.getTimestamp()).thenReturn(Instant.parse("2023-01-01T12:00:00Z"));
        when(savedMessage.getMentions()).thenReturn(List.of(new ChatMention(savedMessage, mentionedUser, 3, 12)));
        when(chatMentionService.createOutgoingMention(eq(renovationId), eq(channelId), eq(user), eq("Hello @Sam Smith"), any(Instant.class))).thenReturn(mockOutgoingMention);
        
        when(chatAiService.handleAiResponse(savedMessage)).thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        IncomingMessage incomingMessage = new IncomingMessage("Hello @Sam Smith", channelId, renovationId, List.of(incomingMention));
        chatController.sendMessage(incomingMessage, headerAccessor);

        ArgumentCaptor<OutgoingMessage> messageCaptor = ArgumentCaptor.forClass(OutgoingMessage.class);
        ArgumentCaptor<OutgoingMention> mentionCaptor = ArgumentCaptor.forClass(OutgoingMention.class);
        verify(simpMessagingTemplate).convertAndSend(
                eq("/topic/renovation/3/channel/2"),
                messageCaptor.capture());
        verify(simpMessagingTemplate).convertAndSend(
                eq("/topic/mention/2"),
                mentionCaptor.capture()
        );
        verify(chatMessageService).saveMessage(2L, 1L, "Hello @Sam Smith", List.of(incomingMention));

        OutgoingMention outgoingMention = mentionCaptor.getValue();
        OutgoingMessage outgoingMessage = messageCaptor.getValue();
        List<ChatMessageFragment> fragments = outgoingMessage.fragments();
        assertEquals(2, fragments.size());
        assertEquals(ChatMessageFragmentType.TEXT, fragments.getFirst().getType());
        assertEquals(ChatMessageFragmentType.MENTION, fragments.getLast().getType());
        assertEquals("Hello ", fragments.getFirst().getText());
        assertEquals("@Sam Smith", fragments.getLast().getText());
        assertEquals(renovationId, outgoingMention.renovationId());
        assertEquals(channelId, outgoingMention.channelId());
        assertEquals(user.getId(), outgoingMention.sender().getId());
    }

    @Test
    void sendMessage_multipleValidMentions_MentionsSent() {
        SimpMessageHeaderAccessor headerAccessor = mock();
        User user = mock();
        User mentionedUser1 = mock();
        User mentionedUser2 = mock();
        ChatMessage savedMessage = mock();
        Renovation renovation = mock(Renovation.class);
        ChatChannel chatChannel = mock();
        Long renovationId = 3L;
        long channelId = 2L;
        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("userId", 1L));
        when(user.getId()).thenReturn(1L);
        when(user.getFname()).thenReturn("Caleb");
        when(user.getLname()).thenReturn("Bradshaw");
        when(user.getImage()).thenReturn("little-caleb-cooper.png");
        when(userService.findUserById(1L)).thenReturn(user);
        when(mentionedUser1.getId()).thenReturn(2L);
        when(mentionedUser2.getId()).thenReturn(4L);
        IncomingMention incomingMention1 = new IncomingMention(2L, 3, 12);
        IncomingMention incomingMention2 = new IncomingMention(4L, 14,23);
        when(chatMessageService.saveMessage(channelId, 1L, "Hello @Sam Smith @Bob Smith", List.of(incomingMention1, incomingMention2))).thenReturn(savedMessage);
        when(chatFragmentService.extractFragmentsFromMessage(savedMessage))
                .thenReturn(List.of(new ChatMessageFragmentText("Hello "),
                        new ChatMessageFragmentMention(2L, "Sam Smith"),
                        new ChatMessageFragmentText(" "),
                        new ChatMessageFragmentMention(4L, "Bob Smith")));

        OutgoingMention mockOutgoingMention = mock(OutgoingMention.class);
        when(mockOutgoingMention.renovationId()).thenReturn(renovationId);
        when(mockOutgoingMention.channelId()).thenReturn(channelId);

        PublicUserDetails senderDetails = mock(PublicUserDetails.class);
        when(senderDetails.getId()).thenReturn(1L);
        when(mockOutgoingMention.sender()).thenReturn(senderDetails);

        when(savedMessage.getSender()).thenReturn(user);
        when(savedMessage.getTimestamp()).thenReturn(Instant.parse("2023-01-01T12:00:00Z"));
        when(savedMessage.getMentions()).thenReturn(List.of(new ChatMention(savedMessage, mentionedUser1, 3, 12),
                new ChatMention(savedMessage, mentionedUser2, 14, 23)));
        when(chatMentionService.createOutgoingMention(eq(renovationId), eq(channelId), eq(user), eq("Hello @Sam Smith @Bob Smith"), any(Instant.class))).thenReturn(mockOutgoingMention);

        when(chatAiService.handleAiResponse(savedMessage)).thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        IncomingMessage incomingMessage = new IncomingMessage("Hello @Sam Smith @Bob Smith", channelId, renovationId, List.of(incomingMention1, incomingMention2));
        chatController.sendMessage(incomingMessage, headerAccessor);

        ArgumentCaptor<OutgoingMessage> messageCaptor = ArgumentCaptor.forClass(OutgoingMessage.class);
        ArgumentCaptor<OutgoingMention> mention1Captor = ArgumentCaptor.forClass(OutgoingMention.class);
        ArgumentCaptor<OutgoingMention> mention2Captor = ArgumentCaptor.forClass(OutgoingMention.class);
        verify(simpMessagingTemplate).convertAndSend(
                eq("/topic/renovation/3/channel/2"),
                messageCaptor.capture());
        verify(simpMessagingTemplate).convertAndSend(
                eq("/topic/mention/2"),
                mention1Captor.capture()
        );
        verify(simpMessagingTemplate).convertAndSend(
                eq("/topic/mention/4"),
                mention2Captor.capture()
        );
        verify(chatMessageService).saveMessage(2L, 1L, "Hello @Sam Smith @Bob Smith", List.of(incomingMention1, incomingMention2));

        OutgoingMention outgoingMention1 = mention1Captor.getValue();
        OutgoingMention outgoingMention2 = mention2Captor.getValue();
        OutgoingMessage outgoingMessage = messageCaptor.getValue();
        List<ChatMessageFragment> fragments = outgoingMessage.fragments();
        assertEquals(4, fragments.size());
        assertEquals(ChatMessageFragmentType.TEXT, fragments.getFirst().getType());
        assertEquals(ChatMessageFragmentType.MENTION, fragments.get(1).getType());
        assertEquals("Hello ", fragments.getFirst().getText());
        assertEquals("@Sam Smith", fragments.get(1).getText());
        assertEquals(" ", fragments.get(2).getText());
        assertEquals("@Bob Smith", fragments.get(3).getText());
        assertEquals(renovationId, outgoingMention1.renovationId());
        assertEquals(renovationId, outgoingMention2.renovationId());
        assertEquals(channelId, outgoingMention1.channelId());
        assertEquals(channelId, outgoingMention2.channelId());
        assertEquals(user.getId(), outgoingMention1.sender().getId());
        assertEquals(user.getId(), outgoingMention2.sender().getId());
    }

    @Test
    void sendMessage_GivenValidUserAndSessionAttributes_ThenSavesAndSendsOutgoingMessage() {
        SimpMessageHeaderAccessor headerAccessor = mock();
        User user = mock();
        ChatMessage savedMessage = mock();
        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("userId", 1L));
        when(user.getId()).thenReturn(1L);
        when(user.getFname()).thenReturn("Caleb");
        when(user.getLname()).thenReturn("Bradshaw");
        when(user.getImage()).thenReturn("little-caleb-cooper.png");
        when(userService.findUserById(1L)).thenReturn(user);
        when(chatMessageService.saveMessage(2L, 1L, "Hello World", List.of())).thenReturn(savedMessage);
        when(chatFragmentService.extractFragmentsFromMessage(savedMessage))
                .thenReturn(List.of(new ChatMessageFragmentText("Hello World")));
        when(chatAiService.handleAiResponse(savedMessage)).thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        when(savedMessage.getSender()).thenReturn(user);
        when(savedMessage.getTimestamp()).thenReturn(Instant.parse("2023-01-01T12:00:00Z"));

        IncomingMessage incomingMessage = new IncomingMessage("Hello World", 2L, 1L, new ArrayList<>());
        chatController.sendMessage(incomingMessage, headerAccessor);

        ArgumentCaptor<OutgoingMessage> captor = ArgumentCaptor.forClass(OutgoingMessage.class);
        verify(simpMessagingTemplate).convertAndSend(
                eq("/topic/renovation/1/channel/2"),
                captor.capture()
        );
        verify(chatMessageService).saveMessage(2L, 1L, "Hello World", List.of());

        OutgoingMessage outgoingMessage = captor.getValue();
        List<ChatMessageFragment> fragments = outgoingMessage.fragments();
        assertEquals(1, fragments.size());
        assertEquals(ChatMessageFragmentType.TEXT, fragments.getFirst().getType());
        assertEquals("Hello World", fragments.getFirst().getText());
        assertEquals("Caleb", outgoingMessage.user().firstName());
        assertEquals("Bradshaw", outgoingMessage.user().lastName());
        assertEquals("little-caleb-cooper.png", outgoingMessage.user().image());
        assertEquals(Instant.parse("2023-01-01T12:00:00Z"), outgoingMessage.date());
    }



    @Test
    void sendMessage_GivenNullSessionAttributes_ThenDoesNotSendMessage() {
        SimpMessageHeaderAccessor headerAccessor = mock();
        when(headerAccessor.getSessionAttributes()).thenReturn(null);

        IncomingMessage incomingMessage = new IncomingMessage("Hello World", 1L, 1L, new ArrayList<>());
        chatController.sendMessage(incomingMessage, headerAccessor);

        verifyNoInteractions(simpMessagingTemplate);
        verifyNoInteractions(chatMessageService);
    }

    @Test
    void sendMessage_GivenMissingUserIdInSession_ThenDoesNotSendMessage() {
        SimpMessageHeaderAccessor headerAccessor = mock();
        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of());

        IncomingMessage incomingMessage = new IncomingMessage("Hello World", 1L, 1L, new ArrayList<>());
        chatController.sendMessage(incomingMessage, headerAccessor);

        verifyNoInteractions(simpMessagingTemplate);
        verifyNoInteractions(chatMessageService);
    }

    @Test
    void sendMessage_GivenUserNotFound_ThenDoesNotSendMessage() {
        SimpMessageHeaderAccessor headerAccessor = mock();
        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("userId", 1L));
        when(userService.findUserById(1L)).thenReturn(null);

        IncomingMessage incomingMessage = new IncomingMessage("Hello World", 1L, 1L, new ArrayList<>());
        chatController.sendMessage(incomingMessage, headerAccessor);

        verifyNoInteractions(simpMessagingTemplate);
        verifyNoInteractions(chatMessageService);
    }

    @Test
    void sendMessage_GivenChatChannelUnauthorisedExceptionThrown_ThenDoesNotSendMessage() {
        SimpMessageHeaderAccessor headerAccessor = mock();
        User user = mock();
        when(user.getId()).thenReturn(1L);
        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("userId", 1L));
        when(userService.findUserById(1L)).thenReturn(user);
        when(chatMessageService.saveMessage(anyLong(), anyLong(), anyString(), anyList()))
                .thenThrow(new ChatChannelUnauthorisedException("User not apart of channel"));

        IncomingMessage incomingMessage = new IncomingMessage("Test", 2L, 1L, new ArrayList<>());
        chatController.sendMessage(incomingMessage, headerAccessor);

        verify(chatMessageService).saveMessage(2L, 1L, "Test", List.of());
        verifyNoInteractions(simpMessagingTemplate);
    }

    @Test
    void sendMessage_GivenChatChannelNotFoundExceptionThrown_ThenDoesNotSendMessage() {
        SimpMessageHeaderAccessor headerAccessor = mock();
        User user = mock();
        when(user.getId()).thenReturn(1L);
        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("userId", 1L));
        when(userService.findUserById(1L)).thenReturn(user);
        when(chatMessageService.saveMessage(anyLong(), anyLong(), anyString(), anyList()))
                .thenThrow(new ChatChannelNotFoundException("Channel not found"));

        IncomingMessage incomingMessage = new IncomingMessage("Test", 2L, 1L, new ArrayList<>());
        chatController.sendMessage(incomingMessage, headerAccessor);

        verify(chatMessageService).saveMessage(2L, 1L, "Test", List.of());
        verifyNoInteractions(simpMessagingTemplate);
    }

    @Test
    void sendMessage_GivenChatMessageExceptionThrown_ThenDoesNotSendMessage() {
        SimpMessageHeaderAccessor headerAccessor = mock();
        User user = mock();
        when(user.getId()).thenReturn(1L);
        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("userId", 1L));
        when(userService.findUserById(1L)).thenReturn(user);
        when(chatMessageService.saveMessage(anyLong(), anyLong(), anyString(), anyList()))
                .thenThrow(new ChatMessageException("Message invalid"));

        IncomingMessage incomingMessage = new IncomingMessage("Test", 2L, 1L, new ArrayList<>());
        chatController.sendMessage(incomingMessage, headerAccessor);

        verify(chatMessageService).saveMessage(2L, 1L, "Test", List.of());
        verifyNoInteractions(simpMessagingTemplate);
    }

    @Test
    void addMember_NewMember_AddMemberIsCalled() {
        long renovationId = 1L;
        long channelId = 42L;
        long userId = 99L;
        List<Long> userIds = List.of(userId);
        HttpServletRequest request = mock();

        Renovation renovation = mock(Renovation.class);
        User user = mock(User.class);

        when(renovationService.getRenovation(renovationId)).thenReturn(Optional.of(renovation));
        when(userService.findUserById(userId)).thenReturn(user);
        when(chatChannelService.isUserMemberOfChannel(user, channelId)).thenReturn(true);
        when(renovation.isMember(user)).thenReturn(true);

        try (MockedStatic<UserUtil> userUtil = mockStatic(UserUtil.class)) {
            userUtil.when(() -> UserUtil.getUserFromHttpServletRequest(any(), eq(request))).thenReturn(user);

            String result = chatController.addMembersToChannel(renovationId, channelId, userIds, request);

            assertEquals("redirect:/renovation/" + renovationId + "/chat/" + channelId, result);
            verify(chatChannelService).addMemberToChatChannel(channelId, userId);
        }

    }

    @Test
    void addMember_RenovationNotFound_Throws404() {
        // Arrange
        long renovationId = 1L;
        long channelId = 42L;
        HttpServletRequest request = mock();
        User user = mock();
        when(renovationService.getRenovation(renovationId)).thenReturn(Optional.empty());
        try (MockedStatic<UserUtil> userUtil = mockStatic(UserUtil.class)) {
            userUtil.when(() -> UserUtil.getUserFromHttpServletRequest(any(), eq(request))).thenReturn(user);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> chatController.addMembersToChannel(renovationId, channelId, List.of(99L), request)
            );


            assertEquals(404, ex.getStatusCode().value());
            assertTrue(Objects.requireNonNull(ex.getReason()).contains("Renovation with the provided ID was not found"));
        }
    }


    @Test
    void addMember_UserNotMemberOfRenovation_RedirectsToNotFound() {
        long renovationId = 1L;
        long channelId = 42L;
        long userId = 99L;

        HttpServletRequest request = mock();


        Renovation renovation = mock(Renovation.class);
        User user = mock(User.class);

        when(renovationService.getRenovation(renovationId)).thenReturn(Optional.of(renovation));
        when(userService.findUserById(userId)).thenReturn(user);
        when(chatChannelService.isUserMemberOfChannel(user, channelId)).thenReturn(true);
        when(renovation.isMember(user)).thenReturn(false);
        try (MockedStatic<UserUtil> userUtil = mockStatic(UserUtil.class)) {
            userUtil.when(() -> UserUtil.getUserFromHttpServletRequest(any(), eq(request))).thenReturn(user);
            String result = chatController.addMembersToChannel(renovationId, channelId, List.of(userId), request);

            assertEquals("redirect:/notFound", result);
            verify(chatChannelService, never()).addMemberToChatChannel(anyLong(), anyLong());
        }
    }

    @Test
    void addMember_UserAlreadyInChannel_LogsAndRedirects() {
        long renovationId = 1L;
        long channelId = 42L;
        long userId = 99L;
        HttpServletRequest request = mock();

        Renovation renovation = mock(Renovation.class);
        User user = mock(User.class);

        when(renovationService.getRenovation(renovationId)).thenReturn(Optional.of(renovation));
        when(userService.findUserById(userId)).thenReturn(user);
        when(chatChannelService.isUserMemberOfChannel(user, channelId)).thenReturn(true);
        when(renovation.isMember(user)).thenReturn(true);
        doThrow(new ChatUserAlreadyMemberException("already member"))
                .when(chatChannelService).addMemberToChatChannel(channelId, userId);
        try (MockedStatic<UserUtil> userUtil = mockStatic(UserUtil.class)) {
            userUtil.when(() -> UserUtil.getUserFromHttpServletRequest(any(), eq(request))).thenReturn(user);

            String result = chatController.addMembersToChannel(renovationId, channelId, List.of(userId), request);

            assertEquals("redirect:/renovation/" + renovationId + "/chat/" + channelId, result);
        }
    }

}
