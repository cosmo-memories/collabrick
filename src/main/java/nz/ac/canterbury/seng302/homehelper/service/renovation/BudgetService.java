package nz.ac.canterbury.seng302.homehelper.service.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Budget;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseCategory;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service class for Budget, defined by the @link{Service} annotation.
 * This class links automatically with @link{BudgetRepository}, see
 * the @link{Autowired} annotation below
 */
@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;

    /**
     * Constructs a {@code BudgetService} with the given repositories.
     *
     * @param budgetRepository The repository for handling budgets
     */
    @Autowired
    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    /**
     * Retrieves a budget by its unique identifier.
     *
     * @param id The unique ID of the budget.
     * @return An optional containing the budget if found, otherwise empty.
     */
    public Optional<Budget> findById(long id) {
        return budgetRepository.findById(id);
    }

    /**
     * Finds a budget by its renovation's id
     *
     * @param renovationId the id of the renovation mapped to the budget
     * @return An optional containing the budget if found, otherwise empty.
     */
    public Optional<Budget> findByRenovationId(long renovationId) {
        return budgetRepository.findByRenovationId(renovationId);
    }

    /**
     * Saves a new budget.
     */
    public Budget saveBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    /**
     * Updates a specific budget category for a given budget.
     *
     * @param budgetId  The ID of the budget to update.
     * @param category  The category to update (from ExpenseCategory).
     * @param newAmount The new amount to set.
     * @return An optional containing the updated budget if found, otherwise empty.
     */
    public Optional<Budget> updateBudgetCategory(long budgetId, ExpenseCategory category, BigDecimal newAmount) {
        Optional<Budget> budgetOptional = budgetRepository.findById(budgetId);

        if (budgetOptional.isPresent()) {
            Budget budget = budgetOptional.get();

            switch (category) {
                case MISCELLANEOUS:
                    budget.setMiscellaneousBudget(newAmount);
                    break;
                case MATERIAL:
                    budget.setMaterialBudget(newAmount);
                    break;
                case LABOUR:
                    budget.setLabourBudget(newAmount);
                    break;
                case EQUIPMENT:
                    budget.setEquipmentBudget(newAmount);
                    break;
                case PROFESSIONAL_SERVICES:
                    budget.setProfessionalServiceBudget(newAmount);
                    break;
                case PERMIT:
                    budget.setPermitBudget(newAmount);
                    break;
                case CLEANUP:
                    budget.setCleanupBudget(newAmount);
                    break;
                case DELIVERY:
                    budget.setDeliveryBudget(newAmount);
                    break;
            }

            Budget updatedBudget = budgetRepository.save(budget);
            return Optional.of(updatedBudget);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Updates the budget with the given ID using the fields from the provided updated budget.
     *
     * @param budgetId      The ID of the budget to update.
     * @param updatedBudget The budget object containing updated values.
     * @return The updated budget, or Optional.empty() if not found.
     */
    public Optional<Budget> updateBudget(long budgetId, Budget updatedBudget) {
        Optional<Budget> existingBudgetOptional = budgetRepository.findById(budgetId);

        if (existingBudgetOptional.isPresent()) {
            Budget existingBudget = existingBudgetOptional.get();

            existingBudget.setMiscellaneousBudget(updatedBudget.getMiscellaneousBudget());
            existingBudget.setMaterialBudget(updatedBudget.getMaterialBudget());
            existingBudget.setLabourBudget(updatedBudget.getLabourBudget());
            existingBudget.setEquipmentBudget(updatedBudget.getEquipmentBudget());
            existingBudget.setProfessionalServiceBudget(updatedBudget.getProfessionalServiceBudget());
            existingBudget.setPermitBudget(updatedBudget.getPermitBudget());
            existingBudget.setCleanupBudget(updatedBudget.getCleanupBudget());
            existingBudget.setDeliveryBudget(updatedBudget.getDeliveryBudget());

            Budget savedBudget = budgetRepository.save(existingBudget);
            return Optional.of(savedBudget);
        } else {
            return Optional.empty();
        }
    }
}
