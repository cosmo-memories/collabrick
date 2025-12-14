package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseDto;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class TaskExpensesFeature {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final TestContext testContext;

    private final List<ExpenseDto> expenses = new ArrayList<>();

    private String responseBody = "";

    @Autowired
    public TaskExpensesFeature(TestContext context) {
        this.testContext = context;
    }

    @Given("They have added an expense named {string} with a price of {double} on {string} with category {string}")
    public void they_have_added_an_expense_named_with_a_price_of_on_with_category(String expenseName, Double price, String date, String category) {
        expenses.add(new ExpenseDto(expenseName, category, date, price));
    }


    @When("The user clicks the Add to Task button")
    public void the_user_clicks_the_button() throws Exception {
        Task task = testContext.getRenovation().getTasks().getFirst();
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/myRenovations/" + testContext.getRenovation().getId() + "/addTaskExpenses/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenses))
                        .with(csrf())
                        .with(user(String.valueOf(testContext.getUser().getId())).password(testContext.getUser().getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
        responseBody = response.getResponse().getContentAsString();
    }

    @Then("They are redirected to the task details page for {string}")
    public void they_are_redirected_to_the_task_details_page_for(String taskName) {
        Renovation renovation = testContext.getRenovation();
        Task task = renovation.getTasks().getFirst();
        String redirectUrl = "renovation/" + renovation.getId() + "/tasks/" + task.getId();

        Assertions.assertThat(responseBody).contains(redirectUrl);
        Assertions.assertThat(taskName).isEqualTo(task.getName());
    }

    @Then("The total expenditure shows {double}")
    public void the_total_expenditure_shows(Double totalCost) {
        Task task = testContext.getRenovation().getTasks().getFirst();
        BigDecimal cost = task.getTotalCost();
        double expenditure = cost.doubleValue();
        Assertions.assertThat(expenditure).isEqualTo(totalCost);
    }
}