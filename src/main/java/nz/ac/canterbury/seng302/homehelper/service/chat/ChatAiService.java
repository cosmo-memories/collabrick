package nz.ac.canterbury.seng302.homehelper.service.chat;

import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.config.chat.AiPromptConfig;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatLink;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMessage;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.TaskDetailsExceptions;
import nz.ac.canterbury.seng302.homehelper.model.ai.ChatMessageAiView;
import nz.ac.canterbury.seng302.homehelper.model.ai.RenovationAiView;
import nz.ac.canterbury.seng302.homehelper.model.chat.OutgoingMessage;
import nz.ac.canterbury.seng302.homehelper.model.chat.ai.AiResponse;
import nz.ac.canterbury.seng302.homehelper.model.chat.ai.AiResponseMessage;
import nz.ac.canterbury.seng302.homehelper.model.chat.ai.AiResponseTaskCreation;
import nz.ac.canterbury.seng302.homehelper.model.chat.ai.AiResponseType;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragment;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentLink;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentText;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatMessageRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BrickAiService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.TaskService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service class responsible for handling AI-driven renovation chats using a ChatClient.
 */
@Service
public class ChatAiService {

    public static final String RESPONSE_FAILURE_MESSAGE = "Sorry, I can't help right now. Try again later.";
    public static final String NO_INFO_FOUND_MESSAGE = "I am sorry, I couldn't find that information.";
    public static final String CHAT_CONTEXT_FOLLOWUP = "Now that you have the chat history, please try to answer the original question. If you still can't, say you couldn't find the information.";
    public static final String TASK_CREATED_MESSAGE = "I have created the task ";
    public static final String TASK_CREATION_FAILED_MESSAGE = "Sorry, I can't create this task. Try again later.";
    public static final String TASK_INCORRECT_DATE_MESSAGE = "The date cannot be in the past.";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ChatClient chatClient;
    private final AiPromptConfig aiPromptConfig;
    private final BrickAiService brickAiService;
    private final ChatMessageService chatMessageService;
    private final RenovationService renovationService;
    private final AppConfig appConfig;
    private final ChatMessageRepository chatMessageRepository;
    private final TaskService taskService;

    @Autowired
    public ChatAiService(ChatClient chatClient, AiPromptConfig aiPromptConfig, BrickAiService brickAiService, ChatMessageService chatMessageService, RenovationService renovationService, AppConfig appConfig, AppConfig appConfig1, ChatMessageRepository chatMessageRepository, TaskService taskService) {
        this.chatClient = chatClient;
        this.aiPromptConfig = aiPromptConfig;
        this.brickAiService = brickAiService;
        this.chatMessageService = chatMessageService;
        this.renovationService = renovationService;
        this.appConfig = appConfig1;
        this.chatMessageRepository = chatMessageRepository;
        this.taskService = taskService;
    }

    /**
     * Asynchronously handles a user message directed at BrickAI.
     * If the message mentions BrickAI, it sends the message to the AI and stores the response.
     *
     * @param userMessage The incoming user message.
     * @return A CompletableFuture containing the AI's outgoing message, or empty if not directed at AI.
     */
    @Async
    public CompletableFuture<Optional<OutgoingMessage>> handleAiResponse(ChatMessage userMessage) {
        boolean isMessageForAi = userMessage.getMentions()
                .stream()
                .anyMatch(mention -> brickAiService.isAiUser(mention.getMentionedUser()));
        if (!isMessageForAi && !Objects.equals(userMessage.getChannel().getName(), "brickAI")) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        ChatMessageWithFragments chatMessageWithFragments = processAiRequest(userMessage);
        return CompletableFuture.completedFuture(Optional.of(new OutgoingMessage(
                chatMessageWithFragments.chatMessage,
                chatMessageWithFragments.fragments,
                true
        )));
    }

    /**
     * Processes the AI request and handles exceptions gracefully.
     *
     * @param userMessage The incoming user message.
     * @return The AI-generated response content.
     */
    private ChatMessageWithFragments processAiRequest(ChatMessage userMessage) {
        try {
            return handleInitialAiRequest(userMessage);
        } catch (Exception e) {
            logger.error("Error while processing AI response", e);
            return savePlainAiMessage(userMessage.getChannel(),  RESPONSE_FAILURE_MESSAGE);
        }
    }

