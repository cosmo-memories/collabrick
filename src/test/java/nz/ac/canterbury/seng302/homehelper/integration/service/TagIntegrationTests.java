package nz.ac.canterbury.seng302.homehelper.integration.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.TagException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class TagIntegrationTests {

    @Autowired
    private TagService tagService;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private ProfanityService profanityService;

    @Autowired
    private MockMvc mockMvc;

    private Renovation renovation;

    @BeforeEach
    void setUpDatabase() {
        renovationRepository.deleteAll();
        User user = new User("Test", "user", "user@test.com", "Password123!", "Password123!");
        userRepository.save(user);
        renovation = new Renovation("TestRenovationName", "TestRenovationDescription");
        renovation.setOwner(user);
        renovationRepository.save(renovation);
        when(profanityService.containsProfanity(any())).thenReturn(false); // prevent sending api requests
    }

    @Test
    public void addTag_Success_CorrectRenovation() throws TagException {
        tagService.save(new Tag("TestTag", renovation));
        List<Tag> savedTags = tagService.getByTag("TestTag");
        assertEquals(renovation, savedTags.get(0).getRenovation());
    }

    @Test
    public void addTag_Success_CorrectText() throws TagException {
        tagService.save(new Tag("TestTag", renovation));
        List<Tag> savedTags = tagService.getByRenovationID(renovation.getId());
        assertEquals("TestTag", savedTags.get(0).getTag());
    }

    @Test
    public void removeTag_Success_DeletedSingleTag() throws TagException {
        tagService.save(new Tag("TestTag", renovation));
        List<Tag> savedTags = tagService.getByTag("TestTag");
        assertEquals(renovation, savedTags.get(0).getRenovation());

        tagService.remove(savedTags.get(0));
        List<Tag> updatedTags = tagService.getByTag("TestTag");
        assertTrue(updatedTags.isEmpty());
    }

    @Test
    public void removeTag_Success_CorrectResultSize() throws TagException {
        tagService.save(new Tag("TestOne", renovation));
        tagService.save(new Tag("TestTwo", renovation));
        tagService.save(new Tag("TestThree", renovation));
        List<Tag> savedTags = tagService.getByRenovationID(renovation.getId());
        assertEquals(3, savedTags.size());

        tagService.remove(savedTags.get(0));
        List<Tag> updatedTags = tagService.getByRenovationID(renovation.getId());
        assertEquals(2, updatedTags.size());
    }

    @Test
    public void removeTag_Success_CorrectRemainingTags() throws TagException {
        tagService.save(new Tag("TestOne", renovation));
        tagService.save(new Tag("TestTwo", renovation));
        tagService.save(new Tag("TestThree", renovation));
        List<Tag> savedTags = tagService.getByRenovationID(renovation.getId());
        assertEquals(3, savedTags.size());

        tagService.remove(savedTags.get(0));
        List<Tag> updatedTags = tagService.getByRenovationID(renovation.getId());
        assertTrue(updatedTags.contains(savedTags.get(1)) && updatedTags.contains(savedTags.get(2)));
    }

    @Test
    public void addTag_OneLetterAtLeast_SavesTag() throws TagException {
        tagService.save(new Tag("123A?1>", renovation));
        List<Tag> savedTags = tagService.getByRenovationID(renovation.getId());
        assertEquals(1, savedTags.size());
        assertEquals("123A?1>", savedTags.get(0).getTag());
    }

    @Test
    public void addTag_32CharTag_SavesTag() throws TagException {
        tagService.save(new Tag("qwertyuiopasdfghjklzxcvbnmqwerty", renovation));
        List<Tag> savedTags = tagService.getByRenovationID(renovation.getId());
        assertEquals(1, savedTags.size());
        assertEquals("qwertyuiopasdfghjklzxcvbnmqwerty", savedTags.get(0).getTag());
    }

    @Test
    public void addTag_OnlyNums_ThrowsValidationMessage() {
        Tag invalid = new Tag("123456", renovation);
        TagException e = assertThrows(TagException.class, () -> tagService.save(invalid));
        assertEquals("Tag must contain at least one letter and have a maximum length of 32 characters", e.getMessage()
        );
    }

    @Test
    public void addTag_OnlySpecialChars_ThrowsValidationMessage() {
        Tag invalid = new Tag("/?<>.{}", renovation);
        TagException e = assertThrows(TagException.class, () -> tagService.save(invalid));
        assertEquals("Tag must contain at least one letter and have a maximum length of 32 characters", e.getMessage()
        );
    }

    @Test
    public void addTag_NumsAndSpecialChars_ThrowsValidationMessage() {
        Tag invalid = new Tag("123/?!", renovation);
        TagException e = assertThrows(TagException.class, () -> tagService.save(invalid));
        assertEquals("Tag must contain at least one letter and have a maximum length of 32 characters", e.getMessage()
        );
    }

    @Test
    public void addTag_TagToLong_ThrowsValidationMessage() {
        Tag invalid = new Tag("qwertyuiopasdfghjklzxcvbnmqwertyu", renovation);
        TagException e = assertThrows(TagException.class, () -> tagService.save(invalid));
        assertEquals("Tag must contain at least one letter and have a maximum length of 32 characters", e.getMessage()
        );
    }

    @Test
    public void addTag_TagToShort_ThrowsValidationMessage() {
        Tag invalid = new Tag("", renovation);
        TagException e = assertThrows(TagException.class, () -> tagService.save(invalid));
        assertEquals("Tag must contain at least one letter and have a maximum length of 32 characters", e.getMessage()
        );
    }

    @Test
    public void save_TooManyTags_ThrowsMaxTagsMessage() throws TagException {
        tagService.save(new Tag("One", renovation));
        tagService.save(new Tag("Two", renovation));
        tagService.save(new Tag("Three", renovation));
        tagService.save(new Tag("Four", renovation));
        tagService.save(new Tag("Five", renovation));
        TagException e = assertThrows(TagException.class, () -> tagService.save(new Tag("Six", renovation)));
        assertEquals("Renovations can't have more than 5 tags.", e.getMessage());
    }

    @Test
    public void save_DuplicateTag_ThrowsDuplicateMessage() throws TagException {
        Tag original = new Tag("DupTag", renovation);
        tagService.save(original);
        TagException e = assertThrows(TagException.class, () -> tagService.save(new Tag("DupTag", renovation)));
        assertEquals("Can't create a duplicate tag.", e.getMessage());
    }

    @Test
    public void save_InappropriateLanguageNotMalformed_ThrowsLanguageStandardsMessage() throws Exception {
        List<User> users = userRepository.findByEmail("user@test.com");
        User mockUser = users.get(0);

        List<Renovation> renovations = renovationRepository.findByNameAndUser("TestRenovationName", mockUser);
        Renovation testRenovation = renovations.get(0);

        when(profanityService.containsProfanity("fuck")).thenReturn(true);

        mockMvc.perform(post("/myRenovations/{id}", testRenovation.getId())
                        .param("tagName", "fuck")
                        .header("Referer", "/myRenovations/" + testRenovation.getId())
                        .with(csrf())
                        .with(user(String.valueOf(mockUser.getId())).password(mockUser.getPassword()).roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/myRenovations/" + testRenovation.getId()))
                .andExpect(flash().attribute("tagError", "Tag does not follow the system language standards"));
    }

    @Test
    public void save_InappropriateLanguageMalformed_ThrowsLanguageStandardsMessage() throws Exception {
        List<User> users = userRepository.findByEmail("user@test.com");
        User mockUser = users.get(0);

        List<Renovation> renovations = renovationRepository.findByNameAndUser("TestRenovationName", mockUser);
        Renovation testRenovation = renovations.get(0);

        when(profanityService.containsProfanity("fück")).thenReturn(true);

        mockMvc.perform(post("/myRenovations/{id}", testRenovation.getId())
                        .param("tagName", "fück")
                        .header("Referer", "/myRenovations/" + testRenovation.getId())
                        .with(csrf())
                        .with(user(String.valueOf(mockUser.getId())).password(mockUser.getPassword()).roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/myRenovations/" + testRenovation.getId()))
                .andExpect(flash().attribute("tagError", "Tag does not follow the system language standards"));
    }

    @Test
    public void search_PublicRenovationsWithTags_FindsRenovations() throws Exception {
        List<User> users = userRepository.findByEmail("user@test.com");
        User mockUser = users.get(0);

        Renovation testRenovationOne = new Renovation("Reno1", "Description");
        testRenovationOne.setPublic(true);
        testRenovationOne.setOwner(mockUser);

        Renovation testRenovationTwo = new Renovation("Reno2", "Description");
        testRenovationTwo.setPublic(true);
        testRenovationTwo.setOwner(mockUser);

        Renovation testRenovationThree = new Renovation("Reno3", "Description");
        testRenovationThree.setPublic(false);
        testRenovationThree.setOwner(mockUser);

        renovationRepository.save(testRenovationOne);
        renovationRepository.save(testRenovationTwo);
        renovationRepository.save(testRenovationThree);

        tagService.save(new Tag("kitchen", testRenovationOne));
        tagService.save(new Tag("tile", testRenovationOne));
        tagService.save(new Tag("modern", testRenovationOne));

        tagService.save(new Tag("kitchen", testRenovationTwo));

        tagService.save(new Tag("kitchen", testRenovationThree));
        tagService.save(new Tag("tile", testRenovationThree));
        tagService.save(new Tag("modern", testRenovationThree));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Renovation> tagSearchResult = tagService.getPublicRenovationsByTags(
                List.of("kitchen", "tile", "modern"),
                "",
                pageable);

        assertEquals(1, tagSearchResult.getTotalElements());
        assertEquals("Reno1", tagSearchResult.getContent().get(0).getName());
    }

    @Test
    public void search_PublicRenovationsWithTags_FindRenovationsWithDifferingTag() throws Exception {
        List<User> users = userRepository.findByEmail("user@test.com");
        User mockUser = users.get(0);

        Renovation testRenovationOne = new Renovation("Reno1", "Description");
        testRenovationOne.setPublic(true);
        testRenovationOne.setOwner(mockUser);
        testRenovationOne.setCreatedTimestamp(LocalDateTime.of(2024, 1, 5, 5, 1));

        Renovation testRenovationTwo = new Renovation("Reno2", "Description");
        testRenovationTwo.setPublic(true);
        testRenovationTwo.setOwner(mockUser);
        testRenovationTwo.setCreatedTimestamp(LocalDateTime.of(2025, 1, 5, 5, 1));

        renovationRepository.save(testRenovationOne);
        renovationRepository.save(testRenovationTwo);

        tagService.save(new Tag("kitchen", testRenovationOne));
        tagService.save(new Tag("tile", testRenovationOne));
        tagService.save(new Tag("modern", testRenovationOne));

        tagService.save(new Tag("kitchen", testRenovationTwo));
        tagService.save(new Tag("tile", testRenovationTwo));
        tagService.save(new Tag("rustic", testRenovationTwo));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Renovation> tagSearchResult = tagService.getPublicRenovationsByTags(
                List.of("kitchen", "tile"),
                "",
                pageable);


        assertEquals(2, tagSearchResult.getTotalElements());
        assertEquals("Reno2", tagSearchResult.getContent().get(0).getName());
        assertEquals("Reno1", tagSearchResult.getContent().get(1).getName());
    }

    @Test
    public void search_PublicRenovationsWithTags_FindNoRenovations() throws Exception {
        List<User> users = userRepository.findByEmail("user@test.com");
        User mockUser = users.get(0);

        Renovation testRenovationOne = new Renovation("Reno1", "Description");
        testRenovationOne.setPublic(true);
        testRenovationOne.setOwner(mockUser);

        renovationRepository.save(testRenovationOne);

        tagService.save(new Tag("kitchen", testRenovationOne));
        tagService.save(new Tag("tile", testRenovationOne));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Renovation> tagSearchResult = tagService.getPublicRenovationsByTags(
                List.of("kitchen", "tile", "modern"),
                "",
                pageable);

        assertEquals(0, tagSearchResult.getTotalElements());
    }

    @Test
    public void search_PublicRenovationsWithTags_NoPublicRenovations() throws Exception {
        List<User> users = userRepository.findByEmail("user@test.com");
        User mockUser = users.get(0);

        Renovation testRenovationOne = new Renovation("Reno1", "Description");
        testRenovationOne.setPublic(false);
        testRenovationOne.setOwner(mockUser);

        renovationRepository.save(testRenovationOne);

        tagService.save(new Tag("kitchen", testRenovationOne));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Renovation> tagSearchResult = tagService.getPublicRenovationsByTags(
                List.of("kitchen"),
                "",
                pageable);

        assertEquals(0, tagSearchResult.getTotalElements());
    }
}
