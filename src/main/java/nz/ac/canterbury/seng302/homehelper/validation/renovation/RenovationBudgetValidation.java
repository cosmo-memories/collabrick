package nz.ac.canterbury.seng302.homehelper.validation.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Budget;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.BudgetException;
import nz.ac.canterbury.seng302.homehelper.model.renovation.BudgetDto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class responsible for validating renovation budget inputs.
 */
public class RenovationBudgetValidation {

    public static final String BUDGET_INVALID_PRICE_MESSAGE = "%s budget must be a positive number in the form 5.99";
    public static final String BUDGET_PRICE_TOO_LARGE_MESSAGE = "%s budget must be less than $10,000,000";
    private static final BigDecimal MAX_VALUE = new BigDecimal("10000000");
    private static final String PRICE_REGEX = "^[0-9]+(\\.[0-9]{1,2})?$";

    /**
     * Mapping between budget category labels and the corresponding
     * BudgetDto getter functions.
     */
    private static final Map<String, Function<BudgetDto, String>> budgetValueSuppliers = Map.of(
            "Miscellaneous", BudgetDto::miscellaneousBudget,
            "Material", BudgetDto::materialBudget,
            "Labour", BudgetDto::labourBudget,
            "Equipment", BudgetDto::equipmentBudget,
            "Professional Service", BudgetDto::professionalServiceBudget,
            "Permit", BudgetDto::permitBudget,
            "Cleanup", BudgetDto::cleanupBudget,
            "Delivery", BudgetDto::deliveryBudget
    );

    /**
     * Validates a single budget field against format and value constraints.
     *
     * @param label  the display name of the budget field
     * @param budget the value to validate
     * @return an empty string if valid, otherwise an error message
     */
    private static String validateBudgetField(String label, String budget) {
        if (budget == null || !budget.matches(PRICE_REGEX)) {
            return String.format(BUDGET_INVALID_PRICE_MESSAGE, label);
        }

        BigDecimal value = new BigDecimal(budget);
        if (BigDecimal.ZERO.compareTo(value) > 0) {
            return String.format(BUDGET_INVALID_PRICE_MESSAGE, label);
        }
        if (value.compareTo(MAX_VALUE) >= 0) {
            return String.format(BUDGET_PRICE_TOO_LARGE_MESSAGE, label);
        }
        return "";
    }

    /**
     * Validates all budget fields in the provided BudgetDto.
     *
     * @param budgetDto the DTO containing budget values as strings
     * @return a Budget entity with validated numeric values
     * @throws BudgetException if one or more budget fields are invalid
     */
    public static Budget validateBudgetField(BudgetDto budgetDto) {
        // Extract field values from the DTO
        Map<String, String> budgetFields = budgetValueSuppliers.entrySet()
                .stream()
                .map(entry -> {
                    String value = entry.getValue().apply(budgetDto);
                    return Map.entry(entry.getKey(), (value == null || value.trim().isEmpty()) ? "0" : value.trim());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Validate each field and collect errors
        Map<String, String> budgetFieldErrors = budgetFields.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey(), validateBudgetField(entry.getKey(), entry.getValue())))
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!budgetFieldErrors.isEmpty()) {
            throw new BudgetException(budgetFieldErrors);
        }
        return new Budget(
                new BigDecimal(budgetFields.get("Miscellaneous")),
                new BigDecimal(budgetFields.get("Material")),
                new BigDecimal(budgetFields.get("Labour")),
                new BigDecimal(budgetFields.get("Equipment")),
                new BigDecimal(budgetFields.get("Professional Service")),
                new BigDecimal(budgetFields.get("Permit")),
                new BigDecimal(budgetFields.get("Cleanup")),
                new BigDecimal(budgetFields.get("Delivery"))
        );
    }
}
