package nz.ac.canterbury.seng302.homehelper.model.renovation;

/**
 * A Data Transfer Object (DTO) representing an expense entry submitted from the client (usually via JSON).
 * Used for receiving expense information from the frontend.
 */
public class ExpenseDto {

    /**
     * The name or description of the expense item.
     */
    public String name;

    /**
     * The category of the expense (e.g., Material, Labour).
     */
    public String category;

    /**
     * The date the expense was incurred, in ISO format (yyyy-MM-dd).
     */
    public String date;

    /**
     * The price or cost of the expense item.
     */
    public double price;

    /**
     * Constructs a new ExpenseDto with the specified values.
     *
     * @param name     the name of the expense
     * @param category the category of the expense
     * @param date     the date the expense was incurred
     * @param price    the cost of the expense
     */
    public ExpenseDto(String name, String category, String date, double price) {
        this.name = name;
        this.category = category;
        this.date = date;
        this.price = price;
    }
}
