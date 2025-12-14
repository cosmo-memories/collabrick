package nz.ac.canterbury.seng302.homehelper.repository.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseCategory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Expense repository accessor using Spring's @link{CrudRepository}.
 */
public interface ExpenseRepository extends CrudRepository<Expense, Long> {

    /**
     * Gets all expenses
     *
     * @return a list of all expenses
     */
    List<Expense> findAll();

    /**
     * Finds an expense by its id
     *
     * @param id the id of the expense
     * @return An optional containing the expense if found, otherwise empty.
     */
    @Query("SELECT e from Expense e WHERE e.id = :id")
    Optional<Expense> findById(long id);

    /**
     * Finds an expense by its exact name (case-insensitive) and its task id.
     *
     * @param name   The name of the expense.
     * @param taskId The id of the task.
     * @return A list containing the expense/s, returns an empty list if none found
     */
    @Query("Select e FROM Expense e WHERE LOWER(e.expenseName) = Lower(:name) AND e.task.id = :taskId")
    List<Expense> findByNameAndTask(String name, long taskId);

    /**
     * Find all Expenses for the given Renovation in the given ExpenseCategory.
     *
     * @param renovation Renovation
     * @param category   ExpenseCategory
     * @return List of Expenses
     */
    @Query("SELECT e FROM Expense e WHERE (e.task.renovation = :renovation) AND (e.expenseCategory = :category)")
    List<Expense> findByRenovationAndCategory(Renovation renovation, ExpenseCategory category);

    /**
     * Sum all Expenses for the given Renovation in the given ExpenseCategory.
     *
     * @param renovation Renovation
     * @param category   ExpenseCategory
     * @return List of Expenses
     */
    @Query("SELECT SUM(e.expenseCost) FROM Expense e WHERE (e.task.renovation = :renovation) AND (e.expenseCategory = :category)")
    BigDecimal sumByRenovationAndCategory(Renovation renovation, ExpenseCategory category);

    /**
     * Find all Expenses for the given Renovation.
     *
     * @param renovationId Renovation ID
     * @return List of Expenses
     */
    @Query("SELECT e FROM Expense e WHERE e.task IN (SELECT t from Task t WHERE t.renovation.id = :renovationId)")
    List<Expense> findByRenovationId(long renovationId);
}
