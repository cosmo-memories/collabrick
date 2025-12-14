package nz.ac.canterbury.seng302.homehelper.integration.controller.renovation;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.Pagination;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BrowseRenovationsIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RenovationService renovationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private TagService tagService;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = userRepository.save(new User("John", "Smith", "john.smith@gmail.com", "password", "password"));
        testUser2 = userRepository.save(new User("First", "Last", "t@e.st", "Abc123!!", "Abc123!!"));
    }

    @Test
    void testBrowseRenovations_WhenNoRenovations_EmptyList() throws Exception {
        MvcResult result = mockMvc.perform(get("/browse"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertTrue(pagination.getItems().isEmpty());
        assertEquals(1, pagination.getTotalPages());
    }

    /*ChatGPT helped write this test*/
    @Test
    void testBrowseRenovations_WhenPageVisited_AllRenovationsPublic() throws Exception {
        List<Renovation> createdRenovations = List.of(
                createRenovation(testUser1, "Renovation 1", "Description 1", true, LocalDateTime.of(2025, 1, 5, 5, 1)),
                createRenovation(testUser2, "Renovation 2", "Description 2", false, LocalDateTime.of(2025, 2, 10, 10, 30)),
                createRenovation(testUser1, "Renovation 3", "Description 3", true, LocalDateTime.of(2025, 3, 15, 8, 45)),
                createRenovation(testUser2, "Renovation 4", "Description 4", false, LocalDateTime.of(2025, 4, 20, 15, 20)),
                createRenovation(testUser1, "Renovation 5", "Description 5", true, LocalDateTime.of(2025, 5, 25, 11, 10)),
                createRenovation(testUser2, "Renovation 6", "Description 6", false, LocalDateTime.of(2025, 6, 30, 14, 55))
        );
        List<Renovation> publicRenovations = createdRenovations.stream()
                .filter(Renovation::getIsPublic)
                .toList();

        MvcResult result = mockMvc.perform(get("/browse"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertEquals(1, pagination.getTotalPages()); // make sure there is 1 page
        assertEquals(1, pagination.getCurrentPage()); // make sure we are on first page
        assertEquals(3, pagination.getItems().size()); // make sure there are 3 items
        assertTrue(pagination.getItems().containsAll(publicRenovations));

        // check they are ordered properly
        LocalDateTime lastDate = LocalDateTime.MAX;
        for (Renovation renovation : pagination.getItems()) {
            assertTrue(renovation.getIsPublic());
            assertTrue(lastDate.isAfter(renovation.getCreatedTimestamp()));
            lastDate = renovation.getCreatedTimestamp();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void testBrowseRenovations_WhenLessThanEqualToThreePublicRenovations_ShowOnePage(int numItems) throws Exception {
        // create renovations
        List<Renovation> renovations = IntStream.range(0, numItems)
                .mapToObj(i -> createRenovation(testUser1, "Renovation " + i, "Description " + i, true, LocalDateTime.now()))
                .toList();

        MvcResult result = mockMvc.perform(get("/browse"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertEquals(1, pagination.getTotalPages()); // make sure there is 1 page
        assertEquals(1, pagination.getCurrentPage()); // make sure we are on first page
        assertEquals(numItems, pagination.getItems().size()); // make sure there are 3 items
        assertTrue(pagination.getItems().containsAll(renovations)); // make sure items are correct
    }

    @ParameterizedTest
    @CsvSource({
            "4, 2",
            "6, 2",
            "7, 3",
            "9, 3"
    })
    void testBrowseRenovations_WhenSixPublicRenovations_ShowTwoPage(int numItems, int expectedNumPages) throws Exception {
        // create renovations
        List<Renovation> renovations = IntStream.range(0, numItems)
                .mapToObj(i -> createRenovation(testUser1, "Renovation " + i, "Description " + i, true, LocalDateTime.now()))
                .toList();

        MvcResult result = mockMvc.perform(get("/browse"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertEquals(expectedNumPages, pagination.getTotalPages()); // make sure there is "expectedNumPages" pages
        assertEquals(1, pagination.getCurrentPage()); // make sure we are on first page
        assertEquals(3, pagination.getItems().size()); // make sure there are 3 items
    }

    private Renovation createRenovation(User user, String name, String description, boolean isPublic, LocalDateTime date) {
        Renovation renovation = new Renovation(name, description);
        renovation.setOwner(user);
        renovation.setPublic(isPublic);
        renovation.setCreatedTimestamp(date);
        return renovationRepository.save(renovation);
    }

    @Test
    void browseRenovation_ValidIdOfPublicRenovation_ReturnRenovationPage() throws Exception {
        Renovation renovation = createRenovation(testUser1, "Renovation " + 1, "Description 1", true, LocalDateTime.now());
        mockMvc.perform(get("/renovation/" + renovation.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("renovation/layout"))
                .andExpect(model().attribute("contentType", "overview"));
    }

    @Test
    void browseRenovation_InvalidId_ReturnNotFoundPage() throws Exception {
        Renovation renovation = createRenovation(testUser1, "Renovation " + 2, "Description 2", false, LocalDateTime.now());
        mockMvc.perform(get("/renovation/" + renovation.getId() + 1))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name("pages/notFoundPage"));
    }

    @Test
    void testBrowseRenovations_WhenSearchNotLoggedIn_ShowPublicRenovationsMatchingSearch() throws Exception {
        List<Renovation> createdRenovations = List.of(
                createRenovation(testUser1, "Modern Kitchen", "Description 1", true, LocalDateTime.of(2025, 1, 5, 5, 1)),
                createRenovation(testUser2, "Rustic Kitchen", "Description 2", false, LocalDateTime.of(2025, 2, 10, 10, 30)),
                createRenovation(testUser1, "Kitchen Renovation", "Description 3", true, LocalDateTime.of(2025, 3, 15, 8, 45)),
                createRenovation(testUser2, "Bathroom", "Description 4", true, LocalDateTime.of(2025, 4, 20, 15, 20)),
                createRenovation(testUser1, "Open living and dining", "Kitchen", true, LocalDateTime.of(2025, 5, 25, 11, 10))
        );
        List<Renovation> publicRenovations = createdRenovations.stream()
                .filter(Renovation::getIsPublic)
                .toList();


        MvcResult result = mockMvc.perform(get("/browse?search=kitchen"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertEquals(1, pagination.getTotalPages());
        assertEquals(1, pagination.getCurrentPage());
        assertEquals(3, pagination.getItems().size());

        LocalDateTime lastDate = LocalDateTime.MAX;
        for (Renovation renovation : pagination.getItems()) {
            assertTrue(renovation.getIsPublic());
            assertTrue(lastDate.isAfter(renovation.getCreatedTimestamp()));
            lastDate = renovation.getCreatedTimestamp();
        }
    }

    @Test
    void testBrowseRenovations_WhenSearchWithOneTagOnlyAndNotLoggedIn_ShowPublicRenovationsMatchingSearch() throws Exception {
        List<Renovation> createdRenovations = List.of(
                createRenovation(testUser1, "Modern Kitchen", "Description 1", true, LocalDateTime.of(2025, 1, 5, 5, 1)),
                createRenovation(testUser2, "Rustic Kitchen", "Description 2", false, LocalDateTime.of(2025, 2, 10, 10, 30)),
                createRenovation(testUser1, "Kitchen Renovation", "Description 3", true, LocalDateTime.of(2025, 3, 15, 8, 45)),
                createRenovation(testUser2, "Bathroom", "Description 4", true, LocalDateTime.of(2025, 4, 20, 15, 20)),
                createRenovation(testUser1, "Open living and dining", "Kitchen", true, LocalDateTime.of(2025, 5, 25, 11, 10))
        );

        Tag kitchenTag1 = new Tag("Kitchen", createdRenovations.get(0));
        Tag kitchenTag2 = new Tag("Kitchen", createdRenovations.get(2));
        tagService.save(kitchenTag1);
        tagService.save(kitchenTag2);


        MvcResult result = mockMvc.perform(get("/browse?search=&tags=Kitchen"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertEquals(1, pagination.getTotalPages());
        assertEquals(1, pagination.getCurrentPage());
        assertEquals(2, pagination.getItems().size());

        LocalDateTime lastDate = LocalDateTime.MAX;
        for (Renovation renovation : pagination.getItems()) {
            assertTrue(renovation.getIsPublic());
            assertTrue(lastDate.isAfter(renovation.getCreatedTimestamp()));
            lastDate = renovation.getCreatedTimestamp();
        }
    }

    @Test
    void testBrowseRenovations_WhenSearchWithMultipleTagsOnlyAndNotLoggedIn_ShowPublicRenovationsMatchingSearch() throws Exception {
        List<Renovation> createdRenovations = List.of(
                createRenovation(testUser1, "Modern Kitchen", "Description 1", true, LocalDateTime.of(2025, 1, 5, 5, 1)),
                createRenovation(testUser2, "Rustic Kitchen", "Description 2", false, LocalDateTime.of(2025, 2, 10, 10, 30)),
                createRenovation(testUser1, "Kitchen Renovation", "Description 3", true, LocalDateTime.of(2025, 3, 15, 8, 45)),
                createRenovation(testUser2, "Bathroom", "Description 4", true, LocalDateTime.of(2025, 4, 20, 15, 20)),
                createRenovation(testUser1, "Open living and dining", "Kitchen", true, LocalDateTime.of(2025, 5, 25, 11, 10))
        );

        Tag kitchenTag1 = new Tag("Kitchen", createdRenovations.get(0));
        Tag kitchenTag2 = new Tag("Kitchen", createdRenovations.get(2));
        Tag renovationTag = new Tag("Renovation", createdRenovations.get(0));
        tagService.save(kitchenTag1);
        tagService.save(kitchenTag2);
        tagService.save(renovationTag);

        MvcResult result = mockMvc.perform(get("/browse?search=&tags=Kitchen,Renovation"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertEquals(1, pagination.getItems().size());
        assertEquals(1, pagination.getTotalPages());
        assertEquals(1, pagination.getCurrentPage());

        LocalDateTime lastDate = LocalDateTime.MAX;
        for (Renovation renovation : pagination.getItems()) {
            assertTrue(renovation.getIsPublic());
            assertTrue(lastDate.isAfter(renovation.getCreatedTimestamp()));
            lastDate = renovation.getCreatedTimestamp();
        }
    }

    @Test
    void testBrowseRenovations_WhenSearchWithSearchStringAndTagsAndNotLoggedIn_ShowPublicRenovationsMatchingSearch() throws Exception {
        List<Renovation> createdRenovations = List.of(
                createRenovation(testUser1, "Modern Kitchen", "Description 1", true, LocalDateTime.of(2025, 1, 5, 5, 1)),
                createRenovation(testUser2, "Rustic Kitchen", "Description 2", false, LocalDateTime.of(2025, 2, 10, 10, 30)),
                createRenovation(testUser1, "Kitchen Renovation", "Description 3", true, LocalDateTime.of(2025, 3, 15, 8, 45)),
                createRenovation(testUser2, "Bathroom", "Description 4", true, LocalDateTime.of(2025, 4, 20, 15, 20)),
                createRenovation(testUser1, "Open living and dining", "Kitchen", true, LocalDateTime.of(2025, 5, 25, 11, 10))
        );

        Tag kitchenTag1 = new Tag("Kitchen", createdRenovations.get(0));
        Tag kitchenTag2 = new Tag("Kitchen", createdRenovations.get(2));
        tagService.save(kitchenTag1);
        tagService.save(kitchenTag2);


        MvcResult result = mockMvc.perform(get("/browse?search=1&tags=Kitchen"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertEquals(1, pagination.getItems().size());
        assertEquals(1, pagination.getTotalPages());
        assertEquals(1, pagination.getCurrentPage());

        LocalDateTime lastDate = LocalDateTime.MAX;
        for (Renovation renovation : pagination.getItems()) {
            assertTrue(renovation.getIsPublic());
            assertTrue(lastDate.isAfter(renovation.getCreatedTimestamp()));
            lastDate = renovation.getCreatedTimestamp();
        }
    }

    @Test
    void testBrowseRenovations_WhenSearchWithOneTagOnlyAndLoggedIn_ShowPublicRenovationsMatchingSearch() throws Exception {
        List<Renovation> createdRenovations = List.of(
                createRenovation(testUser1, "Modern Kitchen", "Description 1", true, LocalDateTime.of(2025, 1, 5, 5, 1)),
                createRenovation(testUser2, "Rustic Kitchen", "Description 2", false, LocalDateTime.of(2025, 2, 10, 10, 30)),
                createRenovation(testUser1, "Kitchen Renovation", "Description 3", true, LocalDateTime.of(2025, 3, 15, 8, 45)),
                createRenovation(testUser2, "Bathroom", "Description 4", true, LocalDateTime.of(2025, 4, 20, 15, 20)),
                createRenovation(testUser1, "Open living and dining", "Kitchen", true, LocalDateTime.of(2025, 5, 25, 11, 10))
        );

        Tag kitchenTag1 = new Tag("Kitchen", createdRenovations.get(0));
        Tag kitchenTag2 = new Tag("Kitchen", createdRenovations.get(2));
        tagService.save(kitchenTag1);
        tagService.save(kitchenTag2);


        MvcResult result = mockMvc.perform(get("/browse?search=&tags=Kitchen")
                        .with(csrf())
                        .with(user(String.valueOf(testUser1.getId())).password(testUser1.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertEquals(1, pagination.getTotalPages());
        assertEquals(1, pagination.getCurrentPage());
        assertEquals(2, pagination.getItems().size());

        LocalDateTime lastDate = LocalDateTime.MAX;
        for (Renovation renovation : pagination.getItems()) {
            assertTrue(renovation.getIsPublic());
            assertTrue(lastDate.isAfter(renovation.getCreatedTimestamp()));
            lastDate = renovation.getCreatedTimestamp();
        }
    }

    @Test
    void testBrowseRenovations_WhenSearchWithMultipleTagsOnlyAndLoggedIn_ShowPublicRenovationsMatchingSearch() throws Exception {
        List<Renovation> createdRenovations = List.of(
                createRenovation(testUser1, "Modern Kitchen", "Description 1", true, LocalDateTime.of(2025, 1, 5, 5, 1)),
                createRenovation(testUser2, "Rustic Kitchen", "Description 2", false, LocalDateTime.of(2025, 2, 10, 10, 30)),
                createRenovation(testUser1, "Kitchen Renovation", "Description 3", true, LocalDateTime.of(2025, 3, 15, 8, 45)),
                createRenovation(testUser2, "Bathroom", "Description 4", true, LocalDateTime.of(2025, 4, 20, 15, 20)),
                createRenovation(testUser1, "Open living and dining", "Kitchen", true, LocalDateTime.of(2025, 5, 25, 11, 10))
        );

        Tag kitchenTag1 = new Tag("Kitchen", createdRenovations.get(0));
        Tag kitchenTag2 = new Tag("Kitchen", createdRenovations.get(2));
        Tag renovationTag = new Tag("Renovation", createdRenovations.get(0));
        tagService.save(kitchenTag1);
        tagService.save(kitchenTag2);
        tagService.save(renovationTag);


        MvcResult result = mockMvc.perform(get("/browse?search=&tags=Kitchen,Renovation")
                        .with(csrf())
                        .with(user(String.valueOf(testUser1.getId())).password(testUser1.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertEquals(1, pagination.getTotalPages());
        assertEquals(1, pagination.getCurrentPage());
        assertEquals(1, pagination.getItems().size());

        LocalDateTime lastDate = LocalDateTime.MAX;
        for (Renovation renovation : pagination.getItems()) {
            assertTrue(renovation.getIsPublic());
            assertTrue(lastDate.isAfter(renovation.getCreatedTimestamp()));
            lastDate = renovation.getCreatedTimestamp();
        }
    }

    @Test
    void testBrowseRenovations_WhenSearchWithSearchStringAndTagsAndLoggedIn_ShowPublicRenovationsMatchingSearch() throws Exception {
        List<Renovation> createdRenovations = List.of(
                createRenovation(testUser1, "Modern Kitchen", "Description 1", true, LocalDateTime.of(2025, 1, 5, 5, 1)),
                createRenovation(testUser2, "Rustic Kitchen", "Description 2", false, LocalDateTime.of(2025, 2, 10, 10, 30)),
                createRenovation(testUser1, "Kitchen Renovation", "Description 3", true, LocalDateTime.of(2025, 3, 15, 8, 45)),
                createRenovation(testUser2, "Bathroom", "Description 4", true, LocalDateTime.of(2025, 4, 20, 15, 20)),
                createRenovation(testUser1, "Open living and dining", "Kitchen", true, LocalDateTime.of(2025, 5, 25, 11, 10))
        );

        Tag kitchenTag1 = new Tag("Kitchen", createdRenovations.get(0));
        Tag kitchenTag2 = new Tag("Kitchen", createdRenovations.get(2));

        tagService.save(kitchenTag1);
        tagService.save(kitchenTag2);

        MvcResult result = mockMvc.perform(get("/browse?search=3&tags=Kitchen")
                        .with(csrf())
                        .with(user(String.valueOf(testUser1.getId())).password(testUser1.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertEquals(1, pagination.getTotalPages());
        assertEquals(1, pagination.getCurrentPage());
        assertEquals(1, pagination.getItems().size());

        LocalDateTime lastDate = LocalDateTime.MAX;
        for (Renovation renovation : pagination.getItems()) {
            assertTrue(renovation.getIsPublic());
            assertTrue(lastDate.isAfter(renovation.getCreatedTimestamp()));
            lastDate = renovation.getCreatedTimestamp();
        }
    }

    @Test
    void testBrowseRenovations_NoPublicRenovationsFound() throws Exception {
        createRenovation(testUser1, "Modern Kitchen", "Description 1", false, LocalDateTime.of(2025, 1, 5, 5, 1));
        createRenovation(testUser2, "Rustic Kitchen", "Description 2", false, LocalDateTime.of(2025, 2, 10, 10, 30));
        createRenovation(testUser1, "Kitchen Renovation", "Description 3", false, LocalDateTime.of(2025, 3, 15, 8, 45));
        createRenovation(testUser2, "Bathroom", "Description 4", false, LocalDateTime.of(2025, 4, 20, 15, 20));
        createRenovation(testUser1, "Open living and dining", "Kitchen", false, LocalDateTime.of(2025, 5, 25, 11, 10));

        MvcResult result = mockMvc.perform(get("/browse")
                        .with(csrf())
                        .with(user(String.valueOf(testUser1.getId())).password(testUser1.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        String noRenovationsFoundError = (String) model.get("searchError");
        assertEquals("No renovations match your search", noRenovationsFoundError);
    }

    @Test
    void testBrowseRenovations_WhenSearchWithSearchString_NoPublicRenovationsFound() throws Exception {
        List<Renovation> createdRenovations = List.of(
                createRenovation(testUser1, "Modern Kitchen", "Description 1", true, LocalDateTime.of(2025, 1, 5, 5, 1)),
                createRenovation(testUser2, "Rustic Kitchen", "Description 2", false, LocalDateTime.of(2025, 2, 10, 10, 30)),
                createRenovation(testUser1, "Kitchen Renovation", "Description 3", true, LocalDateTime.of(2025, 3, 15, 8, 45)),
                createRenovation(testUser2, "Bathroom", "Description 4", true, LocalDateTime.of(2025, 4, 20, 15, 20)),
                createRenovation(testUser1, "Open living and dining", "Kitchen", true, LocalDateTime.of(2025, 5, 25, 11, 10))
        );

        Tag kitchenTag1 = new Tag("Kitchen", createdRenovations.get(0));
        Tag kitchenTag2 = new Tag("Kitchen", createdRenovations.get(2));
        tagService.save(kitchenTag1);
        tagService.save(kitchenTag2);

        // There are tests on SearchBarIntegrationTests that verify that searching on myRenovations works, so don't
        // need to test here
        MvcResult result = mockMvc.perform(get("/browse?search=Closed"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        String noRenovationsFoundError = (String) model.get("searchError");
        assertEquals("No renovations match your search", noRenovationsFoundError);
    }

    @Test
    void testBrowseRenovations_WhenSearchWithOneTag_NoPublicRenovationsFound() throws Exception {
        List<Renovation> createdRenovations = List.of(
                createRenovation(testUser1, "Modern Kitchen", "Description 1", true, LocalDateTime.of(2025, 1, 5, 5, 1)),
                createRenovation(testUser2, "Rustic Kitchen", "Description 2", false, LocalDateTime.of(2025, 2, 10, 10, 30)),
                createRenovation(testUser1, "Kitchen Renovation", "Description 3", true, LocalDateTime.of(2025, 3, 15, 8, 45)),
                createRenovation(testUser2, "Bathroom", "Description 4", true, LocalDateTime.of(2025, 4, 20, 15, 20)),
                createRenovation(testUser1, "Open living and dining", "Kitchen", true, LocalDateTime.of(2025, 5, 25, 11, 10))
        );
        Tag kitchenTag1 = new Tag("Kitchen", createdRenovations.get(0));
        Tag kitchenTag2 = new Tag("Kitchen", createdRenovations.get(2));
        tagService.save(kitchenTag1);
        tagService.save(kitchenTag2);

        MvcResult result = mockMvc.perform(get("/browse?search=&tags=Bathroom"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
        String noRenovationsFoundError = (String) model.get("searchError");
        assertEquals("No renovations match your search", noRenovationsFoundError);
    }
}
