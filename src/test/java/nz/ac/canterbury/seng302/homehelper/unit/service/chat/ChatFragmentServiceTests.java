package nz.ac.canterbury.seng302.homehelper.unit.service.chat;

import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMention;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMessage;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragment;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentMention;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentText;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentType;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatMentionRepository;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatFragmentService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatMentionService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChatFragmentServiceTests {

    @Mock
    private ChatMessage chatMessage;

    private final ChatFragmentService chatFragmentService = new ChatFragmentService();

    @Test
    void testExtractFragmentsFromMessage_WhenMentionsListIsEmpty_ThenReturnsFullTextFragment() {
        String content = "Hi, how are you!";
        when(chatMessage.getContent()).thenReturn(content);
        when(chatMessage.getMentions()).thenReturn(listOf());

        List<ChatMessageFragment> fragments = chatFragmentService.extractFragmentsFromMessage(chatMessage);

        assertThat(fragments, hasSize(1));
        assertThat(fragments.getFirst(), textFragment(content));
    }

    @Test
    void testExtractFragmentsFromMessage_WhenMentionCoversEntireMessage_ThenReturnsSingleMentionFragment() {
        String content = "@Jeremy Joe";
        User userJeremyJoe = new User(1, "Jeremy", "Joe");
        ChatMention chatMention = new ChatMention(chatMessage, userJeremyJoe, 0, 10);
        when(chatMessage.getContent()).thenReturn(content);
        when(chatMessage.getMentions()).thenReturn(listOf(chatMention));

        List<ChatMessageFragment> fragments = chatFragmentService.extractFragmentsFromMessage(chatMessage);

        assertThat(fragments, hasSize(1));
        assertThat(fragments.getFirst(), mentionFragment(userJeremyJoe));
    }

    @Test
    void testExtractFragmentsFromMessage_WhenSingleMentionAtStart_ThenReturnsMentionAndTrailingText() {
        String content = "@Jeremy Joe, how are you?";
        User userJeremyJoe = new User(1, "Jeremy", "Joe");
        ChatMention chatMention = new ChatMention(chatMessage, userJeremyJoe, 0, 10);
        when(chatMessage.getContent()).thenReturn(content);
        when(chatMessage.getMentions()).thenReturn(listOf(chatMention));

        List<ChatMessageFragment> fragments = chatFragmentService.extractFragmentsFromMessage(chatMessage);

        assertThat(fragments, hasSize(2));
        assertThat(fragments.getFirst(), mentionFragment(userJeremyJoe));
        assertThat(fragments.get(1), textFragment(", how are you?"));
    }

    @Test
    void testExtractFragmentsFromMessage_WhenSingleMentionAtEnd_ThenReturnsLeadingTextAndMention() {
        String content = "Hello @Jeremy Joe";
        User userJeremyJoe = new User(1, "Jeremy", "Joe");
        ChatMention chatMention = new ChatMention(chatMessage, userJeremyJoe, 6, 16);
        when(chatMessage.getContent()).thenReturn(content);
        when(chatMessage.getMentions()).thenReturn(listOf(chatMention));

        List<ChatMessageFragment> fragments = chatFragmentService.extractFragmentsFromMessage(chatMessage);

        assertThat(fragments, hasSize(2));
        assertThat(fragments.getFirst(), textFragment("Hello "));
        assertThat(fragments.get(1), mentionFragment(userJeremyJoe));
    }

    @Test
    void testExtractFragmentsFromMessage_WhenMultipleMentions_ThenReturnsInterleavedTextAndMentions() {
        String content = "Hey @Jeremy Joe, have you met @Charlie Bob? We're all going to @Jane Luke's place later.";
        User userJeremyJoe = new User(1, "Jeremy", "Joe");
        User userCharlieBob = new User(2, "Charlie", "Bob");
        User userJaneLuke = new User(2, "Jane", "Luke");
        ChatMention mentionJeremyJoe = new ChatMention(chatMessage, userJeremyJoe, 4, 14);
        ChatMention mentionCharlieBob = new ChatMention(chatMessage, userCharlieBob, 30, 41);
        ChatMention mentionJaneLuke = new ChatMention(chatMessage, userJaneLuke, 63, 72);
        when(chatMessage.getContent()).thenReturn(content);
        when(chatMessage.getMentions()).thenReturn(listOf(mentionJeremyJoe, mentionCharlieBob, mentionJaneLuke));

        List<ChatMessageFragment> fragments = chatFragmentService.extractFragmentsFromMessage(chatMessage);

        assertThat(fragments, hasSize(7));
        assertThat(fragments.getFirst(), textFragment("Hey "));
        assertThat(fragments.get(1), mentionFragment(userJeremyJoe));
        assertThat(fragments.get(2), textFragment(", have you met "));
        assertThat(fragments.get(3), mentionFragment(userCharlieBob));
        assertThat(fragments.get(4), textFragment("? We're all going to "));
        assertThat(fragments.get(5), mentionFragment(userJaneLuke));
        assertThat(fragments.get(6), textFragment("'s place later."));
    }

    @Test
    void testExtractFragmentsFromMessage_WhenMultipleMentionsNotInCorrectOrder_ThenReturnsInterleavedTextAndMentionsInCorrectOrder() {
        String content = "Hey @Jeremy Joe, have you met @Charlie Bob? We're all going to @Jane Luke's place later.";
        User userJeremyJoe = new User(1, "Jeremy", "Joe");
        User userCharlieBob = new User(2, "Charlie", "Bob");
        User userJaneLuke = new User(2, "Jane", "Luke");
        ChatMention mentionJeremyJoe = new ChatMention(chatMessage, userJeremyJoe, 4, 14);
        ChatMention mentionCharlieBob = new ChatMention(chatMessage, userCharlieBob, 30, 41);
        ChatMention mentionJaneLuke = new ChatMention(chatMessage, userJaneLuke, 63, 72);
        when(chatMessage.getContent()).thenReturn(content);
        when(chatMessage.getMentions()).thenReturn(listOf(mentionCharlieBob, mentionJaneLuke, mentionJeremyJoe));

        List<ChatMessageFragment> fragments = chatFragmentService.extractFragmentsFromMessage(chatMessage);

        assertThat(fragments, hasSize(7));
        assertThat(fragments.getFirst(), textFragment("Hey "));
        assertThat(fragments.get(1), mentionFragment(userJeremyJoe));
        assertThat(fragments.get(2), textFragment(", have you met "));
        assertThat(fragments.get(3), mentionFragment(userCharlieBob));
        assertThat(fragments.get(4), textFragment("? We're all going to "));
        assertThat(fragments.get(5), mentionFragment(userJaneLuke));
        assertThat(fragments.get(6), textFragment("'s place later."));
    }

    @Test
    void testExtractFragmentsFromMessage_WhenMentionsAreAdjacent_ThenReturnsConsecutiveMentionFragments() {
        String content = "@Jeremy Joe@Charlie Bob";
        User userJeremyJoe = new User(1, "Jeremy", "Joe");
        User userCharlieBob = new User(2, "Charlie", "Bob");
        ChatMention mentionJeremyJoe = new ChatMention(chatMessage, userJeremyJoe, 0, 10);
        ChatMention mentionCharlieBob = new ChatMention(chatMessage, userCharlieBob, 11, 22);
        when(chatMessage.getContent()).thenReturn(content);
        when(chatMessage.getMentions()).thenReturn(listOf(mentionJeremyJoe, mentionCharlieBob));

        List<ChatMessageFragment> fragments = chatFragmentService.extractFragmentsFromMessage(chatMessage);

        assertThat(fragments.getFirst(), mentionFragment(userJeremyJoe));
        assertThat(fragments.get(1), mentionFragment(userCharlieBob));
    }

    /**
     * Creates a mutable List from a varargs array of elements.
     *
     * @param elements the elements to include in the list
     * @param <T> the type of elements
     * @return a mutable list containing the provided elements
     */
    @SafeVarargs
    private <T> List<T> listOf(T... elements) {
        return new ArrayList<>(List.of(elements));
    }

    /**
     * Constructs a Hamcrest Matcher that verifies a ChatMessageFragment is a ChatMessageFragmentText with
     * the expected text content.
     *
     * @param expectedText the expected text content of the fragment
     * @return a matcher that verifies type and text content
     */
    private Matcher<ChatMessageFragment> textFragment(String expectedText) {
        return allOf(
                instanceOf(ChatMessageFragmentText.class),
                hasProperty("type", is(ChatMessageFragmentType.TEXT)),
                hasProperty("text", is(expectedText))
        );
    }

    /**
     * Constructs a Hamcrest Matcher that verifies a ChatMessageFragment is a ChatMessageFragmentMention
     * referring to the given User.
     *
     * @param user the user expected to be mentioned in the fragment
     * @return a matcher that verifies type, mentioned user ID, and display name
     */
    private Matcher<ChatMessageFragment> mentionFragment(User user) {
        return allOf(
                instanceOf(ChatMessageFragmentMention.class),
                hasProperty("type", is(ChatMessageFragmentType.MENTION)),
                hasProperty("mentionedUserId", is(user.getId())),
                hasProperty("mentionedUserName", is(user.getFullName())),
                hasProperty("text", is("@" + user.getFullName()))
        );
    }
}
