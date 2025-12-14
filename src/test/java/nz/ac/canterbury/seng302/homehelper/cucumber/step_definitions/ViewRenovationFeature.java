package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ViewRenovationFeature {

    @Autowired
    private MockMvc mockMvc;
    private final TestContext testContext;

    public ViewRenovationFeature(TestContext testContext) {
        this.testContext = testContext;
    }

    @Given("There exists a renovation with name {string} and description {string}")
    public void createExistingRenovation(String name, String description) {
        Renovation renovation = new Renovation(name, description);
        renovation.setOwner(testContext.getUser());
        testContext.renovationRepository.save(renovation);
        testContext.setRenovation(renovation);
    }

    @When("I click on the renovation")
    public void clickOnRenovation() throws Exception {
        Renovation renovation = testContext.getRenovation();
        User user = testContext.getUser();
        testContext.setResult(mockMvc.perform(get("/renovation/" + renovation.getId())
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn());
        System.out.println(testContext.getResult());

    }

    @Then("I can see the renovation record information with name {string} and description {string}")
    public void canSeeRenovationRecordInformation(String name, String description) throws Exception {
        ModelMap model = requireNonNull(testContext.getResult().getModelAndView()).getModelMap();
        Renovation renovation = (Renovation) model.getAttribute("renovation");
        assertNotNull(renovation, "Expected 'renovation' attribute in model but it was null");
        assertEquals(name, renovation.getName());
        assertEquals(description, renovation.getDescription());
    }

    @When("I click on \"My Renovations\"")
    public void clickOnMyRenovations() throws Exception {
        User user = testContext.getUser();
        testContext.setResult(mockMvc.perform(get("/myRenovations")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn());
    }

    @Then("I can see a list of all the renovation records I have created")
    public void canSeeAListOfRenovationRecords() throws Exception {
        ModelMap model = requireNonNull(testContext.getResult().getModelAndView()).getModelMap();
        @SuppressWarnings("unchecked")
        Pagination<Renovation> pagination = (Pagination<Renovation>) model.getAttribute("pagination");
        assertNotNull(pagination, "Pagination should not be null");
        List<Renovation> renovations = pagination.getItems();
        assertFalse(renovations.isEmpty(), "Renovation list should not be empty");
        assertEquals(renovations.getFirst(), testContext.getRenovation());
    }

    @Given("user {string} has a renovation with name {string} and description {string}")
    public void userHasARenovationWithNameRenovationNameAndDescriptionRenovationDescription(String userEmail, String renovationName, String renovationDescription) {
        User user = testContext.userRepository.findByEmail(userEmail).getFirst();
        Renovation renovation = new Renovation(renovationName, renovationDescription);
        renovation.setOwner(user);
        testContext.renovationRepository.save(renovation);
    }

    @When("I try to access user {string} renovation")
    public void accessUserRenovation(String email) throws Exception {
        User user = testContext.userRepository.findByEmail(email).getFirst();
        Renovation userRenovation = user.getRenovations().getFirst();
        assertNotNull(userRenovation, "Renovation should not be null");
        testContext.setResult(mockMvc.perform(get("/renovation/" + userRenovation.getId())
                        .with(csrf())
                        .with(user(String.valueOf(testContext.getUser().getId())).password(testContext.getUser().getPassword()).roles("USER")))
                .andReturn());
    }

    @Then("I am redirected to a \"Not Found\" page")
    public void redirectedToNotFoundPage() throws Exception {
        MvcResult result = testContext.getResult();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Then("I am redirected to the Login page")
    public void redirectedToLoginPage() throws Exception {
        MvcResult result = testContext.getResult();
        assertEquals(302, result.getResponse().getStatus());
        assertEquals("/login", result.getResponse().getHeader("Location"));
    }

    @When("I try to access user {string} renovation unauthenticated")
    public void accessUserRenovationUnauthenticated(String email) throws Exception {
        User user = testContext.userRepository.findByEmail(email).getFirst();
        Renovation userRenovation = user.getRenovations().getFirst();
        assertNotNull(userRenovation, "Renovation should not be null");
        testContext.setResult(mockMvc.perform(get("/renovation/" + userRenovation.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn());
    }
}
