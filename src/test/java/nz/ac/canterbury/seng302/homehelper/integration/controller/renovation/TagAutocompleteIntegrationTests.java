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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class TagAutocompleteIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagService tagService;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private ProfanityService profanityService;

    private User user;

    @BeforeEach
    void setUpDatabase() throws Exception {
        when(profanityService.containsProfanity(anyString())).thenReturn(false);

        renovationRepository.deleteAll();
        userRepository.deleteAll();

        String plaintextPassword = "Password123!";
        String encryptedPassword = (passwordEncoder.encode(plaintextPassword));
        user = new User("Test", "user", "user@test.com", encryptedPassword, encryptedPassword);
        user.setActivated(true);
        userRepository.save(user);

        Renovation renovationOne = new Renovation("Kitchen", "Floor Walls Island");
        renovationOne.setOwner(user);
        renovationRepository.save(renovationOne);

        Renovation renovationTwo = new Renovation("Bedroom", "Ceiling Walls");
        renovationTwo.setOwner(user);
        renovationRepository.save(renovationTwo);

        Renovation renovationThree = new Renovation("Bathroom", "Tiles Floor Walls");
        renovationThree.setOwner(user);
        renovationRepository.save(renovationThree);

        tagService.save(new Tag("New", renovationOne));
        tagService.save(new Tag("Fix", renovationOne));
        tagService.save(new Tag("Table", renovationOne));

        tagService.save(new Tag("New", renovationTwo));
        tagService.save(new Tag("Fix", renovationTwo));
        tagService.save(new Tag("Ceiling", renovationTwo));

        tagService.save(new Tag("Old", renovationThree));
        tagService.save(new Tag("Fix", renovationThree));
        tagService.save(new Tag("New Flooring", renovationThree));
    }

    @Test
    void userHasMultipleRenovationsWithTags_userSearchesForExactTag_returnCorrespondingTag() throws Exception {
        mockMvc.perform(get("/tagAutoCompleteRenovation").param("term", "Table")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$", hasItems("Table")));
    }

    @Test
    void userHasMultipleRenovationsWithTags_userSearchesForPartialTag_returnTagsThatMatch() throws Exception {
        mockMvc.perform(get("/tagAutoCompleteRenovation").param("term", "Ne")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$", hasItems("New", "New Flooring")));
    }

    @Test
    void userHasMultipleRenovationsWithTags_userLooksForTagThatDoesntExist_returnEmptyList() throws Exception {
        mockMvc.perform(get("/tagAutoCompleteRenovation").param("term", "Basement")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


}
