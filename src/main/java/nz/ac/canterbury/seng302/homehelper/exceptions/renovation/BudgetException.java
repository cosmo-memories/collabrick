package nz.ac.canterbury.seng302.homehelper.exceptions.renovation;

import java.util.Map;

/**
 * Exception thrown when one or more budget-related validation errors occur.
 */
public class BudgetException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    /**
     * Creates a new BudgetException with the specified field errors.
     *
     * @param fieldErrors a map containing field names as keys and
     *                    their corresponding error messages as values.
     */
    public BudgetException(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
