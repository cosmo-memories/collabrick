package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Transactional
public class TaskStatesFeature {

    @Autowired
    private MockMvc mockMvc;

    private final TestContext testContext;

    @Autowired
    public TaskStatesFeature(TestContext context) {
        this.testContext = context;
    }

    @When("a user with email {string} creates a new task with name {string} and description {string} with state {string} for renovation {string}")
    public void a_user_with_email_creates_a_new_task_with_name_and_description_with_state_for_renovation(String email, String taskName, String taskDescription, String taskState, String renovationName) throws Exception {
        User user = testContext.userRepository.findByEmail(email).getFirst();
        Long renoId = testContext.renovationRepository.findByNameAndUser(renovationName, user).getFirst().getId();
        mockMvc.perform(post("/myRenovations/{id}/newTask", renoId)
                .with(csrf())
                .with(user(String.valueOf(user.getId()))
                        .password(user.getPassword())
                        .roles("USER"))
                .param("taskName", taskName)
                .param("taskDescription", taskDescription)
                .param("dateInvalid", "")           // no quotes, just an empty string
                .param("state", taskState));
    }

    @When("a user with email {string} goes to the individual renovation tasks page of renovation with name {string}")
    public void a_user_with_email_goes_to_the_individual_renovation_tasks_page_of_renovation_with_name(String email, String renovationName) throws Exception {
        User user = testContext.userRepository.findByEmail(email).getFirst();
        Long renoId = testContext.renovationRepository.findByNameAndUser(renovationName, user).getFirst().getId();
        testContext.setResult(mockMvc.perform(get("/renovation/{id}/tasks", renoId).with(csrf()).with(user(String.valueOf(user.getId()))
                .password(user.getPassword())
                .roles("USER"))).andReturn());
    }

    @Then("A user with email {string} has a task for renovation {string} called {string} with state {string}")
    public void a_user_with_email_has_a_task_for_renovation_called_with_state(String email, String renovationName, String taskName, String state) {
        User user = testContext.userRepository.findByEmail(email).getFirst();
        Renovation renovation = testContext.renovationRepository.findByNameAndUser(renovationName, user).getFirst();

        assertEquals(taskName, renovation.getTasks().get(0).getName());
        Task task = renovation.getTasks().get(0);
        Map<String, Object> model = Objects.requireNonNull(testContext.getResult().getModelAndView()).getModel();
        Pagination<Task> pagination = (Pagination<Task>) model.get("pagination");
        assertEquals(1, pagination.getItems().size()); // make sure there is 1 task
        Task taskFromModel = pagination.getItems().getFirst();
        assertEquals(task.getId(), taskFromModel.getId());
        assertEquals(state, taskFromModel.getState().getDisplayName());
    }


}
