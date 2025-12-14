package nz.ac.canterbury.seng302.homehelper.model.ai;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Room;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.utility.DateUtils;

import java.util.List;

/**
 * A lightweight DTO representing a renovation task, formatted for AI context generation.
 *
 * @param name        The name of the task.
 * @param description A textual description of the task.
 * @param state       The current state of the task.
 * @param dueDate     The due date of the task, if present.
 * @param rooms       A list of room names linked to the task.
 * @param expenses    A list of expenses associated with the task.
 */
public record TaskAiView(
        String name,
        String description,
        String state,
        String dueDate,
        List<String> rooms,
        List<ExpenseAiView> expenses
) {
    public TaskAiView(Task task) {
        this(
                task.getName(),
                task.getDescription(),
                task.getState().name(),
                // due dates are optional
                task.getDueDate() == null ? null : DateUtils.formatDateForAi(task.getDueDate()),
                task.getRooms().stream().map(Room::getName).toList(),
                task.getExpenses().stream().map(ExpenseAiView::new).toList()
        );
    }
}
