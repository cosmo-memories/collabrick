package nz.ac.canterbury.seng302.homehelper.service.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.TaskExpenseException;
import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.ExpenseRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.TaskRepository;
import nz.ac.canterbury.seng302.homehelper.validation.renovation.RenovationValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


/**
 * Service class for Task, defined by the @link{Service} annotation.
 * This class links automatically with @link{TaskRepository} and @link{ExpenseRepository}, see
 * the @link{Autowired} annotation below
 */
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ExpenseRepository expenseRepository;
    private final Logger logger = LoggerFactory.getLogger(TaskService.class);

    /**
     * Constructs a {@code RenovationService} with the given repositories.
     *
     * @param taskRepository    The repository for handling tasks.
     * @param expenseRepository The repository for handling expenses.
     */
    @Autowired
    public TaskService(TaskRepository taskRepository, ExpenseRepository expenseRepository) {
        this.taskRepository = taskRepository;
        this.expenseRepository = expenseRepository;
    }

    /**
     * Returns expense by ID
     *
     * @param id ID of expense to be found
     * @return returns expense else returns Optional.empty()
     */
    public Optional<Expense> findExpenseById(long id) {
        return expenseRepository.findById(id);
    }

    /**
     * Returns a list of expenses of a task by searching its name
     *
     * @param name   the name of the searched expense/s
     * @param taskId the ID of the task the expense belongs to
     * @return a list of expenses that match the searched name for a task
     */
    public List<Expense> findExpenseByNameAndTask(String name, long taskId) {
        return expenseRepository.findByNameAndTask(name, taskId);
    }

    /**
     * Adds a new expense to the database.
     * Make sure all params are valid
     *
     * @param expense The expense to add
     */
    public void saveExpense(Expense expense) {
        String expenseNameError = RenovationValidation.validateExpenseName(expense.getExpenseName());
        String expenseDateError = RenovationValidation.validateExpenseDate(expense.getExpenseDate().toString());
        String expensePriceError = RenovationValidation.validateExpensePrice(expense.getExpenseCost().toString());
        if (!expenseNameError.isEmpty() || !expenseDateError.isEmpty() || !expensePriceError.isEmpty()) {
            throw new TaskExpenseException(expenseNameError, expenseDateError, expensePriceError);
        }
        Task task = expense.getTask();
        expenseRepository.save(expense);
        task.addExpense(expense);
    }

    /**
     * Removes an existing expense from the database
     *
     * @param expenseId The expense to be removed
     * @param taskId    The task that the expense to be removed belongs to
     */
    public void removeExpenseFromTask(long expenseId, long taskId) {
        Task task = taskRepository.findTaskById(taskId).orElseThrow();
        Expense expense = expenseRepository.findById(expenseId).orElseThrow();

        task.removeExpense(expense);
        expenseRepository.delete(expense);
    }

    /**
     * Finds a task in the task repo by id
     *
     * @param id tasks id
     * @return the task object
     */
    public Optional<Task> findTaskById(long id) {
        return taskRepository.findTaskById(id);
    }

    /**
     * Counts the amount of tasks that are yet to be completed for a renovation
     *
     * @param renovationId the id of the relevant renovation
     * @return the count of the tasks yet to be completed for a renovation
     */
    public Long countUncompletedTasksByRenovationId(long renovationId) {
        return taskRepository.countUncompletedTasksByRenovationId(renovationId, TaskState.COMPLETED, TaskState.CANCELLED);
    }

    /**
     * Return list of tasks belonging to given user's renovations that are: due within 7 days and not marked 'completed' or 'cancelled'.
     * @param userId        User ID
     * @return              Task list
     */
    public List<Task> findUpcomingTasks(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate weekFromNow = now.plusDays(7);

        return taskRepository.findUpcomingTasks(userId, now, weekFromNow);
    }

    /**
     * Count the total number of completed tasks for a user's renovations.
     * @param user          User
     * @return              Integer
     */
    public int sumCompletedTasksByUser(User user) {
        return taskRepository.sumCompletedTasksByUser(user, TaskState.COMPLETED);
    }

    /**
     * Appends rooms to task through a list of room names
     *
     * @param task Task to append room to
     * @param roomNames list of room names to match with existing renovation rooms
     */
    public void addRoomsToTaskThroughRoomNames(Task task, List<String> roomNames) {
        Renovation renovation = task.getRenovation();
        renovation.getRooms().forEach(room -> {
            if (roomNames.contains(room.getName())) {
                task.addRoom(room);
            }
        });
    }
}
