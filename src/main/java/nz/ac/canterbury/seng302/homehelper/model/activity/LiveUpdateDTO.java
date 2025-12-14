package nz.ac.canterbury.seng302.homehelper.model.activity;

import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for sending LiveUpdate data to websockets.
 */
public class LiveUpdateDTO {

    // Required:
    private long renovationId;
    private String renovationName;
    private ActivityType activityType;
    private Instant timestamp;

    // Optional:
    private long userId;
    private String senderName;

    private long taskId;
    private String taskName;

    private long expenseId;
    private String expenseName;
    private BigDecimal expenseAmount;

    private TaskState oldState;
    private TaskState newState;

    private String email;

    public LiveUpdateDTO(long renovationId, String renovationName, ActivityType activityType, Instant timestamp) {
        this.renovationId = renovationId;
        this.renovationName = renovationName;
        this.activityType = activityType;
        this.timestamp = timestamp;
    }


    // Getters and Setters

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getRenovationId() { return renovationId; }
    public void setRenovationId(long renovationId) { this.renovationId = renovationId; }

    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }

    public TaskState getOldState() { return oldState; }
    public void setOldState(TaskState oldState) { this.oldState = oldState; }

    public TaskState getNewState() { return newState; }
    public void setNewState(TaskState newState) { this.newState = newState; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String name) { this.senderName = name; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public BigDecimal getExpenseAmount() { return expenseAmount; }
    public void setExpenseAmount(BigDecimal expenseAmount) { this.expenseAmount = expenseAmount; }

    public String getExpenseName() { return expenseName; }
    public void setExpenseName(String expenseName) { this.expenseName = expenseName; }

    public String getRenovationName() { return renovationName; }
    public void setRenovationName(String name) { this.renovationName = name; }
}


