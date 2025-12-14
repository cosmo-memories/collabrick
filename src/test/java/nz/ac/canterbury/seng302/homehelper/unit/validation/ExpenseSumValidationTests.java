package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpenseSumValidationTests {

    Renovation reno;
    Task task;

    @BeforeEach
    public void setUp() {
        reno = new Renovation("Test Reno", "Test Description");
        task = new Task(reno, "Test Task", "Test Description", "test.png");
    }


    @Test
    public void expenseSum_OnePositiveInteger() {
        Expense expenseA = new Expense(task, "Test Expense A", ExpenseCategory.EQUIPMENT, new BigDecimal("10"), LocalDate.now());
        task.addExpense(expenseA);

        assertEquals(new BigDecimal("10"), task.getTotalCost());
    }

    @Test
    public void expenseSum_TwoPositiveIntegers() {
        Expense expenseA = new Expense(task, "Test Expense A", ExpenseCategory.EQUIPMENT, new BigDecimal("10"), LocalDate.now());
        Expense expenseB = new Expense(task, "Test Expense B", ExpenseCategory.EQUIPMENT, new BigDecimal("20"), LocalDate.now());
        task.addExpense(expenseA);
        task.addExpense(expenseB);

        assertEquals(new BigDecimal("30"), task.getTotalCost());
    }

    @Test
    public void expenseSum_ThreePositiveIntegers() {
        Expense expenseA = new Expense(task, "Test Expense A", ExpenseCategory.EQUIPMENT, new BigDecimal("10"), LocalDate.now());
        Expense expenseB = new Expense(task, "Test Expense B", ExpenseCategory.EQUIPMENT, new BigDecimal("20"), LocalDate.now());
        Expense expenseC = new Expense(task, "Test Expense C", ExpenseCategory.EQUIPMENT, new BigDecimal("30"), LocalDate.now());
        task.addExpense(expenseA);
        task.addExpense(expenseB);
        task.addExpense(expenseC);

        assertEquals(new BigDecimal("60"), task.getTotalCost());
    }


    /**
     * Valid lists of expense values for testing.
     *
     * @return Stream of ArrayLists containing valid expense BigDecimals
     */
    static Stream<ArrayList<BigDecimal>> validExpenses() {
        return Stream.of(
                new ArrayList<>(List.of(new BigDecimal("10.00"), new BigDecimal("20.99"))),
                new ArrayList<>(List.of(new BigDecimal("11.11"), new BigDecimal("42.42"))),
                new ArrayList<>(List.of(new BigDecimal("1.00"), new BigDecimal("2.00"), new BigDecimal("3.00"), new BigDecimal("4.00"), new BigDecimal("5.00"))),
                new ArrayList<>(List.of(new BigDecimal("1.01"), new BigDecimal("2.02"), new BigDecimal("3.03"), new BigDecimal("4.04"), new BigDecimal("5.05"))),
                new ArrayList<>(List.of(new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"))),
                new ArrayList<>(List.of(new BigDecimal("20.45"), new BigDecimal("22.65"), new BigDecimal("65.22"))),
                new ArrayList<>(List.of(new BigDecimal("1000.00"), new BigDecimal("2000.00"), new BigDecimal("3000.00"))),
                new ArrayList<>(List.of(new BigDecimal("123.45"))),
                new ArrayList<>(List.of(new BigDecimal("1.00"), new BigDecimal("1.00"), new BigDecimal("1"))),
                new ArrayList<>(List.of(new BigDecimal("0.99"), new BigDecimal("0.01"), new BigDecimal("0.50"))),
                new ArrayList<>(List.of(new BigDecimal("0.00"), new BigDecimal("1000000.00"))),
                new ArrayList<>(List.of(new BigDecimal("0.00"))),
                new ArrayList<>(List.of(new BigDecimal("0.01"))),
                new ArrayList<>(List.of(new BigDecimal("999999.99"))),
                new ArrayList<>(),
                new ArrayList<>(List.of(new BigDecimal("0"), new BigDecimal("43636"), new BigDecimal("1.11")))
        );
    }

    // Summing the total value of all expenses
    @ParameterizedTest
    @MethodSource("validExpenses")
    public void expenseSum_ValidExpenses(ArrayList<BigDecimal> expenses) {
        for (BigDecimal cost : expenses) {
            task.addExpense(new Expense(task, "Test Expense", ExpenseCategory.EQUIPMENT, cost, LocalDate.now()));
        }

        BigDecimal expectedResult = expenses.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal actualResult = task.getTotalCost();

        assertEquals(0, expectedResult.compareTo(actualResult));
    }

    // Counting the total number of individual expense items
    @ParameterizedTest
    @MethodSource("validExpenses")
    public void expenseCount_ValidExpenses(ArrayList<BigDecimal> expenses) {
        for (BigDecimal cost : expenses) {
            task.addExpense(new Expense(task, "Test Expense", ExpenseCategory.EQUIPMENT, cost, LocalDate.now()));
        }
        assertEquals(expenses.size(), task.getExpenseCount());
    }


}
