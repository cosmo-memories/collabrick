package nz.ac.canterbury.seng302.homehelper.model.ai;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Expense;

/**
 * A lightweight DTO representing a renovation expense, formatted for AI context generation.
 *
 * @param name     The name of the expense.
 * @param category The category of the expense (e.g., Labour, Material).
 * @param cost     The cost of the expense, formatted as a string.
 * @param date     The date the expense was recorded.
 */
public record ExpenseAiView(
        String name,
        String category,
        String cost,
        String date
) {
    public ExpenseAiView(Expense expense) {
        this(
                expense.getExpenseName(),
                expense.getExpenseCategory(),
                expense.getExpenseCost().toString(),
                expense.getExpenseDate().toString()
        );
    }
}
