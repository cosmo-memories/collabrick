package nz.ac.canterbury.seng302.homehelper.integration.service.ai;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatChannelDetailsException;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatUserAlreadyMemberException;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.InvitationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BrickAiService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class BrickAiServiceTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrickAiService brickAiService;

    @Autowired
    private RenovationService renovationService;

    @Autowired
    private InvitationService invitationService;

    private Renovation renovation;
    private User user;
    private User user2;
    @Autowired
    private ChatChannelService chatChannelService;

    @BeforeEach
    void setUp() {
        brickAiService.createAiUser();
        user = new User("Testy", "Testerson", "testy@test.com", "Abc123!!", "Abc123!!");
        userRepository.save(user);
        user2 = new User("Some", "Guy", "someguy@test.com", "Abc123!!", "Abc123!!");
        userRepository.save(user2);

        renovation = new Renovation("Test Reno", "Test Description");
        renovationService.createRenovation(renovation, Collections.emptyList(), user);
    }

    @Test
    void findAiChannel_OwnersReno_ChannelExists() {
        assertTrue(brickAiService.getAiChannel(renovation, user).isPresent());
    }

    @Test
    void findAiChannel_OwnersReno_ChannelHasCorrectDetails() throws RuntimeException {
        ChatChannel channel = brickAiService.getAiChannel(renovation, user).orElseThrow(() -> new RuntimeException("Channel not found"));
        assertEquals(renovation, channel.getRenovation());
        assertTrue(channel.getMembers().contains(user));
        assertTrue(channel.getMembers().contains(brickAiService.getAiUser()));
        assertEquals(2, channel.getMembers().size());
    }

    @Test
    void findAiChannel_UserNotAMemberOfRenovation_ChannelNotFound() {
        Optional<ChatChannel> channel = brickAiService.getAiChannel(renovation, user2);
        assertTrue(channel.isEmpty());
    }

    @Test
    void findAiChannel_UserInvitedToRenovation_ChannelExists() {
        Invitation invite = invitationService.createInvite(user2.getEmail(), renovation);
        invitationService.acceptInvitation(invite);

        Optional<ChatChannel> channel = brickAiService.getAiChannel(renovation, user2);
        assertTrue(channel.isPresent());
    }

    @Test
    void findAiChannel_UserInvitedToRenovation_ChannelHasCorrectDetails() {
        Invitation invite = invitationService.createInvite(user2.getEmail(), renovation);
        invitationService.acceptInvitation(invite);

        ChatChannel channel = brickAiService.getAiChannel(renovation, user2).orElseThrow(() -> new RuntimeException("Channel not found"));
        assertEquals(renovation, channel.getRenovation());
        assertTrue(channel.getMembers().contains(user2));
        assertTrue(channel.getMembers().contains(brickAiService.getAiUser()));
        assertEquals(2, channel.getMembers().size());
    }

    @Test
    void createAiChannel_ChannelDoesNotExist_CreatedSuccessfully() {
        // Adding a member directly like this doesn't (currently) create a channel automatically
        // Usually channels are created when the user accepts their invite to the renovation
        renovation.addMember(user2, RenovationMemberRole.MEMBER);
        Optional<ChatChannel> channel = brickAiService.getAiChannel(renovation, user2);
        assertTrue(channel.isEmpty());

        // So we can create a channel manually and confirm it exists
        assertDoesNotThrow(() -> brickAiService.createAiChannel(renovation, user2));
        assertTrue(brickAiService.getAiChannel(renovation, user2).isPresent());
    }

    @Test
    void createAiChannel_ChannelAlreadyExists_ChannelNotCreated() {
        assertThrows(ChatChannelDetailsException.class, () -> brickAiService.createAiChannel(renovation, user));
    }

    @Test
    void createAiChannel_UserNotMemberOfRenovation_ChannelNotCreated() {
        assertThrows(ChatChannelDetailsException.class, () -> brickAiService.createAiChannel(renovation, user2));
        Optional<ChatChannel> channel = brickAiService.getAiChannel(renovation, user2);
        assertTrue(channel.isEmpty());
    }

    @Test
    void createAiChannel_UserDeclinesInvite_ChannelNotCreated() {
        Invitation invite = invitationService.createInvite(user2.getEmail(), renovation);
        invitationService.declineInvitation(invite);

        Optional<ChatChannel> channel = brickAiService.getAiChannel(renovation, user2);
        assertTrue(channel.isEmpty());
    }

    @Test
    void addUserToAiChannel_UserAlreadyAMemberOfChannel_NotAdded() {
        ChatChannel channel = brickAiService.getAiChannel(renovation, user).orElseThrow(() -> new RuntimeException("Channel not found"));
        assertThrows(ChatUserAlreadyMemberException.class, () -> chatChannelService.addMemberToChatChannel(channel.getId(), user.getId()));
        assertEquals(2, channel.getMembers().size());
    }

}
