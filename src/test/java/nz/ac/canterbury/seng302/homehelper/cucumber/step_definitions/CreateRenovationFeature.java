package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.ModelMap;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class CreateRenovationFeature {
    @Autowired
    private MockMvc mockMvc;
    private final TestContext testContext;
    private final Map<String, String> formInput;

    public CreateRenovationFeature(TestContext testContext) {
        formInput = new HashMap<>();
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

    @When("I click Create New Renovation Record")
    public void iClickCreateNewRenovationRecord() throws Exception {
        User user = testContext.getUser();
        testContext.setResult(mockMvc.perform(get("/myRenovations/newRenovation")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn());
    }

    @Then("I see a form to create a new renovation record")
    public void iSeeAFormToCreateARenovationRecord() throws Exception {
        String view = testContext.getResult().getModelAndView().getViewName();
        assertEquals(view, "pages/renovation/createEditRenovationPage");
    }


    @Given("I input {string} into the renovation record {string} field")
    public void insertRenovationName(String value, String field) {
        formInput.put(field, value);
    }

    @When("I click the Create button")
    public void clickCreateButton() throws Exception {
        User user = testContext.getUser();
        String name = formInput.getOrDefault("name", "");
        String description = formInput.getOrDefault("description", "");
        String room = formInput.getOrDefault("room", "");
        // Perform the POST request
        MvcResult result = mockMvc.perform(post("/myRenovations/newRenovation")
                        .param("name", name)
                        .param("description", description)
                        .param("roomName[]", room)
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andReturn();

        testContext.setResult(result);

        // Check if the result is a redirect
        int status = result.getResponse().getStatus();
        if (status >= 300 && status < 400) {
            String redirectUrl = result.getResponse().getRedirectedUrl();
            String[] parts = redirectUrl.split("/");
            long renovationId = Long.parseLong(parts[parts.length - 1]);

            Renovation renovation = testContext.renovationRepository.findById(renovationId).orElse(null);
            assert renovation != null;
            testContext.setRenovation(renovation);
        } else {
            String viewName = result.getModelAndView() != null ? result.getModelAndView().getViewName() : "No view";
            System.out.println("Form submission failed. Stayed on view: " + viewName);
        }
    }

    @Then("A new renovation record is created with name {string} and description {string}")
    public void renovationIsCreated(String name, String description) throws Exception {
        Renovation renovation = testContext.getRenovation();
        assertEquals(name, renovation.getName());
        assertEquals(description, renovation.getDescription());
    }


    @Then("A new renovation record is created with name {string}, description {string} and room {string}")
    public void renovationWithRoomIsCreated(String name, String description, String room) throws Exception {
        Renovation renovation = testContext.getRenovation();
        assertEquals(name, renovation.getName());
        assertEquals(description, renovation.getDescription());
        assertEquals(room, renovation.getRooms().getFirst().getName());
    }

    @Then("The user is redirected to the created renovation page")
    public void userIsRedirectedToTheCreatedRenovationPage() throws Exception {
        Renovation renovation = testContext.getRenovation();
        String expectedUrl = "/renovation/" + renovation.getId();
        assertEquals(expectedUrl, testContext.getResult().getResponse().getRedirectedUrl());

    }

    @Then("I should see an error message indicating invalid characters in the renovation record name field")
    public void errorMessageIndicatingInvalidCharactersInRenovationRecordNameField() throws Exception {
        ModelMap model = requireNonNull(testContext.getResult().getModelAndView()).getModelMap();
        String errorMessage = (String) model.getAttribute("renovationNameError");
        assertEquals("Renovation record name must only include letters, numbers, spaces, dots, hyphens, or apostrophes, and must contain at least one letter or number", errorMessage);
    }

    @Then("No Renovation record is created")
    public void noRenovationRecordIsCreated() throws Exception {
        assertEquals(0, testContext.renovationRepository.findAll().size());
    }

    @Then("I should see an error message indicating the renovation record name field cannot be blank")
    public void errorMessageIndicatingTheRenovationRecordNameFieldCannotBeBlank() throws Exception {
        ModelMap model = requireNonNull(testContext.getResult().getModelAndView()).getModelMap();
        String errorMessage = (String) model.getAttribute("renovationNameError");
        assertEquals("Renovation record name cannot be empty", errorMessage);
    }

    @Then("I should see an error message indicating the renovation record name is not unique")
    public void nonUniqueNameError() {
        ModelMap model = requireNonNull(testContext.getResult().getModelAndView()).getModelMap();
        String errorMessage = (String) model.getAttribute("renovationNameError");
        assertEquals("Renovation record name is not unique", errorMessage);
    }

    @Then("No new Renovation record is created")
    public void noNewRenovationRecordIsCreated() throws Exception {
        assertEquals(1, testContext.renovationRepository.findAll().size());
    }

    @Then("I should see an error message indicating the renovation description is too long")
    public void errorIndicatingDescriptionIsTooLong() {
        ModelMap model = requireNonNull(testContext.getResult().getModelAndView()).getModelMap();
        String errorMessage = (String) model.getAttribute("renovationDescriptionError");
        assertEquals("Renovation record description must be 512 characters or less", errorMessage);
    }


}
