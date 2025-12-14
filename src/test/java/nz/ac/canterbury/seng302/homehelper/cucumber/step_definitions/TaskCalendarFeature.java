package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Transactional
public class TaskCalendarFeature {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private RenovationService renovationService;

    //    private final User testUser;
//    private final Renovation testRenovation;
    private MvcResult result;
    private Task testTask;
    private final TestContext testContext;

    public TaskCalendarFeature(TestContext testContext) {
        this.testContext = testContext;
//        testUser = testContext.getUser();
//        testRenovation = testContext.getRenovation();
    }

    @When("The user double clicks the date {string} on the task calendar")
    public void the_user_double_clicks_the_date_on_the_task_calendar(String date) throws Exception {
        result = mockMvc.perform(get("/myRenovations/{id}/newTask?taskDueDate={date}", testContext.getRenovation().getId(), date)
                        .with(csrf())
                        .with(user(String.valueOf(testContext.getUser().getId())).password(testContext.getUser().getPassword()).roles("USER")))
                .andReturn();
    }

    @Then("They are directed to a new task form")
    public void they_are_directed_to_a_new_task_form() throws Exception {
        String expectedUrl = "/myRenovations/" + testContext.getRenovation().getId() + "/newTask";
        assertEquals(expectedUrl, result.getRequest().getRequestURI());
    }

    @And("The date is prefilled with {string}")
    public void the_date_is_prefilled_with(String date) throws Exception {
        assertEquals(date, Objects.requireNonNull(result.getModelAndView()).getModel().get("taskDueDate"));
    }

    @Given("There is a task titled {string}")
    public void there_is_a_task_titled(String taskName) throws Exception {
        testTask = new Task(testContext.getRenovation(), taskName, "Test Description", "house.png");
        renovationService.saveTask(testContext.getRenovation().getId(), testTask, "2026-01-01");
    }

    @When("The user double clicks on a task in the calendar")
    public void the_user_double_clicks_on_a_task_in_the_calendar() throws Exception {
        result = mockMvc.perform(get("/myRenovations/{renovationId}/editTask/{taskId}", testContext.getRenovation().getId(), testTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(testContext.getUser().getId())).password(testContext.getUser().getPassword()).roles("USER")))
                .andReturn();
    }

    @Then("They are redirected to the renovation's calendar tab")
    public void they_are_directed_to_the_renovation_s_calendar_tab() {
        String expectedUrl = "/renovation/" + testContext.getRenovation().getId() + "/calendar?dateStr=" + testTask.getDueDate();
        assertEquals(expectedUrl, result.getResponse().getRedirectedUrl());
    }

    @Then("They land on the calendar tab")
    public void they_land_on_the_calendar_tab() {
        // Needed to GPT this step because the cancel button doesn't use mapping unlike the submit button
        String expectedPath = "/renovation/" + testContext.getRenovation().getId() + "/calendar";
        assertEquals(expectedPath, result.getRequest().getRequestURI());
        assertEquals(testTask.getDueDate().toString(), result.getRequest().getParameter("dateStr"));
    }

    @Given("They are directed to the edit task form for that task")
    public void they_are_redirected_to_the_edit_task_form_for_that_task() throws Exception {
        result = mockMvc.perform(get("/myRenovations/{renovationId}/editTask/{taskId}", testContext.getRenovation().getId(), testTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(testContext.getUser().getId())).password(testContext.getUser().getPassword()).roles("USER")))
                .andReturn();

        assertEquals("pages/renovation/createEditTaskPage", Objects.requireNonNull(result.getModelAndView()).getViewName());
    }

    @When("The user clicks the cancel button on the edit task form")
    public void the_user_clicks_the_cancel_button_on_the_edit_task_form() throws Exception {
        String date = "2026-01-01";
        result = mockMvc.perform(get("/renovation/{id}/calendar", testContext.getRenovation().getId())
                        .param("dateStr", date)
                        .with(user(String.valueOf(testContext.getUser().getId())).password(testContext.getUser().getPassword()).roles("USER")))
                .andReturn();
    }

    @When("The user clicks the submit button on the edit task form")
    public void the_user_clicks_the_submit_button_on_the_edit_task_form() throws Exception {
        result = mockMvc.perform(
                post("/myRenovations/{id}/editTask/{taskId}", testContext.getRenovation().getId(), testTask.getId())
                        .param("taskName", testTask.getName())
                        .param("taskDescription", testTask.getDescription())
                        .param("taskDueDate", testTask.getDueDate().toString())
                        .param("pageNumber", "1")
                        .param("dateInvalid", "")
                        .param("state", testTask.getState().name())
                        .param("referer", "calendar")
                        .with(csrf())
                        .with(user(String.valueOf(testContext.getUser().getId())).password(testContext.getUser().getPassword()).roles("USER"))
        ).andReturn();
    }

    @And("They change the due date to {string}")
    public void they_change_the_due_date_to(String newDate) {
        testTask.setDueDate(java.time.LocalDate.parse(newDate));
    }

    @Then("The calendar highlights the date {string}")
    public void the_calendar_highlights_the_date(String expectedDate) {
        String expectedPath = "/renovation/" + testContext.getRenovation().getId() + "/calendar";
        String redirectedPath = result.getResponse().getRedirectedUrl();

        // I split the functionality here into two paths: one for submit functionality, one for cancel
        if (redirectedPath != null) {
            String expectedUrl = expectedPath + "?dateStr=" + expectedDate;
            assertEquals(expectedUrl, redirectedPath);
            return;
        }

        assertEquals(expectedPath, result.getRequest().getRequestURI());
        assertEquals(expectedDate, result.getRequest().getParameter("dateStr"));
    }

}
