package nz.ac.canterbury.seng302.homehelper.model.ai;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Room;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Tag;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole;

import java.util.List;

/**
 * A lightweight DTO representing a renovation, formatted for AI context generation.
 *
 * @param name          The name of the renovation.
 * @param description   A textual description of the renovation.
 * @param ownerFullName The full name of the renovation owner.
 * @param tags          A list of descriptive tags associated with the renovation.
 * @param rooms         A list of room names involved in the renovation.
 * @param members       A list of members part of the renovation.
 * @param tasks         A list of tasks associated with the renovation.
 * @param budget        A breakdown of the renovation budget.
 */
public record RenovationAiView(
        String name,
        String description,
        String ownerFullName,
        List<String> tags,
        List<String> rooms,
        List<String> members,
        List<TaskAiView> tasks,
        BudgetAiView budget
) {
    public RenovationAiView(Renovation renovation, List<RenovationMember> members) {
        this(
                renovation.getName(),
                renovation.getDescription(),
                renovation.getOwner().getFullName(),
                renovation.getTags().stream().map(Tag::getTag).toList(),
                renovation.getRooms().stream().map(Room::getName).toList(),
                members.stream()
                        .filter(member -> member.getRole() != RenovationMemberRole.OWNER)
                        .map(member -> member.getUser().getFullName())
                        .toList(),
                renovation.getTasks().stream().map(TaskAiView::new).toList(),
                new BudgetAiView(renovation.getBudget())
        );
    }
}
