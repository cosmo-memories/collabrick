package nz.ac.canterbury.seng302.homehelper.integration.controller.renovation;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.ProfanityService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TagSearchIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagService tagService;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private ProfanityService profanityService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Renovation renovation;

    private MockHttpSession authenticationSession;

    @BeforeEach
    void setUpDatabase() throws Exception {
        when(profanityService.containsProfanity(any())).thenReturn(false); // prevent sending api requests
        renovationRepository.deleteAll();
        userRepository.deleteAll();

        String plaintextPassword = "Password123!";
        String encryptedPassword = (passwordEncoder.encode(plaintextPassword));
        User user = new User("Test", "user", "user@test.com", encryptedPassword, encryptedPassword);
        user.setActivated(true);
        userRepository.save(user);
        renovation = new Renovation("TestRenovationName", "TestRenovationDescription");
        renovation.setOwner(user);
        renovationRepository.save(renovation);
        tagService.save(new Tag("Kitchen", renovation));
        tagService.save(new Tag("Bathroom", renovation));
        tagService.save(new Tag("Bedroom", renovation));
        tagService.save(new Tag("Shower", renovation));
        tagService.save(new Tag("Light Bulb", renovation));

        MvcResult result = mockMvc.perform(post("/do_login")
                        .param("email", "user@test.com")
                        .param("password", "Password123!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        authenticationSession = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(authenticationSession);
        assertFalse(authenticationSession.isInvalid());
    }

    @Test
    void tagSearch_OneTag_TagReturned() throws Exception {
        mockMvc.perform(get("/tagAutoComplete")
                        .param("tag", "Kitchen")
                        .session(authenticationSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", contains("Kitchen")));
    }

    @Test
    void tagSearch_TwoTagsSubstrings_BothTagsReturned() throws Exception {
        mockMvc.perform(get("/tagAutoComplete")
                        .param("tag", "room")
                        .session(authenticationSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", containsInAnyOrder("Bathroom", "Bedroom")));
    }

    @Test
    void tagSearch_SearchWithDiffCaps_TagReturned() throws Exception {
        mockMvc.perform(get("/tagAutoComplete")
                        .param("tag", "shOwEr")
                        .session(authenticationSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", contains("Shower")));
    }

    @Test
    void tagSearch_NoMatchingTags_NoTagReturned() throws Exception {
        mockMvc.perform(get("/tagAutoComplete")
                        .param("tag", "z")
                        .session(authenticationSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(empty())));
    }
}
