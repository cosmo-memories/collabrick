package nz.ac.canterbury.seng302.homehelper.unit.controller.chat;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.controller.chat.ChatHistoryRestController;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMessage;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.chat.OutgoingMessage;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragment;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentText;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentType;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatFragmentService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatMentionService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BrickAiService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatMessageService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatHistoryRestControllerTests {

    private @Mock ChatChannelService chatChannelService;
    private @Mock ChatMessageService chatMessageService;
    private @Mock ChatFragmentService chatFragmentService;
    private @Mock BrickAiService brickAiService;
    private @Mock UserService userService;
    private @Mock User user;

    private @InjectMocks ChatHistoryRestController chatHistoryRestController;

    @Test
    void testGetChatHistory_GivenUserNotMemberOfChannelOrChannelDoesNotExist_ThenThrowNoResourceFoundException() {
        long channelId = 10L;
        HttpServletRequest request = mock();
        when(chatChannelService.isUserMemberOfChannel(user, channelId)).thenReturn(false);
        try (MockedStatic<UserUtil> mockedStatic = mockStatic(UserUtil.class)) {
            mockedStatic.when(() -> UserUtil.getUserFromHttpServletRequest(userService, request))
                    .thenReturn(user);

            assertThrows(NoResourceFoundException.class, () -> chatHistoryRestController.getChatHistory(channelId, request));
        }
    }

    @Test
    void testGetChatHistory_GivenUserMemberAndMessagesExist_ThenReturnOutgoingMessageList() throws NoResourceFoundException {
        long userId = 5L;
        long channelId = 10L;
        HttpServletRequest request = mock();
        ChatMessage message = mock();
        User user = mock();
        when(message.getContent()).thenReturn("Hello");
        when(message.getTimestamp()).thenReturn(Instant.now());
        when(message.getSender()).thenReturn(user);
        when(user.getFname()).thenReturn("Joe");
        when(user.getLname()).thenReturn("Bob");
        when(user.getImage()).thenReturn("image");
        when(user.getId()).thenReturn(userId);
        when(chatChannelService.isUserMemberOfChannel(user, channelId)).thenReturn(true);
        when(chatMessageService.getLatestMessage(channelId)).thenReturn(List.of(message));
        when(chatFragmentService.extractFragmentsFromMessage(message))
                .thenReturn(List.of(new ChatMessageFragmentText("Hello")));
        when(brickAiService.isAiUser(user)).thenReturn(false);

        try (MockedStatic<UserUtil> mockedStatic = mockStatic(UserUtil.class)) {
            mockedStatic.when(() -> UserUtil.getUserFromHttpServletRequest(userService, request))
                    .thenReturn(user);

            List<OutgoingMessage> chatHistory = chatHistoryRestController.getChatHistory(channelId, request);
            assertEquals(1, chatHistory.size());

            OutgoingMessage outgoingMessage = chatHistory.getFirst();
            List<ChatMessageFragment> fragments = outgoingMessage.fragments();
            assertEquals(1, fragments.size());
            assertEquals(ChatMessageFragmentType.TEXT, fragments.getFirst().getType());
            assertEquals(message.getContent(), fragments.getFirst().getText());
            assertEquals(outgoingMessage.date(), message.getTimestamp());
            assertEquals("Joe", outgoingMessage.user().firstName());
            assertEquals("Bob", outgoingMessage.user().lastName());
            assertEquals("image", outgoingMessage.user().image());
            assertEquals(userId, outgoingMessage.user().id());
        }
    }

    @Test
    void testGetChatHistory_GivenUserMemberAndNoMessages_ThenReturnEmptyList() throws NoResourceFoundException {
        long channelId = 10L;
        HttpServletRequest request = mock();
        User user = mock();
        when(chatChannelService.isUserMemberOfChannel(user, channelId)).thenReturn(true);
        when(chatMessageService.getLatestMessage(channelId)).thenReturn(List.of());

        try (MockedStatic<UserUtil> mockedStatic = mockStatic(UserUtil.class)) {
            mockedStatic.when(() -> UserUtil.getUserFromHttpServletRequest(userService, request))
                    .thenReturn(user);

            List<OutgoingMessage> chatHistory = chatHistoryRestController.getChatHistory(channelId, request);
            assertTrue(chatHistory.isEmpty());
        }
    }
}
