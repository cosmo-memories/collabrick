package nz.ac.canterbury.seng302.homehelper.integration.controller.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseCategory;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ExpenseTests {

    @Autowired
    private RenovationService renovationService;

    private Task task;
    private final LocalDate now = LocalDate.now();

    @BeforeEach
    void setUp() {
        task = new Task(null, "Test Task", "Description", "icon.png");
    }

    @Test
    public void sumExpenseTest_SingleExpense_Integer() {
        Expense expense = new Expense(task, "Test Expense", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        List<Expense> expenses = new ArrayList<>();
        expenses.add(expense);
        assertEquals(new BigDecimal("100.00"), renovationService.getExpenseTotal(expenses));
    }

    @Test
    public void sumExpenseTest_SingleExpense_Decimal() {
        Expense expense = new Expense(task, "Test Expense", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100.01"), now);
        List<Expense> expenses = new ArrayList<>();
        expenses.add(expense);
        assertEquals(new BigDecimal("100.01"), renovationService.getExpenseTotal(expenses));
    }

    @Test
    public void sumExpenseTest_SeveralExpenseS_AllIntegers() {
        Expense expense1 = new Expense(task, "Test Expense", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), now);
        Expense expense2 = new Expense(task, "Test Expense", ExpenseCategory.MISCELLANEOUS, new BigDecimal("200"), now);
        Expense expense3 = new Expense(task, "Test Expense", ExpenseCategory.MISCELLANEOUS, new BigDecimal("300"), now);
        List<Expense> expenses = new ArrayList<>();
        expenses.add(expense1);
        expenses.add(expense2);
        expenses.add(expense3);
        assertEquals(new BigDecimal("600.00"), renovationService.getExpenseTotal(expenses));
    }

    @Test
    public void sumExpenseTest_SeveralExpenseS_AllDecimals() {
        Expense expense1 = new Expense(task, "Test Expense", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100.00"), now);
        Expense expense2 = new Expense(task, "Test Expense", ExpenseCategory.MISCELLANEOUS, new BigDecimal("200.01"), now);
        Expense expense3 = new Expense(task, "Test Expense", ExpenseCategory.MISCELLANEOUS, new BigDecimal("300.04"), now);
        List<Expense> expenses = new ArrayList<>();
        expenses.add(expense1);
        expenses.add(expense2);
        expenses.add(expense3);
        assertEquals(new BigDecimal("600.05"), renovationService.getExpenseTotal(expenses));
    }

    @Test
    public void sumExpenseTest_SeveralExpenseS_MixedValues() {
        Expense expense1 = new Expense(task, "Test Expense", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100.00"), now);
        Expense expense2 = new Expense(task, "Test Expense", ExpenseCategory.MISCELLANEOUS, new BigDecimal("200"), now);
        Expense expense3 = new Expense(task, "Test Expense", ExpenseCategory.MISCELLANEOUS, new BigDecimal("300.01"), now);
        List<Expense> expenses = new ArrayList<>();
        expenses.add(expense1);
        expenses.add(expense2);
        expenses.add(expense3);
        assertEquals(new BigDecimal("600.01"), renovationService.getExpenseTotal(expenses));
    }


}
