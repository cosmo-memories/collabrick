package nz.ac.canterbury.seng302.homehelper.entity.activity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.activity.ActivityType;

import java.time.Instant;

/**
 * Entity for storing live activity feed updates so that they can be retrieved when user logs in.
 */
@Entity
public class LiveUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "renovation_id", nullable = false)
    private Renovation renovation;

    @Column(nullable = false)
    private Instant timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @ManyToOne
    @JoinColumn(name = "invitation_id")
    private Invitation invitation;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    /**
     * JPA Constructor
     */
    protected LiveUpdate() {}

    /**
     * LiveUpdate constructor for: updating budget
     * @param user              User who made the change
     * @param renovation        Renovation the change was made on
     * @param activityType      Type of activity update
     */
    public LiveUpdate(User user, Renovation renovation, ActivityType activityType) {
        this.user = user;
        this.renovation = renovation;
        this.timestamp = Instant.now();
        this.activityType = activityType;
    }

    /**
     * LiveUpdate constructor for: adding a task, editing a task, changing a task state
     * @param user              User who made the change
     * @param renovation        Renovation the change was made on
     * @param activityType      Type of activity update
     * @param task              Task added or edited
     */
    public LiveUpdate(User user, Renovation renovation, ActivityType activityType, Task task) {
        this.user = user;
        this.renovation = renovation;
        this.timestamp = Instant.now();
        this.activityType = activityType;
        this.task = task;
    }

    /**
     * LiveUpdate constructor for: adding an expense
     * @param user              User who made the change
     * @param renovation        Renovation the change was made on
     * @param activityType      Type of activity update
     * @param expense           Expense added
     */
    public LiveUpdate(User user, Renovation renovation, ActivityType activityType, Expense expense, Task task) {
        this.user = user;
        this.renovation = renovation;
        this.timestamp = Instant.now();
        this.activityType = activityType;
        this.expense = expense;
        this.task = task;
    }

    /**
     * LiveUpdate constructor for: accepting/rejecting invitations
     * @param user              User who made the change
     * @param renovation        Renovation the change was made on
     * @param activityType      Type of activity update
     * @param invitation        Invitation
     */
    public LiveUpdate(User user, Renovation renovation, ActivityType activityType, Invitation invitation) {
        this.user = user;
        this.renovation = renovation;
        this.timestamp = Instant.now();
        this.activityType = activityType;
        this.invitation = invitation;
    }


    // Getters and Setters

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Renovation getRenovation() { return renovation; }
    public void setRenovation(Renovation renovation) { this.renovation = renovation; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public Expense getExpense() { return expense; }
    public void setExpense(Expense expense) { this.expense = expense; }

    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }

    public Invitation getInvitation() { return invitation; }
    public void setInvitation(Invitation invitation) { this.invitation = invitation; }

}
