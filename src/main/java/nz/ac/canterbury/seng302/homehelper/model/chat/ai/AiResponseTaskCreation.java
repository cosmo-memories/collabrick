package nz.ac.canterbury.seng302.homehelper.model.chat.ai;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;

import java.util.List;

/**
 * Represents a structured task creation response from the AI.
 * <pre>
 * {
 *     "type": "TASK_CREATION",
 *     "name": "Task Name",
 *     "description": "Task Descriptions",
 *     "date": "21/06/2025",
 *     "rooms": [
 *          "Kitchen"
 *     ]
 * }
 * </pre>
 */
public class AiResponseTaskCreation implements AiResponse {

    private final String name;
    private final String description;
    private final String date;
    private final TaskState state;
    private final List<String> rooms;

    @JsonCreator
    public AiResponseTaskCreation(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("date") String date,
            @JsonProperty("state") String state,
            @JsonProperty("rooms") List<String> rooms
    ) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.state = TaskState.valueOf(state);
        this.rooms = rooms;
    }

    @Override
    public AiResponseType getType() {
        return AiResponseType.TASK_CREATION;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public TaskState getState() { return state; }

    public List<String> getRooms() {
        return rooms;
    }

    @Override
    public String toString() {
        return "AiResponseTaskCreation{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", rooms=" + rooms +
                '}';
    }
}
