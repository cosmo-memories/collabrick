package nz.ac.canterbury.seng302.homehelper.integration.controller.renovation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseCategory;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ExpenseIntegrationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RenovationRepository renovationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession authenticationSession;
    private Renovation renovation;
    private Task task;
    private long renovationId;
    private long taskId;
    @Autowired
    private TaskService taskService;

    @BeforeEach
    public void setUp() throws Exception {
        renovationRepository.deleteAll();
        userRepository.deleteAll();

        String plaintextPassword = "Password123!";
        String encryptedPassword = (passwordEncoder.encode(plaintextPassword));
        User user = new User("Test", "user", "user@test.com", encryptedPassword, encryptedPassword);
        user.setActivated(true);
        userRepository.save(user);

        renovation = new Renovation("TestRenovationName", "TestRenovationDescription");
        task = new Task(renovation, "Task Name", "Task Description", "/test/filename");

        renovation.addTask(task);
        renovation.setOwner(user);
        renovationRepository.save(renovation);

        renovationId = renovation.getId();
        taskId = task.getId();

        MvcResult result = mockMvc.perform(post("/do_login")
                        .param("email", "user@test.com")
                        .param("password", "Password123!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        authenticationSession = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(authenticationSession);
        assertFalse(authenticationSession.isInvalid());
    }

    @Test
    void deleteExpense_OneExpenseOnTask_Success() throws Exception {
        ExpenseCategory expenseCategory = ExpenseCategory.fromDisplayName("Professional Services");
        Expense expense = new Expense(task, "Pizza", expenseCategory, new BigDecimal("4.20"), LocalDate.parse("1993-01-16"));
        taskService.saveExpense(expense);
        long expenseId = expense.getId();

        mockMvc.perform(MockMvcRequestBuilders.post("/task/" + task.getId() + "/expense/" + expenseId + "/delete")
                        .session(authenticationSession)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(taskService.findExpenseById(expenseId).isEmpty());
    }

    @Test
    void deleteExpense_ManyExpensesOnTask_Success() throws Exception {
        ExpenseCategory expenseCategory = ExpenseCategory.fromDisplayName("Professional Services");
        Expense expense1 = new Expense(task, "Pizza 1", expenseCategory, new BigDecimal("4.20"), LocalDate.parse("1993-01-16"));
        taskService.saveExpense(expense1);
        long expenseId = expense1.getId();
        Expense expense2 = new Expense(task, "Pizza 2", expenseCategory, new BigDecimal("4.20"), LocalDate.parse("1993-01-16"));
        taskService.saveExpense(expense2);
        Expense expense3 = new Expense(task, "Pizza 3", expenseCategory, new BigDecimal("4.20"), LocalDate.parse("1993-01-16"));
        taskService.saveExpense(expense3);
        Expense expense4 = new Expense(task, "Pizza 4", expenseCategory, new BigDecimal("4.20"), LocalDate.parse("1993-01-16"));
        taskService.saveExpense(expense4);

        mockMvc.perform(MockMvcRequestBuilders.post("/task/" + task.getId() + "/expense/" + expenseId + "/delete")
                        .session(authenticationSession)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(taskService.findExpenseById(expenseId).isEmpty());
        assertEquals(3, task.getExpenses().size());
    }

}
