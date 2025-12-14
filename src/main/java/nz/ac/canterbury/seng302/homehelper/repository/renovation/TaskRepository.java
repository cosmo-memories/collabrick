package nz.ac.canterbury.seng302.homehelper.repository.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Task repository accessor using Spring's @link{CrudRepository}.
 */
public interface TaskRepository extends PagingAndSortingRepository<Task, Integer> {

    /**
     * Finds a task by its exact name (case-insensitive) and its renovation id.
     *
     * @param name         The name of the task.
     * @param renovationId The id of the renovation.
     * @return An optional containing the task if found, otherwise empty.
     */
    @Query("Select t FROM Task t WHERE LOWER(t.name) = Lower(:name) AND t.renovation.id = :renovationId")
    Optional<Task> findByNameAndRenovation(String name, Long renovationId);

    /**
     * Retrieves a paginated list of Task entities associated with a given renovation id.
     *
     * @param renovationId the id of the renovation
     * @param pageable     the pagination and sorting information
     * @return a Page containing the tasks associated with the specified renovation id.
     */
    Page<Task> findByRenovationId(Long renovationId, Pageable pageable);

    /**
     * Retrieves a paginated list of Task entities associated with a given renovation id and a list of task states.
     *
     * @param renovation_id
     * @param state
     * @param pageable
     * @return
     */
    Page<Task> findByRenovationIdAndStateIn(long renovation_id, Collection<TaskState> state, Pageable pageable);


    /**
     * Finds a task by its id
     *
     * @param id the id of the task
     * @return An optional containing the task if found, otherwise empty.
     */
    @Query("SELECT t from Task  t WHERE t.id = :id")
    Optional<Task> findTaskById(long id);

    /**
     * Finds tasks by its renovation ID and between a date range
     *
     * @param startDate    the start of the date range
     * @param endDate      the end of the date range
     * @param renovationId the id of the renovation
     * @return An optional containing the task if found, otherwise empty.
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate AND t.renovation.id = :renovationId")
    List<Task> findTasksByDateRange(LocalDate startDate, LocalDate endDate, Long renovationId);

    /**
     * Finds tasks by its renovation ID and between a date range, filters the tasks so that only tasks with relevant states are returned.
     *
     * @param startDate    the start of the date range
     * @param endDate      the end of the date range
     * @param renovationId the id of the renovation
     * @param states       a collection of wanted task states
     * @return An optional containing the task if found, otherwise empty.
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate AND t.renovation.id = :renovationId AND t.state IN :states")
    List<Task> findTasksByDateRangeAndStates(LocalDate startDate, LocalDate endDate, Long renovationId, Collection<TaskState> states);

    /**
     * Counts tasks that have not been cancelled or completed
     *
     * @param renovationId the id of the relevant renovation
     * @param completed    completed task state
     * @param cancelled    cancelled task state
     * @return the number of tasks that are yet to be completed
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.renovation.id = :renovationId  AND t.state != :completed AND t.state != :cancelled")
    Long countUncompletedTasksByRenovationId(Long renovationId, TaskState completed, TaskState cancelled);

    /**
     * Return list of tasks belonging to given user's renovations that are:
     * due within 7 days and not marked 'completed' or 'cancelled'.
     *
     * @param userId        User ID
     * @param now           Current date
     * @param weekFromNow   7 days from current date
     * @return              Task list
     */
    @Query("""
    SELECT t FROM Task t
    WHERE t.dueDate BETWEEN :now AND :weekFromNow
      AND t.renovation.id IN (
          SELECT m.renovation.id
          FROM RenovationMember m
          WHERE m.user.id = :userId
      )
      AND t.state NOT IN ('COMPLETED', 'CANCELLED')
    ORDER BY t.dueDate
""")
    List<Task> findUpcomingTasks(
            @Param("userId") Long userId,
            @Param("now") LocalDate now,
            @Param("weekFromNow") LocalDate weekFromNow
    );


    /**
     * Count the total number of completed tasks for a user's renovations.
     * @param user          User
     * @param state         Task State
     * @return              Integer
     */
    @Query("""
        SELECT COUNT(t) FROM Task t
            WHERE t.state = :state
            AND t.renovation IN (
            SELECT r FROM Renovation r
            WHERE r.owner = :user
            OR EXISTS (
                SELECT 1 FROM RenovationMember rm
                WHERE rm MEMBER OF r.members
                AND rm.user = :user
            )
        )
    """)
    int sumCompletedTasksByUser(User user, TaskState state);


}
