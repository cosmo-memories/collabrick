package nz.ac.canterbury.seng302.homehelper.integration.controller.rest;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatChannelRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ChatControllerTests {

    @Autowired
    private ChatChannelService chatChannelService;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatChannelRepository chatChannelRepository;

    @Autowired
    private MockMvc mockMvc;

    private User user;
    private Renovation renovation;

    @BeforeEach
    public void setUp() {
        user = new User("Test", "User", "test@user.email", "Abc123!", "Abc123!!");
        userRepository.save(user);
        renovation = new Renovation("Test Renovation", "Test Description");
        renovation.setOwner(user);
        renovationRepository.save(renovation);
        chatChannelService.createChannel(renovation, "general");
    }

    @Test
    public void createChat_ValidName_CreatedSuccessfully() throws Exception {
        MvcResult result = mockMvc.perform(post("/renovation/" + renovation.getId() + "/chat/new")
                        .param("channelName", "Test Channel")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String url = result.getResponse().getRedirectedUrl();
        Optional<ChatChannel> newChannel = chatChannelRepository.findByNameAndRenovation("Test Channel", renovation);

        assertTrue(newChannel.isPresent());
        assertEquals("/renovation/" + renovation.getId() + "/chat/" + newChannel.get().getId(), url);
    }

    @Test
    public void createChat_InvalidName_NotCreated() throws Exception {
        mockMvc.perform(post("/renovation/" + renovation.getId() + "/chat/new")
                        .param("channelName", "Invalid,Channel")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andReturn();

        Optional<ChatChannel> newChannel = chatChannelRepository.findByNameAndRenovation("Invalid,Channel", renovation);
        assertTrue(newChannel.isEmpty());
    }

}
