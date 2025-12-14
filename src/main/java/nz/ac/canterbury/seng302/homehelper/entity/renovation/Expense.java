package nz.ac.canterbury.seng302.homehelper.entity.renovation;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing an expense object
 */
@Entity
public class Expense {

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String expenseName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExpenseCategory expenseCategory;

    @Column(nullable = false)
    private BigDecimal expenseCost;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LiveUpdate> liveUpdates = new ArrayList<>();

    /**
     * JPA constructor
     */
    public Expense() {
    }

    /**
     * Creates a new Expense object with default state
     *
     * @param task        the task that the expense belongs to
     * @param expenseName the name of the expense
     * @param expenseCost the price of the expense
     */
    public Expense(Task task, String expenseName, ExpenseCategory expenseCategory, BigDecimal expenseCost, LocalDate expenseDate) {
        this.task = task;
        this.expenseName = expenseName;
        this.expenseCategory = expenseCategory;
        this.expenseCost = expenseCost;
        this.expenseDate = expenseDate;
    }

    /**
     * Gets the id of the expense
     *
     * @return the expense's id
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the id of the expense
     *
     * @param id the id of the expense
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the task of the expense
     *
     * @return the expense's task
     */
    public Task getTask() {
        return task;
    }

    /**
     * Sets the task of the expense
     *
     * @param task the task of the expense
     */
    public void setTask(Task task) {
        this.task = task;
    }

    /**
     * Gets the name of the expense
     *
     * @return the expense's name
     */
    public String getExpenseName() {
        return expenseName;
    }

    /**
     * Sets the name of the expense
     *
     * @param expenseName the name of the expense
     */
    public void setExpenseName(String expenseName) {
        this.expenseName = expenseName;
    }

    /**
     * Gets the category of the expense
     *
     * @return the expense's category
     */
    public String getExpenseCategory() {
        return expenseCategory.getDisplayName();
    }

    /**
     * Sets the category of the expense
     *
     * @param expenseCategory the category of the expense
     */
    public void setExpenseCategory(String expenseCategory) {
        this.expenseCategory = ExpenseCategory.valueOf(expenseCategory);
    }

    /**
     * Gets the cost of the expense
     *
     * @return the expense's cost
     */
    public BigDecimal getExpenseCost() {
        return expenseCost;
    }

    /**
     * Sets the cost of the expense
     *
     * @param expenseCost the cost of the expense
     */
    public void setExpenseCost(BigDecimal expenseCost) {
        this.expenseCost = expenseCost;
    }

    /**
     * Gets the date of creation of the expense
     *
     * @return the expense's date of creation
     */
    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    /**
     * Sets the date of creation of the expense
     *
     * @param expenseDate the date of creation of the expense
     */
    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }
}

