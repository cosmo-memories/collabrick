package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

//@AutoConfigureMockMvc
@Transactional
public class SearchByTagsFeature {

    @Autowired
    private MockMvc mockMvc;

    private final TestContext testContext;

    private final List<String> tags = new ArrayList<>();

    private String searchString = "";

    @Autowired
    public SearchByTagsFeature(TestContext context) {
        this.testContext = context;
    }

    @Given("a user with email {string} adds a tag named {string} to renovation with title {string}")
    public void a_user_with_email_adds_a_tag_named_to_renovation_with_title(String email, String tagName, String renovationTitle) {
        User user = testContext.userRepository.findByEmail(email).stream().findFirst().orElseThrow();
        Renovation renovation = testContext.renovationRepository.findByNameAndUser(renovationTitle, user).stream().findFirst().orElseThrow();
        Tag newTag = new Tag(tagName, renovation);
        testContext.tagRepository.save(newTag);
    }

    @Given("an anonymous user adds tag {string} to the search bar")
    public void an_anonymous_user_adds_to_the_search_bar(String tagName) {
        tags.add(tagName);
    }

    @Given("an anonymous user adds text {string} to the search bar")
    public void an_anonymous_user_adds_text_to_the_search_bar(String searchString) {
        this.searchString = searchString;
    }


    @When("an anonymous user presses the search icon")
    public void an_anonymous_user_presses_the_search_icon() throws Exception {
        String query = String.join(",", tags);
        testContext.setResult(mockMvc.perform(get("/browse?search=" + searchString + "&selectedTagList=" + query))
                .andReturn());
    }

    @Then("they should see a public renovation created by {string}, titled {string} with tag {string}")
    public void they_should_see_a_public_renovation_created_by_titled_with_tag(String email, String renovationTitle, String tagName) {
        User user = testContext.userRepository.findByEmail(email).stream().findFirst().orElseThrow();
        Renovation renovation = testContext.renovationRepository.findByNameAndUser(renovationTitle, user).stream().findFirst().orElseThrow();
        List<Tag> tagsForRenovations = testContext.tagRepository.findByRenovation(renovation.getId());
        assertTrue(tagsForRenovations.stream().anyMatch(tag -> tag.getTag().equals(tagName)));
        Map<String, Object> model = Objects.requireNonNull(testContext.getResult().getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertTrue(pagination.getItems().contains(renovation));
    }

    @Then("there is a total of {int} results")
    public void there_is_a_total_of_results_on_page(Integer numResults) {
        Map<String, Object> model = Objects.requireNonNull(testContext.getResult().getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertEquals((long) numResults, pagination.getTotalItems());
    }

    @Then("they should see an error that says {string}")
    public void they_should_see_an_error_that(String errorMessage) {
        Map<String, Object> model = Objects.requireNonNull(testContext.getResult().getModelAndView()).getModel();
        String noRenovationsFoundError = (String) model.get("searchError");
        assertEquals("No renovations match your search", noRenovationsFoundError);
    }

    @When("a logged in user with email {string} searches using the tag named {string}")
    public void a_logged_in_user_with_email_searches_using_the_tag_named(String email, String tagName) throws Exception {
        User user = testContext.userRepository.findByEmail(email).stream().findFirst().orElseThrow();
        tags.add(tagName);
        String query = String.join(",", tags);
        testContext.setResult(mockMvc.perform(get("/browse?search=" + searchString + "&selectedTagList=" + query)
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn());
    }

    @When("a user with email {string} searches with the search {string}")
    public void a_user_with_email_searches_with_the_search(String email, String searchString) throws Exception {
        User user = testContext.userRepository.findByEmail(email).stream().findFirst().orElseThrow();
        this.searchString = searchString;
        testContext.setResult(mockMvc.perform(get("/browse?search=" + searchString)
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andReturn());
    }
}
