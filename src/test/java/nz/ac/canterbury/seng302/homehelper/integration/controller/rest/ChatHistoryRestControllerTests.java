package nz.ac.canterbury.seng302.homehelper.integration.controller.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.chat.OutgoingMessage;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatChannelRepository;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatMessageRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ChatHistoryRestControllerTests {

    @Autowired
    private ChatChannelService chatChannelService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatChannelRepository chatChannelRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private User user;
    private ChatChannel chatChannel;
    private Renovation renovation;

    @BeforeEach
    public void setUp() {
        chatMessageRepository.deleteAll();
        user = new User("James", "Li", "James@gmail.com", "password", "password");
        userRepository.save(user);
        renovation = new Renovation("Social Club", "IssaSocialClub");
        renovation.setOwner(user);
        renovationRepository.save(renovation);
        chatChannel = chatChannelService.createChannel(renovation, "general");
    }

    @Test
    public void testChatHistoryBlueSky() throws Exception {
        String messageBody = "Test Message";
        chatMessageService.saveMessage(chatChannel.getId(), user.getId(), messageBody, List.of());

        MvcResult result = mockMvc.perform(get("/chat/" + chatChannel.getId() + "/history")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<OutgoingMessage> messages = mapper.readValue(
                jsonResponse,
                new TypeReference<List<OutgoingMessage>>() {
                }
        );
        assertFalse(messages.isEmpty());
//        assertEquals(messageBody, messages.getFirst().content());
    }

    @Test
    public void getChatHistoryNoMessages() throws Exception {
        MvcResult result = mockMvc.perform(get("/chat/" + chatChannel.getId() + "/history")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<OutgoingMessage> messages = mapper.readValue(
                jsonResponse,
                new TypeReference<List<OutgoingMessage>>() {
                }
        );
        assertTrue(messages.isEmpty());
    }

    @Test
    public void getChatHistoryNotAMemberThrowsError() throws Exception {
        user = new User("Test", "Test Li", "123@gmail.com", "password", "password");
        userRepository.save(user);

        mockMvc.perform(get("/chat/" + chatChannel.getId() + "/history")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().is(404));
    }

    @Test
    public void getChatHistoryIsRenovationMemberAndChannelMember() throws Exception {
        user = new User("JustMember", "JustMember", "123@gmail.com", "password", "password");
        userRepository.save(user);
        renovation.addMember(user, RenovationMemberRole.MEMBER);
        renovationRepository.save(renovation);
        chatChannel.addMember(user);
        chatChannelRepository.save(chatChannel);

        String messageBody = "Test Message";
        chatMessageService.saveMessage(chatChannel.getId(), user.getId(), messageBody, List.of());

        MvcResult result = mockMvc.perform(get("/chat/" + chatChannel.getId() + "/history")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<OutgoingMessage> messages = mapper.readValue(
                jsonResponse,
                new TypeReference<>() {
                }
        );
        assertFalse(messages.isEmpty());
//        assertEquals(messageBody, messages.getFirst().content());
    }

    @Test
    public void getChatHistoryIsRenovationMemberAndNotChannelMember() throws Exception {
        user = new User("JustMember", "JustMember", "123@gmail.com", "password", "password");
        userRepository.save(user);
        renovation.addMember(user, RenovationMemberRole.MEMBER);
        renovationRepository.save(renovation);

        mockMvc.perform(get("/chat/" + chatChannel.getId() + "/history")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().is(404));
    }


}
