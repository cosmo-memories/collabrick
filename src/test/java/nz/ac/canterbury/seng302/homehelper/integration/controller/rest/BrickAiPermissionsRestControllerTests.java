package nz.ac.canterbury.seng302.homehelper.integration.controller.rest;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BrickAiPermissionsRestControllerTests {

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private UserRepository userRepository;

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
    }

    @Test
    public void setBrickAIPermissionsRenovation_NewRenovation_AIPermissionsTurnedOff() throws Exception {
        assertTrue(renovation.isAllowBrickAI());
        mockMvc.perform(post("/renovation/" + renovation.getId() + "/allowBrickAI")
                        .with(csrf())
                .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovation/" + renovation.getId()))
                .andReturn();
        Renovation renovation2 = renovationRepository.findById(renovation.getId()).orElse(null);
        assertNotNull(renovation2);
        assertFalse(renovation2.isAllowBrickAI());
    }

    @Test
    public void setBrickAIPermissionsRenovation_Renovation_AIPermissionsTurnedOn() throws Exception {
        renovation.setAllowBrickAI(false);
        assertFalse(renovation.isAllowBrickAI());
        mockMvc.perform(post("/renovation/" + renovation.getId() + "/allowBrickAI")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovation/" + renovation.getId()))
                .andReturn();
        Renovation renovation2 = renovationRepository.findById(renovation.getId()).orElse(null);
        assertNotNull(renovation2);
        assertTrue(renovation2.isAllowBrickAI());
    }

    @Test
    public void setBrickAIPermissionsUser_UserTurnsPermissionsOff_AIPermissionsTurnedOff() throws Exception {
        assertTrue(user.isAllowBrickAIChatAccess());
        mockMvc.perform(post("/userPage/allowBrickAI")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
        User user1 = userRepository.findUserById(user.getId()).getFirst();
        assertNotNull(user1);
        assertFalse(user1.isAllowBrickAIChatAccess());
    }

    @Test
    public void setBrickAIPermissionsUser_UserTurnsPermissionsOn_AIPermissionsTurnedOn() throws Exception {
        user.setAllowBrickAIChatAccess(false);
        assertFalse(user.isAllowBrickAIChatAccess());
        mockMvc.perform(post("/userPage/allowBrickAI")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
        User user1 = userRepository.findUserById(user.getId()).getFirst();
        assertNotNull(user1);
        assertTrue(user1.isAllowBrickAIChatAccess());
    }
}
