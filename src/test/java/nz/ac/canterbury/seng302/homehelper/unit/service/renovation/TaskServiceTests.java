package nz.ac.canterbury.seng302.homehelper.unit.service.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseCategory;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.ExpenseRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.TaskRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTests {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private Expense expense;

    @BeforeEach
    void setUp() {
        task = new Task(null, "Test Task", "Description", "icon.png");
        task.setId(1L);
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = String.format("%d-%02d-%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        expense = new Expense(task, "Test Expense", ExpenseCategory.MISCELLANEOUS, new BigDecimal("100"), LocalDate.parse(formattedDate));
        expense.setId(2L);
    }

    @Test
    void findExpenseById_existingId_returnsExpense() {
        when(expenseRepository.findById(2L)).thenReturn(Optional.of(expense));

        Optional<Expense> resultExpense = taskService.findExpenseById(2L);

        assertTrue(resultExpense.isPresent());
        assertEquals(expense, resultExpense.get());
        verify(expenseRepository).findById(2L);
    }

    @Test
    void findExpenseById_nonExistingId_returnsEmpty() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Expense> resultExpense = taskService.findExpenseById(99L);

        assertFalse(resultExpense.isPresent());
        verify(expenseRepository).findById(99L);
    }

    @Test
    void findExpenseByNameAndTask_matching_returnsList() {
        when(expenseRepository.findByNameAndTask("test expense", 1L))
                .thenReturn(Collections.singletonList(expense));

        List<Expense> resultExpenses = taskService.findExpenseByNameAndTask("test expense", 1L);

        assertEquals(1, resultExpenses.size());
        assertEquals(expense, resultExpenses.getFirst());
        verify(expenseRepository).findByNameAndTask("test expense", 1L);
    }

    @Test
    void saveExpenseToTask_validIds_savesExpense() {
        taskService.saveExpense(expense);

        assertTrue(task.getExpenses().contains(expense));
        verify(expenseRepository).save(expense);
    }

    @Test
    void removeExpenseFromTask_validIds_expenseRemoved() {
        task.addExpense(expense);
        when(taskRepository.findTaskById(1L)).thenReturn(Optional.of(task));
        when(expenseRepository.findById(2L)).thenReturn(Optional.of(expense));

        taskService.removeExpenseFromTask(2L, 1L);

        assertFalse(task.getExpenses().contains(expense));
        verify(expenseRepository).delete(expense);
    }


}

