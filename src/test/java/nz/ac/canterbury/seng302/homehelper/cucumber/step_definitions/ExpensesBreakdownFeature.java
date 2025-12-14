package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseCategory;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.ExpenseRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ExpensesBreakdownFeature {

    @Autowired
    private MockMvc mockMvc;

    private final TestContext testContext;
    private final ExpenseRepository expenseRepository;
    private final TaskService taskService;

    @Autowired
    public ExpensesBreakdownFeature(TestContext context, ExpenseRepository expenseRepository, TaskService taskService) {
        this.testContext = context;
        this.expenseRepository = expenseRepository;
        this.taskService = taskService;
    }

    @Given("The task has an expense named {string} with a price of {string} on {string} with category {string}")
    public void the_task_has_an_expense_named_with_a_price_of_on_with_category(String expenseName, String price, String date, String category) {
        Task task = testContext.getRenovation().getTasks().getFirst();
        ExpenseCategory expenseCategory = ExpenseCategory.fromDisplayName(category.trim());
        Expense expense = new Expense(task, expenseName, expenseCategory, new BigDecimal(price), LocalDate.parse(date));
        taskService.saveExpense(expense);
    }

    @When("The user clicks the Delete button on {string}")
    public void the_user_clicks_the_delete_button_on(String expenseName) throws Exception {
        Task task = testContext.getRenovation().getTasks().getFirst();
        long expenseId = expenseRepository.findByNameAndTask(expenseName, task.getId()).getFirst().getId();
        mockMvc.perform(MockMvcRequestBuilders.post("/task/" + task.getId() + "/expense/" + expenseId + "/delete")
                        .with(csrf())
                        .with(user(String.valueOf(testContext.getUser().getId())).password(testContext.getUser().getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("The expense {string} is deleted")
    public void the_expense_is_deleted(String expenseName) {
        Task task = testContext.getRenovation().getTasks().getFirst();
        List<Expense> expenses = expenseRepository.findByNameAndTask(expenseName, task.getId());
        assertTrue(expenses.isEmpty());
    }

}
