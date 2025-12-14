package nz.ac.canterbury.seng302.homehelper.integration.repository.chat;

import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMessage;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatChannelRepository;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatMessageRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class ChatMessageRepositoryTests {

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private ChatChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    private User user;
    private Renovation renovation;
    private ChatChannel channel;

    @BeforeEach
    void setup() {
        user = new User("John", "Smith", "john@gmail.com", "pass", "pass");
        user = userRepository.save(user);

        renovation = new Renovation("Reno", "Desc");
        renovation.setOwner(user);
        renovation = renovationRepository.save(renovation);

        channel = new ChatChannel("general", renovation);
        channel = channelRepository.save(channel);
    }

    @Test
    void testFindLatestMessages_GivenChannelWithNoMessages_ReturnsEmptyList() {
        List<ChatMessage> latestMessages = messageRepository.findLatestMessages(channel.getId(), 50);
        assertTrue(latestMessages.isEmpty());
    }

    @Test
    void testFindLatestMessage_GivenValidChannel_ThenReturnsMessagesInDescendingOrder() {
        Instant now = Instant.now();
        createMessages(1,
                5,
                i -> "Message " + i,
                i -> now.plus(i, ChronoUnit.MINUTES));

        List<ChatMessage> latestMessages = messageRepository.findLatestMessages(channel.getId(), 50);
        List<String> expectedOrder = List.of("Message 5", "Message 4", "Message 3", "Message 2", "Message 1");
        assertEquals(expectedOrder.size(), latestMessages.size());
        assertEquals(expectedOrder, latestMessages.stream().map(ChatMessage::getContent).toList());
    }

    @Test
    void testFindLatestMessages_GivenSameTimestampsDifferentIds_ReturnsCorrectOrderByIdDescending() {
        Instant now = Instant.now();
        createMessages(1,
                5,
                i -> "Message " + i,
                i -> now.plus(i, ChronoUnit.MINUTES));

        List<ChatMessage> chatMessages = messageRepository.findLatestMessages(channel.getId(), 50);
        List<String> expectedOrder = List.of("Message 5", "Message 4", "Message 3", "Message 2", "Message 1");
        assertEquals(expectedOrder.size(), chatMessages.size());
        assertEquals(expectedOrder, chatMessages.stream().map(ChatMessage::getContent).toList());
    }

    @Test
    void testFindLatestMessages_GivenChannelWithManyMessages_ReturnsLatestOnlyUpToLimit() {
        Instant now = Instant.now();
        createMessages(1,
                50,
                i -> "Message " + i,
                i -> now.plus(i, ChronoUnit.MINUTES));
        List<ChatMessage> expectedMessages = createMessages(51,
                100,
                i -> "Message " + i,
                i -> now.plus(i, ChronoUnit.MINUTES));


        List<ChatMessage> latestMessages = messageRepository.findLatestMessages(channel.getId(), 50);
        assertEquals(50, latestMessages.size());
        assertTrue(expectedMessages.containsAll(latestMessages));
    }

    @Test
    void testFindPreviousMessages_GivenChannelWithNoMessages_ReturnsEmptyList() {
        List<ChatMessage> latestMessages = messageRepository.findPreviousMessages(channel.getId(), Instant.now(), 0, PageRequest.of(0, 50));
        assertTrue(latestMessages.isEmpty());
    }

    @Test
    void testFindPreviousMessages_GivenChannelWithNoOlderMessages_ReturnsEmptyList() {
        Instant now = Instant.now();
        ChatMessage topMessage = createMessages(1,
                50,
                i -> "Message " + i,
                i -> now.plus(i, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS)
        ).getFirst();


        List<ChatMessage> latestMessages = messageRepository.findPreviousMessages(
                channel.getId(),
                topMessage.getTimestamp(),
                topMessage.getId(),
                PageRequest.of(0, 50));

        latestMessages.forEach(chatMessage -> System.out.println(chatMessage.toString()));
        assertTrue(latestMessages.isEmpty());
    }

    @Test
    void testFindTop6Messages_GivenChannelWithManyMessages_ReturnsLatest6BeforeOrAtTimestamp() {
        Instant now = Instant.now();
        createMessages(1, 10,
                i -> "Message " + i,
                i -> now.plus(i, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS));

        Instant timestamp = now.plus(10, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);

        List<ChatMessage> messages = messageRepository
                .findTop6ByChannelIdAndTimestampLessThanEqualOrderByTimestampDesc(channel.getId(), timestamp);

        assertEquals(6, messages.size());
        List<String> expected = List.of("Message 10", "Message 9", "Message 8", "Message 7", "Message 6", "Message 5");
        assertEquals(expected, messages.stream().map(ChatMessage::getContent).toList());
    }

    @Test
    void testFindTop10Messages_GivenChannelWithManyMessages_ReturnsLatest10BeforeOrAtTimestamp() {
        Instant now = Instant.now();
        createMessages(1, 50,
                i -> "Message " + i,
                i -> now.plus(i, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS));


        // Use timestamp at Message 15
        Instant timestamp = now.plus(15, ChronoUnit.MINUTES);

        List<ChatMessage> messages = messageRepository
                .findTop10ByChannelIdAndTimestampLessThanEqualOrderByTimestampDesc(channel.getId(), timestamp);

        assertEquals(10, messages.size());
        List<String> expected = IntStream.iterate(15, i -> i >= 6, i -> i - 1)
                .mapToObj(i -> "Message " + i)
                .toList();


        assertEquals(expected, messages.stream().map(ChatMessage::getContent).toList());
    }

    @Test
    void testFindTop5MessagesAfterTimestamp_GivenMessages_ReturnsOldest5AfterTimestamp() {
        Instant now = Instant.now();
        createMessages(1, 50,
                i -> "Message " + i,
                i -> now.plus(i, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS));

        Instant timestamp = now.plus(5, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);

        List<ChatMessage> messages = messageRepository
                .findTop5ByChannelIdAndTimestampGreaterThanOrderByTimestampAsc(channel.getId(), timestamp);

        assertEquals(5, messages.size());
        List<String> expected = List.of("Message 6", "Message 7", "Message 8", "Message 9", "Message 10");
        assertEquals(expected, messages.stream().map(ChatMessage::getContent).toList());
    }

    @Test
    void testFindNextMessages_GivenTimestampAndMessageId_ReturnsMessagesAfterThatPoint() {
        Instant now = Instant.now();
        List<ChatMessage> allMessages = createMessages(1, 10,
                i -> "Message " + i,
                i -> now.plus(i, ChronoUnit.MINUTES));

        ChatMessage reference = allMessages.get(4); // Message 5
        List<ChatMessage> messages = messageRepository.findNextMessages(
                channel.getId(),
                reference.getTimestamp(),
                reference.getId(),
                PageRequest.of(0, 5)
        );

        assertEquals(5, messages.size());
        List<String> expected = List.of("Message 6", "Message 7", "Message 8", "Message 9", "Message 10");
        assertEquals(expected, messages.stream().map(ChatMessage::getContent).toList());
    }


    private List<ChatMessage> createMessages(int start, int end, Function<Integer, String> contentSupplier, Function<Integer, Instant> timestampSupplier) {
        List<ChatMessage> list = IntStream.rangeClosed(start, end)
                .mapToObj(i -> new ChatMessage(
                        contentSupplier.apply(i),
                        timestampSupplier.apply(i),
                        channel,
                        user))
                .toList();
        return Lists.newArrayList(messageRepository.saveAll(list));
    }


}
