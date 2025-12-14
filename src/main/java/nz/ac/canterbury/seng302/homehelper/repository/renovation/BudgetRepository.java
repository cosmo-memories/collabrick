package nz.ac.canterbury.seng302.homehelper.repository.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Budget;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BudgetRepository extends CrudRepository<Budget, Long> {

    /**
     * Finds a budget by its id
     *
     * @param id the id of the budget
     * @return An optional containing the budget if found, otherwise empty.
     */
    @Query("SELECT b FROM Budget b WHERE b.id = :id")
    Optional<Budget> findById(long id);


    /**
     * Finds a budget by its renovation's id
     *
     * @param renovationId the id of the renovation mapped to the budget
     * @return An optional containing the budget if found, otherwise empty.
     */
    @Query("SELECT b FROM Budget b WHERE b.renovation.id = :renovationId")
    Optional<Budget> findByRenovationId(long renovationId);
}
