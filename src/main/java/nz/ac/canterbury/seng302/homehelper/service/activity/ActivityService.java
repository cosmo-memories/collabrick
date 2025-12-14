package nz.ac.canterbury.seng302.homehelper.service.activity;

import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.activity.ActivityType;
import nz.ac.canterbury.seng302.homehelper.model.activity.LiveUpdateDTO;
import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;
import nz.ac.canterbury.seng302.homehelper.repository.activity.ActivityRepository;
import org.apache.catalina.LifecycleState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service methods for handling live activity feed updates.
 */
@Service
public class ActivityService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ActivityRepository activityRepository;

    @Autowired
    public ActivityService(SimpMessagingTemplate messagingTemplate, ActivityRepository activityRepository) {
        this.messagingTemplate = messagingTemplate;
        this.activityRepository = activityRepository;
    }

    /**
     * Send a LiveUpdate to all applicable users.
     * @param liveUpdate        LiveUpdate object containing relevant update information
     */
    public void sendUpdate(LiveUpdate liveUpdate) {
        LiveUpdateDTO toSend = mapToDTO(liveUpdate);
        if (liveUpdate.getActivityType() == ActivityType.INVITE_ACCEPTED || liveUpdate.getActivityType() == ActivityType.INVITE_DECLINED) {
            // Send invite updates to renovation owner only
            messagingTemplate.convertAndSend("/topic/feed/" + liveUpdate.getRenovation().getOwner().getId(), toSend);
        } else {
            // Send other updates to all members
            for (RenovationMember member : liveUpdate.getRenovation().getMembers()) {
                messagingTemplate.convertAndSend("/topic/feed/" + member.getUser().getId(), toSend);
            }
        }
    }

    /**
     * Map LiveUpdate to LiveUpdateDTO.
     * @param liveUpdate        LiveUpdate object
     * @return                  DTO containing update information
     */
    public LiveUpdateDTO mapToDTO(LiveUpdate liveUpdate) {
        LiveUpdateDTO dto = new LiveUpdateDTO(liveUpdate.getRenovation().getId(), liveUpdate.getRenovation().getName(), liveUpdate.getActivityType(), liveUpdate.getTimestamp());

        if (liveUpdate.getUser() != null) {
            dto.setUserId(liveUpdate.getUser().getId());
            dto.setSenderName(liveUpdate.getUser().getFullName());
        }

        if (liveUpdate.getTask() != null) {
            dto.setTaskId(liveUpdate.getTask().getId());
            dto.setNewState(liveUpdate.getTask().getState());
            dto.setTaskName(liveUpdate.getTask().getName());
        }

        if (liveUpdate.getExpense() != null) {
            dto.setExpenseId(liveUpdate.getExpense().getId());
            dto.setExpenseAmount(liveUpdate.getExpense().getExpenseCost());
            dto.setExpenseName(liveUpdate.getExpense().getExpenseName());
        }

        if (liveUpdate.getInvitation() != null && liveUpdate.getInvitation().getUser() == null) {
            dto.setEmail(liveUpdate.getInvitation().getEmail());
        }

        switch (liveUpdate.getActivityType()) {
            case TASK_CHANGED_FROM_NOT_STARTED:
                dto.setOldState(TaskState.NOT_STARTED);
                break;
            case TASK_CHANGED_FROM_IN_PROGRESS:
                dto.setOldState(TaskState.IN_PROGRESS);
                break;
            case TASK_CHANGED_FROM_COMPLETED:
                dto.setOldState(TaskState.COMPLETED);
                break;
            case TASK_CHANGED_FROM_CANCELLED:
                dto.setOldState(TaskState.CANCELLED);
                break;
            case TASK_CHANGED_FROM_BLOCKED:
                dto.setOldState(TaskState.BLOCKED);
                break;
            default:
                break;
        }

        return dto;
    }

    /**
     * Save a new LiveUpdate.
     * @param liveUpdate        LiveUpdate object
     * @return                  Saved LiveUpdate
     */
    public LiveUpdate saveLiveUpdate(LiveUpdate liveUpdate) {
        return activityRepository.save(liveUpdate);
    }

    /**
     * gets the 10 latest updates for a user from all the renovations they are apart of
     * @param userId the user to find updates for
     * @return the 10 updates
     */
    public List<LiveUpdateDTO> getUserUpdates(Long userId) {
        return activityRepository.findLast10UpdatesForUser(userId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }
}