    /**
     * Sends a prompt to the AI with the given content and context.
     *
     * @param content       The user's message content.
     * @param systemMessage The system prompt to use.
     * @param channel       The chat channel.
     * @return The AI's structured response.
     */
    private AiResponse sendAiRequest(
            String content,
            Message systemMessage,
            ChatChannel channel
    ) {
        Message userMessage = new UserMessage("Incoming message: " + content);
        List<Message> messages = List.of(systemMessage, userMessage);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        logger.debug("Messages sent to AI:");
        prompt.getInstructions().forEach(m -> {
            logger.debug(" - [{}] {}", m.getMessageType(), m.getText());
        });

        try {
            return chatClient
                    .prompt(prompt)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, channel.getId()))
                    .call()
                    .entity(AiResponse.class);
        } catch (Exception e) {
            logger.warn("Primary AI model failed, falling back to secondary model.");
            try {
                Prompt fallbackPrompt = new Prompt(messages, ChatOptions.builder()
                        .model("gemini-2.5-flash-lite")
                        .build());
                return chatClient
                        .prompt(fallbackPrompt)
                        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, channel.getId()))
                        .call()
                        .entity(AiResponse.class);
            }  catch (Exception ex) {
                logger.error("Fallback AI model also failed", ex);
                throw ex;
            }
        }
    }

    /**
     * Handles the initial AI request and determines whether chat history is needed.
     *
     * @param userMessage The incoming user message.
     * @return The AI's response content.
     */
    private ChatMessageWithFragments handleInitialAiRequest(ChatMessage userMessage) {
        ChatChannel channel = userMessage.getChannel();
        List<RenovationMember> renovationMembers = renovationService.getRenovationMembers(channel.getRenovation());
        RenovationAiView renovationAiView = new RenovationAiView(channel.getRenovation(), renovationMembers);
        ChatMessageAiView chatMessageAiView = new ChatMessageAiView(userMessage);
        Message systemPrompt = aiPromptConfig.getSystemPromptTemplateInitial(renovationAiView);
        AiResponse response = sendAiRequest(
                "Incoming message: " + chatMessageAiView,
                systemPrompt,
                channel
        );

        return switch (response.getType()) {
            case AiResponseType.MESSAGE -> savePlainAiMessage(channel, ((AiResponseMessage) response).getContent());
            case AiResponseType.REQUIRE_CHAT_CONTEXT -> handleChatContextRequest(channel);
            case AiResponseType.TASK_CREATION -> handleTaskCreation((AiResponseTaskCreation) response, channel);
        };
    }

    /**
     * Handles AI requests that require chat history context.
     * Retrieves recent messages and sends them to the AI for follow-up.
     *
     * @param channel The chat channel.
     * @return The AI's response based on chat history, or fallback message.
     */
    private ChatMessageWithFragments handleChatContextRequest(ChatChannel channel) {
        List<ChatMessageAiView> messages = chatMessageService.getLatestMessagesExcludingUser(channel.getId(), brickAiService.getAiUser(), 20)
                .stream()
                .map(ChatMessageAiView::new)
                .toList();
        Message systemPrompt = aiPromptConfig.getSystemPromptTemplateWithChatContext(messages);
        AiResponse contextResponse = sendAiRequest(
                CHAT_CONTEXT_FOLLOWUP,
                systemPrompt,
                channel
        );

        // sometimes the AI bugs out and asks for chat context again
        // to fail gracefully, return a message when this happens
        if (contextResponse.getType() == AiResponseType.MESSAGE) {
            return savePlainAiMessage(channel, ((AiResponseMessage) contextResponse).getContent());
        }

        logger.warn("AI was unable to produce a message response after given chat context");
        return savePlainAiMessage(channel, NO_INFO_FOUND_MESSAGE);
    }

    /**
     * Handles creating a task when the AI response is of the task creation format
     *
     * @param aiResponse AI response in the task creation format
     * @param channel the channel that the AI is responding in
     * @return the appropriate response if the task is/isn't created
     */
    private ChatMessageWithFragments handleTaskCreation(AiResponseTaskCreation aiResponse, ChatChannel channel) {
        logger.debug(aiResponse.toString());
        try {
            Task task = new Task(channel.getRenovation(), aiResponse.getName(), aiResponse.getDescription(), "house.png");
            task.setState(aiResponse.getState());
            taskService.addRoomsToTaskThroughRoomNames(task,aiResponse.getRooms());

            try {
                renovationService.saveTask(channel.getRenovation().getId(), task, aiResponse.getDate());
            } catch (TaskDetailsExceptions e) {
                if (!e.getDueDateErrorMessage().isEmpty()) {
                    return savePlainAiMessage(channel, TASK_INCORRECT_DATE_MESSAGE);
                }
            }

            String content = TASK_CREATED_MESSAGE;
            String taskLink = appConfig.getFullBaseUrl() + "/renovation/" + channel.getRenovation().getId() + "/tasks/" + task.getId();
            List<ChatMessageFragment> fragments = List.of(
                    new ChatMessageFragmentText(content),
                    new ChatMessageFragmentLink(taskLink, aiResponse.getName()));
            ChatMessage chatMessage = saveAiMessage(channel, content + task.getName());
            chatMessage.getLinks().add(new ChatLink(chatMessage, task.getName(), taskLink, content.length(), content.length() + task.getName().length() - 1));
            chatMessageRepository.save(chatMessage);
            return new ChatMessageWithFragments(chatMessage, fragments);
        } catch (Exception e) {
            logger.error("Task creation failed", e);
            return savePlainAiMessage(channel, TASK_CREATION_FAILED_MESSAGE);
        }
    }

    private ChatMessageWithFragments savePlainAiMessage(ChatChannel channel, String content) {
        ChatMessage chatMessage = saveAiMessage(channel, content);
        return new ChatMessageWithFragments(chatMessage, List.of(new ChatMessageFragmentText(content)));
    }

    private ChatMessage saveAiMessage(ChatChannel channel, String content) {
        User aiUser = brickAiService.getAiUser();
        return chatMessageService.saveMessage(
                channel.getId(),
                aiUser.getId(),
                content,
                List.of());
    }

    public record ChatMessageWithFragments(ChatMessage chatMessage, List<ChatMessageFragment> fragments) {

    }
}
