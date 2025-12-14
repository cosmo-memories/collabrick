package nz.ac.canterbury.seng302.homehelper.entity.renovation;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import nz.ac.canterbury.seng302.homehelper.model.calendar.CalendarItem;
import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity class representing a task object.
 */
@Entity
public class Task implements CalendarItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 600)
    private String description;

    @Column
    private LocalDate dueDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskState state;

    @Column(nullable = false)
    private String iconFileName;

    @ManyToOne
    @JoinColumn(name = "renovation_id", nullable = false)
    private Renovation renovation;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LiveUpdate> liveUpdates = new ArrayList<>();

    @ManyToMany(fetch =  FetchType.EAGER)
    @JoinTable(
            name = "task_room",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "room_id"))
    private List<Room> rooms;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Expense> expenses = new ArrayList<>();

    /**
     * JPA required no-args constructor
     */
    protected Task() {
    }

    /**
     * Creates a new Task object with default state
     *
     * @param renovation  The renovation this task belongs to.
     * @param name        The name of the task
     * @param description The description of the task
     */
    public Task(Renovation renovation, String name, String description, String iconFileName) {
        this.renovation = renovation;
        this.name = name;
        this.description = description;
        this.state = TaskState.NOT_STARTED;
        this.iconFileName = iconFileName;
        this.rooms = new ArrayList<>();
        this.expenses = new ArrayList<>();
    }

    /**
     * Creates a new Task object with state set in constructor
     *
     * @param renovation   Renovation the task belongs to
     * @param name         Name of the task
     * @param description  Description of the task
     * @param state        Task state
     * @param iconFileName Task icon
     */
    public Task(Renovation renovation, String name, String description, TaskState state, String iconFileName) {
        this.renovation = renovation;
        this.name = name;
        this.description = description;
        this.state = state;
        this.iconFileName = iconFileName;
        this.rooms = new ArrayList<>();
        this.expenses = new ArrayList<>();
    }

    /**
     * Gets the ID of the task.
     *
     * @return The task ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the tasks ID, used for tests
     *
     * @param id The id for the task
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the name of the task.
     *
     * @return The name of the task.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of a task
     *
     * @param name the name to set the task to
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the task
     *
     * @return The description of the task
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description for a task
     *
     * @param description the description to set for the task
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Set the state for a task
     *
     * @param state the state for the task
     */
    public void setState(TaskState state) {
        this.state = state;
    }

    /**
     * Get the state for the task
     *
     * @return the current task state
     */
    public TaskState getState() {
        return this.state;
    }

    /**
     * Gets the due date of the task
     *
     * @return The due date of the task
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Sets the due date for a task
     *
     * @param dueDate the due date to set for the task
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Gets the renovation that this task is for.
     *
     * @return The renovation
     */
    public Renovation getRenovation() {
        return renovation;
    }

    /**
     * Sets the renovation that this task is for.
     *
     * @param renovation The renovation the task is for
     */
    public void setRenovation(Renovation renovation) {
        this.renovation = renovation;
        if (renovation != null && !renovation.getTasks().contains(this)) {
            renovation.addTask(this);
        }
    }

    /**
     * Gets the icon file name for this task
     *
     * @return The icon file name of the task
     */
    public String getIconFileName() {
        return iconFileName;
    }

    /**
     * Sets the icon file name for this task
     *
     * @param iconFileName the image file name to set to
     */
    public void setIconFileName(String iconFileName) {
        this.iconFileName = iconFileName;
    }

    /**
     * Gets the list of rooms for this task
     *
     * @return the list of rooms
     */
    public List<Room> getRooms() {
        return rooms;
    }

    /**
     * Sets the rooms for a task
     *
     * @param rooms a list of rooms to set for the task
     */
    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    /**
     * Adds a new room to this task
     *
     * @param room the room to add
     */
    public void addRoom(Room room) {
        if (rooms == null) {
            rooms = new ArrayList<>();
        }
        if (!rooms.contains(room)) {
            rooms.add(room);
        }
    }

    /**
     * Adds a new expense to this task
     *
     * @param expense the expense to add
     */
    public void addExpense(Expense expense) {
        if (expenses == null) {
            expenses = new ArrayList<>();
        }
        if (!expenses.contains(expense)) {
            expenses.add(expense);
        }
    }

    /**
     * Removes an expense from this task
     *
     * @param expense the expense to remove
     */
    public void removeExpense(Expense expense) {
        if (expenses.contains(expense)) {
            expenses.remove(expense);
            expense.setTask(null);
        }
    }

    /**
     * Gets all of this task's expenses
     *
     * @return a list of expenses for this task
     */
    public List<Expense> getExpenses() {
        return expenses;
    }

    /**
     * Calculates the total cost of the task
     *
     * @return the sum of the task's expense costs
     */
    @Transient
    public BigDecimal getTotalCost() {
        BigDecimal total = new BigDecimal(("0"));
        for (Expense expense : expenses) {
            total = total.add(expense.getExpenseCost());
        }
        return total;
    }

    /**
     * Count the total number of individual expenses.
     *
     * @return Integer number of individual expense items for this task.
     */
    @Transient
    public int getExpenseCount() {
        return expenses.size();
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", state=" + state +
                ", iconFileName='" + iconFileName + '\'' +
                ", renovation=" + renovation +
                ", rooms=" + rooms +
                '}';
    }
}
