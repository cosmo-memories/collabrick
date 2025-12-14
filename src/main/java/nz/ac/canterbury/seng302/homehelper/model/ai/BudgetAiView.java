package nz.ac.canterbury.seng302.homehelper.model.ai;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Budget;

/**
 * A lightweight DTO representing a renovation budget, formatted for AI context generation.
 *
 * @param miscellaneous       Budget for miscellaneous items.
 * @param material            Budget for materials.
 * @param labour              Budget for labour.
 * @param equipment           Budget for equipment.
 * @param professionalService Budget for professional services.
 * @param permit              Budget for permits.
 * @param cleanup             Budget for cleanup.
 * @param delivery            Budget for delivery.
 */
public record BudgetAiView(
        String miscellaneous,
        String material,
        String labour,
        String equipment,
        String professionalService,
        String permit,
        String cleanup,
        String delivery
) {
    public BudgetAiView(Budget budget) {
        this(
                budget.getMiscellaneousBudget().toString(),
                budget.getMaterialBudget().toString(),
                budget.getLabourBudget().toString(),
                budget.getEquipmentBudget().toString(),
                budget.getProfessionalServiceBudget().toString(),
                budget.getPermitBudget().toString(),
                budget.getCleanupBudget().toString(),
                budget.getDeliveryBudget().toString()
        );
    }
}
