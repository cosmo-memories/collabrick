package nz.ac.canterbury.seng302.homehelper.unit.service.chat;

import nz.ac.canterbury.seng302.homehelper.config.chat.AiPromptConfig;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatAiService;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMention;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMessage;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.ai.RenovationAiView;
import nz.ac.canterbury.seng302.homehelper.model.chat.OutgoingMessage;
import nz.ac.canterbury.seng302.homehelper.model.chat.ai.AiResponse;
import nz.ac.canterbury.seng302.homehelper.model.chat.ai.AiResponseMessage;
import nz.ac.canterbury.seng302.homehelper.model.chat.ai.AiResponseType;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragment;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatMessageService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BrickAiService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatAiServiceTests {

    private @Mock ChatClient chatClient;
    private @Mock RenovationService renovationService;
    private @Mock BrickAiService brickAiService;
    private @Mock ChatMessageService chatMessageService;
    private @Mock AiPromptConfig aiPromptConfig;
    private @InjectMocks ChatAiService chatAiService;

    // chat client prompt builder
    private @Mock ChatClient.ChatClientRequestSpec chatClientRequestSpec;
    private @Mock ChatClient.CallResponseSpec chatClientResponseSpec;

    private User user;
    private User aiUser;
    private Renovation renovation;
    private ChatChannel channel;
    private @Mock ChatMessage userMessage;
    private @Mock ChatMessage aiMessage;
    private @Mock ChatMention aiChatMention;
    private @Mock Message message;
    private @Mock Message contextMessage;


    @BeforeEach
    void setup() {
        this.user = new User("Jane", "Doe", "jane@doe.nz");
        this.user.setId(1L);
        this.aiUser = new User("BrickAI", "", "brickai@homehelper.nz");
        this.aiUser.setId(2L);
        this.renovation = new Renovation("Luxury Bathroom Remodel", "Make a sick asf bathroom");
        this.renovation.setId(10L);
        this.renovation.setOwner(user);
        this.channel = new ChatChannel(100L, "General", this.renovation, List.of(user, aiUser), List.of());
    }

    @Test
    void testHandleAiResponse_GivenMessageNotForAi_ThenReturnsEmptyOptional() {
        when(userMessage.getChannel()).thenReturn(channel);

        Optional<OutgoingMessage> optionalAiMessage = chatAiService.handleAiResponse(userMessage).join();
        assertTrue(optionalAiMessage.isEmpty());
    }

    @Test
    void testHandleAiResponse_GivenMessageForAi_ThenReturnsOutgoingMessage() {
        String userMessageContent = "Hello @BrickAI!";
        String aiMessageContent = "Hi I am BrickAI!";
        AiResponse aiResponse = new AiResponseMessage(aiMessageContent);
        when(userMessage.getMentions()).thenReturn(List.of(aiChatMention));
        when(userMessage.getChannel()).thenReturn(channel);
        when(userMessage.getContent()).thenReturn(userMessageContent);
        when(userMessage.getSender()).thenReturn(user);
        when(userMessage.getTimestamp()).thenReturn(Instant.now());
        when(aiMessage.getSender()).thenReturn(user);
        when(aiChatMention.getMentionedUser()).thenReturn(aiUser);
        when(brickAiService.isAiUser(aiUser)).thenReturn(true);
        when(brickAiService.getAiUser()).thenReturn(aiUser);
        when(renovationService.getRenovationMembers(renovation)).thenReturn(List.of());
        when(chatMessageService.saveMessage(channel.getId(), aiUser.getId(), aiMessageContent, List.of())).thenReturn(aiMessage);
        when(aiPromptConfig.getSystemPromptTemplateInitial(new RenovationAiView(renovation, List.of()))).thenReturn(message);
        when(chatClient.prompt(any(Prompt.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.advisors((Consumer<ChatClient.AdvisorSpec>) any())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(chatClientResponseSpec);
        when(chatClientResponseSpec.entity(AiResponse.class)).thenReturn(aiResponse);

        Optional<OutgoingMessage> optionalAiMessage = chatAiService.handleAiResponse(userMessage).join();
        assertTrue(optionalAiMessage.isPresent());
        ChatMessageFragment first = optionalAiMessage.get().fragments().getFirst();
        assertEquals(aiMessageContent, first.getText());
    }

    @Test
    void testHandleAiResponse_GivenAiThrowsException_ThenReturnsOutgoingResponseFailureMessage() {
        String userMessageContent = "Hello @BrickAI!";
//        ChatMessageAiView aiViewChatMessage = new ChatMessageAiView(userMessage);
        String aiMessageContent = ChatAiService.RESPONSE_FAILURE_MESSAGE;
        when(userMessage.getMentions()).thenReturn(List.of(aiChatMention));
        when(userMessage.getChannel()).thenReturn(channel);
        when(userMessage.getContent()).thenReturn(userMessageContent);
        when(userMessage.getSender()).thenReturn(user);
        when(userMessage.getTimestamp()).thenReturn(Instant.now());
        when(aiMessage.getSender()).thenReturn(user);
        when(aiChatMention.getMentionedUser()).thenReturn(aiUser);
        when(brickAiService.isAiUser(aiUser)).thenReturn(true);
        when(brickAiService.getAiUser()).thenReturn(aiUser);
        when(chatMessageService.saveMessage(channel.getId(), aiUser.getId(), aiMessageContent, List.of())).thenReturn(aiMessage);
        when(aiPromptConfig.getSystemPromptTemplateInitial(new RenovationAiView(renovation, List.of()))).thenReturn(message);
        when(chatClient.prompt(any(Prompt.class))).thenThrow(new RuntimeException("ERROR"));
        when(renovationService.getRenovationMembers(renovation)).thenReturn(List.of());

        Optional<OutgoingMessage> optionalAiMessage = chatAiService.handleAiResponse(userMessage).join();
        assertTrue(optionalAiMessage.isPresent());
        ChatMessageFragment first = optionalAiMessage.get().fragments().getFirst();
        assertEquals(aiMessageContent, first.getText());
    }


    // ChatGPT helped me write this because I couldn't get it working
    @Test
    void testHandleAiResponse_GivenAiNeedsMoreInformation_ThenRequiresChatContextAndSendsMessageBack() {
        String userMessageContent = "@BrickAI What colour is the door?";
        String aiFinalResponseContent = "The door is yellow";

        AiResponse requireContextResponse = mock(AiResponse.class);
        when(requireContextResponse.getType()).thenReturn(AiResponseType.REQUIRE_CHAT_CONTEXT);

        AiResponse finalResponse = new AiResponseMessage(aiFinalResponseContent);

        when(userMessage.getMentions()).thenReturn(List.of(aiChatMention));
        when(userMessage.getChannel()).thenReturn(channel);
        when(userMessage.getContent()).thenReturn(userMessageContent);
        when(userMessage.getSender()).thenReturn(user);
        when(userMessage.getTimestamp()).thenReturn(Instant.now());
        when(aiMessage.getSender()).thenReturn(user);
        when(aiChatMention.getMentionedUser()).thenReturn(aiUser);
        when(brickAiService.isAiUser(aiUser)).thenReturn(true);
        when(brickAiService.getAiUser()).thenReturn(aiUser);
        when(renovationService.getRenovationMembers(renovation)).thenReturn(List.of());

        when(aiPromptConfig.getSystemPromptTemplateInitial(any(RenovationAiView.class))).thenReturn(message);
        when(aiPromptConfig.getSystemPromptTemplateWithChatContext(any())).thenReturn(contextMessage);

        ChatMessage previousMessage = mock(ChatMessage.class);
        User previousUser = mock(User.class);
        when(previousMessage.getSender()).thenReturn(previousUser);
        when(previousMessage.getTimestamp()).thenReturn(Instant.now());
        when(previousUser.getFullName()).thenReturn("Steve");
        when(previousMessage.getContent()).thenReturn("The door is yellow");
        when(chatMessageService.getLatestMessagesExcludingUser(channel.getId(), aiUser, 20)).thenReturn(List.of(previousMessage));

        when(chatMessageService.saveMessage(channel.getId(), aiUser.getId(), aiFinalResponseContent, List.of())).thenReturn(aiMessage);
        when(aiMessage.getSender()).thenReturn(user);

        when(chatClient.prompt(any(Prompt.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.advisors((Consumer<ChatClient.AdvisorSpec>) any())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(chatClientResponseSpec);

        when(chatClientResponseSpec.entity(AiResponse.class)).thenReturn(requireContextResponse).thenReturn(finalResponse);

        Optional<OutgoingMessage> optionalAiMessage = chatAiService.handleAiResponse(userMessage).join();

        assertTrue(optionalAiMessage.isPresent());
        ChatMessageFragment first = optionalAiMessage.get().fragments().getFirst();
        assertEquals(aiFinalResponseContent, first.getText());
    }

    @Test
    void testHandleAiResponse_GivenAiNeedsMoreInformationAndInformationDoesntHaveAnswer_ThenSendsFallbackMessage() {
        String userMessageContent = "@BrickAI What colour is the windows?";
        String aiFinalResponseContent = ChatAiService.NO_INFO_FOUND_MESSAGE;

        AiResponse requireContextResponse = mock(AiResponse.class);
        when(requireContextResponse.getType()).thenReturn(AiResponseType.REQUIRE_CHAT_CONTEXT);

        AiResponse requireMoreContextResponse = mock(AiResponse.class);
        when(requireMoreContextResponse.getType()).thenReturn(AiResponseType.REQUIRE_CHAT_CONTEXT);

        when(userMessage.getMentions()).thenReturn(List.of(aiChatMention));
        when(userMessage.getChannel()).thenReturn(channel);
        when(userMessage.getContent()).thenReturn(userMessageContent);
        when(userMessage.getSender()).thenReturn(user);
        when(userMessage.getTimestamp()).thenReturn(Instant.now());
        when(aiMessage.getSender()).thenReturn(user);
        when(aiChatMention.getMentionedUser()).thenReturn(aiUser);
        when(brickAiService.isAiUser(aiUser)).thenReturn(true);
        when(brickAiService.getAiUser()).thenReturn(aiUser);
        when(renovationService.getRenovationMembers(renovation)).thenReturn(List.of());

        when(aiPromptConfig.getSystemPromptTemplateInitial(any(RenovationAiView.class))).thenReturn(message);
        when(aiPromptConfig.getSystemPromptTemplateWithChatContext(any())).thenReturn(contextMessage);

        ChatMessage previousMessage = mock(ChatMessage.class);
        User previousUser = mock(User.class);
        when(previousMessage.getSender()).thenReturn(previousUser);
        when(previousMessage.getTimestamp()).thenReturn(Instant.now());
        when(previousUser.getFullName()).thenReturn("Steve");
        when(previousMessage.getContent()).thenReturn("The door is yellow");
        when(chatMessageService.getLatestMessagesExcludingUser(channel.getId(), aiUser, 20)).thenReturn(List.of(previousMessage));


        when(chatMessageService.saveMessage(channel.getId(), aiUser.getId(), aiFinalResponseContent, List.of())).thenReturn(aiMessage);
        when(aiMessage.getSender()).thenReturn(user);

        when(chatClient.prompt(any(Prompt.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.advisors((Consumer<ChatClient.AdvisorSpec>) any())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(chatClientResponseSpec);

        when(chatClientResponseSpec.entity(AiResponse.class)).thenReturn(requireContextResponse).thenReturn(requireMoreContextResponse);

        Optional<OutgoingMessage> optionalAiMessage = chatAiService.handleAiResponse(userMessage).join();

        assertTrue(optionalAiMessage.isPresent());
        ChatMessageFragment first = optionalAiMessage.get().fragments().getFirst();
        assertEquals(aiFinalResponseContent, first.getText());
    }

    @Test
    void testHandleAiResponse_GivenAiNeedsMoreInformationAndNoInformationToGive_ThenSendsFallbackMessage() {
        String userMessageContent = "@BrickAI What colour is the windows?";
        String aiFinalResponseContent = ChatAiService.NO_INFO_FOUND_MESSAGE;

        AiResponse requireContextResponse = mock(AiResponse.class);
        when(requireContextResponse.getType()).thenReturn(AiResponseType.REQUIRE_CHAT_CONTEXT);

        AiResponse requireMoreContextResponse = mock(AiResponse.class);
        when(requireMoreContextResponse.getType()).thenReturn(AiResponseType.REQUIRE_CHAT_CONTEXT);

        when(userMessage.getMentions()).thenReturn(List.of(aiChatMention));
        when(userMessage.getChannel()).thenReturn(channel);
        when(userMessage.getContent()).thenReturn(userMessageContent);
        when(userMessage.getSender()).thenReturn(user);
        when(userMessage.getTimestamp()).thenReturn(Instant.now());
        when(aiMessage.getSender()).thenReturn(user);
        when(aiChatMention.getMentionedUser()).thenReturn(aiUser);
        when(brickAiService.isAiUser(aiUser)).thenReturn(true);
        when(brickAiService.getAiUser()).thenReturn(aiUser);
        when(renovationService.getRenovationMembers(renovation)).thenReturn(List.of());

        when(aiPromptConfig.getSystemPromptTemplateInitial(any(RenovationAiView.class))).thenReturn(message);
        when(aiPromptConfig.getSystemPromptTemplateWithChatContext(any())).thenReturn(contextMessage);

        when(chatMessageService.getLatestMessagesExcludingUser(channel.getId(), aiUser, 20)).thenReturn(List.of());

        when(chatMessageService.saveMessage(channel.getId(), aiUser.getId(), aiFinalResponseContent, List.of())).thenReturn(aiMessage);
        when(aiMessage.getSender()).thenReturn(user);

        when(chatClient.prompt(any(Prompt.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.advisors((Consumer<ChatClient.AdvisorSpec>) any())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(chatClientResponseSpec);

        when(chatClientResponseSpec.entity(AiResponse.class)).thenReturn(requireContextResponse).thenReturn(requireMoreContextResponse);

        Optional<OutgoingMessage> optionalAiMessage = chatAiService.handleAiResponse(userMessage).join();

        assertTrue(optionalAiMessage.isPresent());
        ChatMessageFragment first = optionalAiMessage.get().fragments().getFirst();
        assertEquals(aiFinalResponseContent, first.getText());
    }
}
