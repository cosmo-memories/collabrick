//ChatGPT helped in how to write these as I was really unsure on how to write them myself with mocking, everything I tried wasn't working
package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Transactional
public class PublicRenovationFeature {

    @Autowired
    private MockMvc mockMvc;

    private final TestContext testContext;

    private Renovation testRenovation;

    @Autowired
    public PublicRenovationFeature(TestContext testContext) {
        this.testContext = testContext;
    }

    /**
     * Ensures that the database is fully reset between scenario outline examples
     */
    @Before
    public void resetDatabase() {
        testContext.renovationRepository.deleteAll();
        testContext.userRepository.deleteAll();
    }


    @Given("A user with the email {string} is logged in")
    public void a_user_with_the_email_is_logged_in(String email) {
        User testUser = new User("Test", "user", email, "Password123!", "Password123!");
        testUser = testContext.userRepository.save(testUser);
        testContext.setUser(testUser);
    }

    @And("They own a renovation titled {string}")
    public void they_own_a_renovation_titled(String title) {
        testRenovation = new Renovation(title, "Test description");
        testRenovation.setOwner(testContext.getUser());
        testContext.renovationRepository.save(testRenovation);
        testContext.setRenovation(testRenovation);
    }

    @And("The renovation visibility is set to {string}")
    public void the_renovation_visibility_is_set_to(String isPublic) {
        testRenovation.setPublic(Boolean.valueOf(isPublic));
        testContext.renovationRepository.save(testRenovation);
    }

    @When("The user sets the renovation visibility to {string}")
    public void the_user_sets_the_renovation_visibility_to(String isPublic) throws Exception {
        testContext.setResult(mockMvc.perform(post("/renovation/{id}/setVisibility", testRenovation.getId())
                        .param("isPublic", isPublic)
                        .with(csrf())
                        .with(user(String.valueOf(testContext.getUser().getId())).password(testContext.getUser().getPassword()).roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andReturn());
    }

    @Then("The renovation is set to public")
    public void the_renovation_is_set_to_public() {
        Optional<Renovation> editedRenovation = testContext.renovationRepository.findById(testRenovation.getId());
        assertTrue(editedRenovation.isPresent());
        assertTrue(editedRenovation.get().getIsPublic());
    }

    @And("The user is redirected to the individual renovation page")
    public void the_user_is_redirected_to_the_individual_renovation_page() {
        String expectedUrl = "/renovation/" + testRenovation.getId();
        assertEquals(expectedUrl, testContext.getResult().getResponse().getRedirectedUrl());
    }

    @Then("The renovation is set to private")
    public void the_renovation_is_set_to_private() {
        Optional<Renovation> editedRenovation = testContext.renovationRepository.findById(testRenovation.getId());
        assertTrue(editedRenovation.isPresent());
        assertFalse(editedRenovation.get().getIsPublic());
    }

    /**
     * modified step definitions made for AC3
     **/
    @Given("a user registers with first name {string}, last name {string}, email {string}, password {string}, retype password {string}")
    public void a_user_registers_with_first_name_last_name_email_password_retype_password(String firstName, String lastName, String email, String password, String retypePassword) {
        User user = new User(firstName, lastName, email, password, retypePassword);
        user.setActivated(true);
        user = testContext.userRepository.save(user);
        testContext.setUser(user);
    }

    @Given("a user with email {string} creates a renovation with title {string}, description {string}, timestamp {string}")
    public void a_user_with_email_creates_a_renovation_with_title_description_timestamp(String email, String renovationTitle, String renovationDescription, String renovationTimestamp) {
        User user = testContext.userRepository.findByEmail(email).stream().findFirst().orElseThrow();
        Renovation renovation = new Renovation(renovationTitle, renovationDescription);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(renovationTimestamp, formatter);
        renovation.setOwner(user);
        renovation.setCreatedTimestamp(dateTime);
        testContext.renovationRepository.save(renovation);
    }

    @Given("a user with email {string} makes the renovation with title {string} public")
    public void a_user_with_email_makes_the_renovation_with_title_public(String email, String renovationTitle) {
        User user = testContext.userRepository.findByEmail(email).stream().findFirst().orElseThrow();
        Renovation renovation = testContext.renovationRepository.findByNameAndUser(renovationTitle, user).stream().findFirst().orElseThrow();
        renovation.setPublic(true);
        testContext.renovationRepository.save(renovation);
    }

    @When("an anonymous user goes to page {int} of the browse renovations page")
    public void an_anonymous_user_goes_to_page_of_the_browse_renovations_page(Integer pageNumber) throws Exception {
        testContext.setResult(mockMvc.perform(get("/browse").param("page", String.valueOf(pageNumber)))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn());
    }

    @When("a logged in user with {string} goes to page {int} of the browse renovations page")
    public void a_logged_in_user_with_goes_to_page_of_the_browse_renovations_page(String email, Integer pageNumber) throws Exception {
        User user = testContext.userRepository.findByEmail(email).stream().findFirst().orElseThrow();
        testContext.setResult(mockMvc.perform(get("/browse")
                        .with(user(user.getId().toString()).password(user.getPassword())))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/browseRenovationsPage"))
                .andReturn());
    }

    @Then("they should see a public renovation created by {string}, titled {string}")
    public void they_should_see_a_public_renovation_created_by_titled(String email, String renovationTitle) {
        User user = testContext.userRepository.findByEmail(email).stream().findFirst().orElseThrow();
        Renovation renovation = testContext.renovationRepository.findByNameAndUser(renovationTitle, user).stream().findFirst().orElseThrow();
        Map<String, Object> model = Objects.requireNonNull(testContext.getResult().getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertTrue(pagination.getItems().contains(renovation));
    }

    @Then("they should not see a private renovation created by {string}, titled {string}")
    public void they_should_not_see_a_private_renovation_created_by_titled(String email, String renovationTitle) {
        User user = testContext.userRepository.findByEmail(email).stream().findFirst().orElseThrow();
        Renovation renovation = testContext.renovationRepository.findByNameAndUser(renovationTitle, user).stream().findFirst().orElseThrow();
        Map<String, Object> model = Objects.requireNonNull(testContext.getResult().getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertFalse(pagination.getItems().contains(renovation));
    }

    @Then("the renovations are displayed in descending order by timestamp")
    public void the_renovations_should_be_displayed_in_descending_order_by_timestamp() {
        Map<String, Object> model = Objects.requireNonNull(testContext.getResult().getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        List<Renovation> sortedRenovations = pagination.getItems().stream()
                .sorted(Comparator.comparing(Renovation::getCreatedTimestamp).reversed())
                .toList();

        assertEquals(sortedRenovations, pagination.getItems());
    }

    @Then("There are {int} renovations on page {int}")
    public void there_are_renovations_on_page(int expectedNumRenovations, int pageNumber) {
        Map<String, Object> model = Objects.requireNonNull(testContext.getResult().getModelAndView()).getModel();
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.get("pagination");
        assertEquals(expectedNumRenovations, pagination.getItems().size());
        assertEquals(pageNumber, pagination.getCurrentPage());
    }
}
