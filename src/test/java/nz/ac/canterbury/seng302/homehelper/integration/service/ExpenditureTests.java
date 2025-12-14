package nz.ac.canterbury.seng302.homehelper.integration.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseCategory;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.ExpenseRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.TaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class ExpenditureTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private RenovationService renovationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TaskRepository taskRepository;

    private Renovation renovation;
    private Task task;
    private User user;

    LocalDate now;

    @BeforeEach
    void setUp() {
        String plaintextPassword = "Password123!";
        String encryptedPassword = (passwordEncoder.encode(plaintextPassword));
        user = new User("Test", "user", "user@test.com", encryptedPassword, encryptedPassword);
        userRepository.save(user);

        renovation = new Renovation("TestRenovationName", "TestRenovationDescription");
        task = new Task(renovation, "Task Name", "Task Description", "/test/filename");
        renovation.addTask(task);
        renovation.setOwner(user);
        renovationRepository.save(renovation);

        now = LocalDate.now();
    }

    @AfterEach
    void tearDown() {
        expenseRepository.deleteAll();
        userRepository.deleteAll();
        renovationRepository.deleteAll();
    }

    @Test
    void getExpensesByCategory_OnlyOneExpense_ReturnSingleExpense() {
        Expense expense = new Expense(task, "Test Expense", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        expenseRepository.save(expense);

        List<Expense> result = renovationService.getExpensesByCategory(renovation, ExpenseCategory.MISCELLANEOUS);
        assertEquals(1, result.size());
        assertEquals(expense, result.getFirst());
    }

    @Test
    void getExpensesByCategory_TwoExpensesSameCategory_ReturnBothExpenses() {
        Expense expense1 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense2 = new Expense(task, "Test Expense 2", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);

        List<Expense> result = renovationService.getExpensesByCategory(renovation, ExpenseCategory.MISCELLANEOUS);
        assertEquals(2, result.size());
        assertTrue(result.contains(expense1) && result.contains(expense2));
    }

    @Test
    void getExpensesByCategory_TwoExpensesDifferentCategories_ReturnCorrectExpense() {
        Expense expense1 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense2 = new Expense(task, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);

        List<Expense> result = renovationService.getExpensesByCategory(renovation, ExpenseCategory.MISCELLANEOUS);
        assertEquals(1, result.size());
        assertEquals(expense1, result.getFirst());
    }

    @Test
    void getExpensesByCategory_ManyExpensesDifferentCategories_ReturnCorrectSingleExpense() {
        Expense expense1 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense2 = new Expense(task, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        Expense expense3 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense4 = new Expense(task, "Test Expense 2", ExpenseCategory.DELIVERY, new BigDecimal("100"), now);
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);
        expenseRepository.save(expense3);
        expenseRepository.save(expense4);

        List<Expense> result = renovationService.getExpensesByCategory(renovation, ExpenseCategory.PROFESSIONAL_SERVICES);
        assertEquals(1, result.size());
        assertEquals(expense2, result.getFirst());
    }

    @Test
    void getExpensesByCategory_ManyExpensesDifferentCategories_ReturnCorrectExpenses() {
        Expense expense1 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense2 = new Expense(task, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        Expense expense3 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense4 = new Expense(task, "Test Expense 2", ExpenseCategory.DELIVERY, new BigDecimal("100"), now);
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);
        expenseRepository.save(expense3);
        expenseRepository.save(expense4);

        List<Expense> result = renovationService.getExpensesByCategory(renovation, ExpenseCategory.MISCELLANEOUS);
        assertEquals(2, result.size());
        assertTrue(result.contains(expense1) && result.contains(expense3));
    }

    @Test
    void getExpensesByCategory_NoExpensesForCategory_ReturnEmptyList() {
        Expense expense1 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense2 = new Expense(task, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        Expense expense3 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense4 = new Expense(task, "Test Expense 2", ExpenseCategory.DELIVERY, new BigDecimal("100"), now);
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);
        expenseRepository.save(expense3);
        expenseRepository.save(expense4);

        List<Expense> result = renovationService.getExpensesByCategory(renovation, ExpenseCategory.LABOUR);
        assertTrue(result.isEmpty());
    }

    @Test
    void getExpensesByCategory_ExpensesExistForOtherRenovations_ReturnOnlyThisRenovation() {
        Expense expense1 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense2 = new Expense(task, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        Expense expense3 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense4 = new Expense(task, "Test Expense 2", ExpenseCategory.DELIVERY, new BigDecimal("100"), now);
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);
        expenseRepository.save(expense3);
        expenseRepository.save(expense4);

        Renovation newRenovation = new Renovation("TestRenovationName2", "TestRenovationDescription2");
        Task newTask = new Task(newRenovation, "Task Name", "Task Description", "/test/filename");
        newRenovation.addTask(newTask);
        newRenovation.setOwner(user);
        renovationRepository.save(newRenovation);
        Expense newExpense1 = new Expense(newTask, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense newExpense2 = new Expense(newTask, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        expenseRepository.save(newExpense1);
        expenseRepository.save(newExpense2);

        List<Expense> result = renovationService.getExpensesByCategory(renovation, ExpenseCategory.MISCELLANEOUS);
        assertEquals(2, result.size());
        assertTrue(result.contains(expense1) && result.contains(expense3));
    }

    @Test
    void sumExpensesByCategory_NoExpensesForCategory_ReturnZero() {
        BigDecimal result = renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.MISCELLANEOUS);
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void sumExpensesByCategory_ExpensesExistForOtherRenovations_ReturnZero() {
        Renovation newRenovation = new Renovation("TestRenovationName2", "TestRenovationDescription2");
        Task newTask = new Task(newRenovation, "Task Name", "Task Description", "/test/filename");
        newRenovation.addTask(newTask);
        newRenovation.setOwner(user);
        renovationRepository.save(newRenovation);
        Expense newExpense1 = new Expense(newTask, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense newExpense2 = new Expense(newTask, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        expenseRepository.save(newExpense1);
        expenseRepository.save(newExpense2);

        BigDecimal result = renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.MISCELLANEOUS);
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void sumExpensesByCategory_OnlyOneExpense_ReturnValue() {
        Expense expense = new Expense(task, "Test Expense", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        expenseRepository.save(expense);

        BigDecimal result = renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.MISCELLANEOUS);
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void sumExpensesByCategory_TwoExpensesSameCategory_ReturnTotal() {
        Expense expense1 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense2 = new Expense(task, "Test Expense 2", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);

        BigDecimal result = renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.MISCELLANEOUS);
        assertEquals(new BigDecimal("200.00"), result);
    }

    @Test
    void sumExpensesByCategory_TwoExpensesDifferentCategories_ReturnCorrectValue() {
        Expense expense1 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense2 = new Expense(task, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);

        BigDecimal result = renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.MISCELLANEOUS);
        assertEquals(new BigDecimal("100.00"), result);
    }


    @Test
    void sumExpensesByCategory_ManyExpensesDifferentCategories_ReturnCorrectSingleValue() {
        Expense expense1 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense2 = new Expense(task, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        Expense expense3 = new Expense(task, "Test Expense 3", ExpenseCategory.DELIVERY, new BigDecimal("100"), now);
        Expense expense4 = new Expense(task, "Test Expense 4", ExpenseCategory.DELIVERY, new BigDecimal("100"), now);
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);
        expenseRepository.save(expense3);
        expenseRepository.save(expense4);

        BigDecimal result = renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.MISCELLANEOUS);
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void sumExpensesByCategory_ManyExpensesDifferentCategories_ReturnCorrectTotal() {
        Expense expense1 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense2 = new Expense(task, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        Expense expense3 = new Expense(task, "Test Expense 3", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense4 = new Expense(task, "Test Expense 4", ExpenseCategory.DELIVERY, new BigDecimal("100"), now);
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);
        expenseRepository.save(expense3);
        expenseRepository.save(expense4);

        BigDecimal result = renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.MISCELLANEOUS);
        assertEquals(new BigDecimal("200.00"), result);
    }

    @Test
    void sumExpensesByCategory_ManyExpensesDifferentCategories_ReturnZero() {
        Expense expense1 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense2 = new Expense(task, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        Expense expense3 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense4 = new Expense(task, "Test Expense 2", ExpenseCategory.DELIVERY, new BigDecimal("100"), now);
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);
        expenseRepository.save(expense3);
        expenseRepository.save(expense4);

        BigDecimal result = renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.LABOUR);
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void sumExpensesByCategory_ExpensesExistForOtherRenovations_ReturnOnlyThisRenovation() {
        Expense expense1 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense2 = new Expense(task, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        Expense expense3 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense4 = new Expense(task, "Test Expense 2", ExpenseCategory.DELIVERY, new BigDecimal("100"), now);
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);
        expenseRepository.save(expense3);
        expenseRepository.save(expense4);

        Renovation newRenovation = new Renovation("TestRenovationName2", "TestRenovationDescription2");
        Task newTask = new Task(newRenovation, "Task Name", "Task Description", "/test/filename");
        newRenovation.addTask(newTask);
        newRenovation.setOwner(user);
        renovationRepository.save(newRenovation);
        Expense newExpense1 = new Expense(newTask, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense newExpense2 = new Expense(newTask, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        expenseRepository.save(newExpense1);
        expenseRepository.save(newExpense2);

        BigDecimal result = renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.MISCELLANEOUS);
        assertEquals(new BigDecimal("200.00"), result);
    }

    @Test
    void sumExpensesByCategory_DecimalExpenses_ReturnCorrectSum() {
        Expense expense1 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100.69"), now);
        Expense expense2 = new Expense(task, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        Expense expense3 = new Expense(task, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100.42"), now);
        Expense expense4 = new Expense(task, "Test Expense 2", ExpenseCategory.DELIVERY, new BigDecimal("100"), now);
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);
        expenseRepository.save(expense3);
        expenseRepository.save(expense4);

        Renovation newRenovation = new Renovation("TestRenovationName2", "TestRenovationDescription2");
        Task newTask = new Task(newRenovation, "Task Name", "Task Description", "/test/filename");
        newRenovation.addTask(newTask);
        newRenovation.setOwner(user);
        renovationRepository.save(newRenovation);
        Expense newExpense1 = new Expense(newTask, "Test Expense 1", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense newExpense2 = new Expense(newTask, "Test Expense 2", ExpenseCategory.PROFESSIONAL_SERVICES, new BigDecimal("100"), now);
        expenseRepository.save(newExpense1);
        expenseRepository.save(newExpense2);

        BigDecimal result = renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.MISCELLANEOUS);
        assertEquals(new BigDecimal("201.11"), result);
    }


}
