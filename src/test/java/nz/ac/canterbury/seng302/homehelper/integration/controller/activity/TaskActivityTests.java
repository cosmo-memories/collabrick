package nz.ac.canterbury.seng302.homehelper.integration.controller.activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.activity.ActivityType;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseDto;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole;
import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;
import nz.ac.canterbury.seng302.homehelper.repository.activity.ActivityRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.activity.ActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskActivityTests {

    @MockBean
    private ActivityService activityService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private User ownerUser;
    private User memberUser;
    private Renovation renovation;

    private Task notStartedTask;
    private Task inProgressTask;
    private Task completedTask;
    private Task blockedTask;
    private Task cancelledTask;

    @BeforeEach
    void setup() {
        activityRepository.deleteAll();
        userRepository.deleteAll();
        renovationRepository.deleteAll();

        ownerUser = new User("John", "Terraria", "terrarian@gmail.com", "Abc123!!", "Abc123!!");
        ownerUser.setActivated(true);
        userRepository.save(ownerUser);

        memberUser = new User("Jane", "Infernum", "infernum@gmail.com", "Abc123!!", "Abc123!!");
        memberUser.setActivated(true);
        userRepository.save(memberUser);

        renovation = new Renovation("GetFixedBoi", "1.4.5 when?");
        notStartedTask = new Task(renovation, "the final update", "plz release", TaskState.NOT_STARTED, "");
        inProgressTask = new Task(renovation, "1.4.5", "this year", TaskState.IN_PROGRESS, "");
        completedTask = new Task(renovation, "1.4.4", "it's been years", TaskState.COMPLETED, "");
        blockedTask = new Task(renovation, "dunno", "no shot they stop", TaskState.BLOCKED, "");
        cancelledTask = new Task(renovation, "bring back throwing", "surely it comes back", TaskState.CANCELLED, "");

        renovation.setOwner(ownerUser);
        renovation.addMember(memberUser, RenovationMemberRole.MEMBER);
        renovation.addTask(notStartedTask);
        renovation.addTask(inProgressTask);
        renovation.addTask(completedTask);
        renovation.addTask(blockedTask);
        renovation.addTask(cancelledTask);
        renovationRepository.save(renovation);

    }

    @Test
    void renovation_ownerCreatesTask_savesTaskAddedActivity() throws Exception {
        mockMvc.perform(post("/myRenovations/{id}/newTask", renovation.getId())
                        .with(csrf())
                        .with(user(String.valueOf(ownerUser.getId())).password(ownerUser.getPassword()).roles("USER"))
                        .param("taskName", "Task")
                        .param("taskDescription", "plz release")
                        .param("state", "NOT_STARTED")
                        .param("dateInvalid", ""))
                .andExpect(status().is3xxRedirection());

        verify(activityService, times(1)).saveLiveUpdate(any());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(1)).saveLiveUpdate(captor.capture());
        LiveUpdate capturedUpdate = captor.getValue();
        assertEquals(ActivityType.TASK_ADDED, capturedUpdate.getActivityType());
        assertEquals(ownerUser, capturedUpdate.getUser());
        assertEquals(renovation, capturedUpdate.getRenovation());
    }

    @Test
    void renovation_ownerEditsTask_savesTaskEditedActivity() throws Exception {
        mockMvc.perform(post("/myRenovations/{renoId}/editTask/{taskId}", renovation.getId(), notStartedTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(ownerUser.getId())).password(ownerUser.getPassword()).roles("USER"))
                        .param("taskName", "Task")
                        .param("taskDescription", "plz release")
                        .param("state", "NOT_STARTED")
                        .param("dateInvalid", "")
                        .param("pageNumber", "1"))
                .andExpect(status().is3xxRedirection());

        verify(activityService, times(1)).saveLiveUpdate(any());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(1)).saveLiveUpdate(captor.capture());
        LiveUpdate capturedUpdate = captor.getValue();
        assertEquals(ActivityType.TASK_EDITED, capturedUpdate.getActivityType());
        assertEquals(ownerUser, capturedUpdate.getUser());
        assertEquals(renovation, capturedUpdate.getRenovation());
    }

    @Test
    void renovation_memberCreatesTask_savesTaskAddedActivity() throws Exception {
        mockMvc.perform(post("/myRenovations/{id}/newTask", renovation.getId())
                        .with(csrf())
                        .with(user(String.valueOf(memberUser.getId())).password(memberUser.getPassword()).roles("USER"))
                        .param("taskName", "New Task")
                        .param("taskDescription", "plz release")
                        .param("state", "NOT_STARTED")
                        .param("dateInvalid", ""))
                .andExpect(status().is3xxRedirection());

        verify(activityService, times(1)).saveLiveUpdate(any());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(1)).saveLiveUpdate(captor.capture());
        LiveUpdate capturedUpdate = captor.getValue();
        assertEquals(ActivityType.TASK_ADDED, capturedUpdate.getActivityType());
        assertEquals(memberUser, capturedUpdate.getUser());
        assertEquals(renovation, capturedUpdate.getRenovation());
    }

    @Test
    void renovation_memberEditsTask_savesTaskEditedActivity() throws Exception {
        mockMvc.perform(post("/myRenovations/{renoId}/editTask/{taskId}", renovation.getId(), notStartedTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(memberUser.getId())).password(memberUser.getPassword()).roles("USER"))
                        .param("taskName", "Task")
                        .param("taskDescription", "plz release")
                        .param("state", "NOT_STARTED")
                        .param("dateInvalid", "")
                        .param("pageNumber", "1"))
                .andExpect(status().is3xxRedirection());

        verify(activityService, times(1)).saveLiveUpdate(any());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(1)).saveLiveUpdate(captor.capture());
        LiveUpdate capturedUpdate = captor.getValue();
        assertEquals(ActivityType.TASK_EDITED, capturedUpdate.getActivityType());
        assertEquals(memberUser, capturedUpdate.getUser());
        assertEquals(renovation, capturedUpdate.getRenovation());
    }

    @Test
    void renovation_ownerAddsExpenseToTask_savesExpenseAddedActivity() throws Exception {
        List<ExpenseDto> expense = List.of(new ExpenseDto("Easter Eggs", "Material", "2024-12-12", 10000.50));

        mockMvc.perform(post("/myRenovations/{id}/addTaskExpenses/{taskId}", renovation.getId(), notStartedTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(ownerUser.getId())).password(ownerUser.getPassword()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isOk());

        verify(activityService, times(1)).saveLiveUpdate(any());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(1)).saveLiveUpdate(captor.capture());
        LiveUpdate capturedUpdate = captor.getValue();
        assertEquals(ActivityType.EXPENSE_ADDED, capturedUpdate.getActivityType());
        assertEquals(ownerUser, capturedUpdate.getUser());
        assertEquals(renovation, capturedUpdate.getRenovation());
    }

    @Test
    void renovation_memberAddsExpense_savesExpenseAddedActivity() throws Exception {
        List<ExpenseDto> expense = List.of(new ExpenseDto("Easter Eggs", "Material", "2024-12-12", 10000.50));

        mockMvc.perform(post("/myRenovations/{id}/addTaskExpenses/{taskId}", renovation.getId(), notStartedTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(memberUser.getId())).password(memberUser.getPassword()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isOk());

        verify(activityService, times(1)).saveLiveUpdate(any());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(1)).saveLiveUpdate(captor.capture());
        LiveUpdate capturedUpdate = captor.getValue();
        assertEquals(ActivityType.EXPENSE_ADDED, capturedUpdate.getActivityType());
        assertEquals(memberUser, capturedUpdate.getUser());
        assertEquals(renovation, capturedUpdate.getRenovation());
    }

    @Test
    void editTask_ownerChangesTaskFromNotStartedState_savesTaskChangeFromNotStartedActivity() throws Exception {
        mockMvc.perform(post("/myRenovations/{renoId}/editTask/{taskId}", renovation.getId(), notStartedTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(ownerUser.getId())).password(ownerUser.getPassword()).roles("USER"))
                        .param("taskName", "Task")
                        .param("taskDescription", "plz release")
                        .param("state", "IN_PROGRESS")
                        .param("dateInvalid", "")
                        .param("pageNumber", "1"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(2)).saveLiveUpdate(captor.capture());

        List<LiveUpdate> updates = captor.getAllValues();

        // GPT'd this bit here to get the activity types into a set
        Set<ActivityType> types = updates.stream()
                .map(LiveUpdate::getActivityType)
                .collect(Collectors.toSet());

        assertTrue(types.contains(ActivityType.TASK_EDITED));
        assertTrue(types.contains(ActivityType.TASK_CHANGED_FROM_NOT_STARTED));

        for (LiveUpdate u : updates) {
            assertEquals(ownerUser, u.getUser());
            assertEquals(renovation, u.getRenovation());
        }
    }

    @Test
    void editTask_ownerChangesTaskFromInProgressState_savesTaskChangeFromInProgressActivity() throws Exception {
        mockMvc.perform(post("/myRenovations/{renoId}/editTask/{taskId}", renovation.getId(), inProgressTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(ownerUser.getId())).password(ownerUser.getPassword()).roles("USER"))
                        .param("taskName", "Task")
                        .param("taskDescription", "plz release")
                        .param("state", "COMPLETED")
                        .param("dateInvalid", "")
                        .param("pageNumber", "1"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(2)).saveLiveUpdate(captor.capture());

        List<LiveUpdate> updates = captor.getAllValues();

        Set<ActivityType> types = updates.stream()
                .map(LiveUpdate::getActivityType)
                .collect(Collectors.toSet());

        assertTrue(types.contains(ActivityType.TASK_EDITED));
        assertTrue(types.contains(ActivityType.TASK_CHANGED_FROM_IN_PROGRESS));

        for (LiveUpdate u : updates) {
            assertEquals(ownerUser, u.getUser());
            assertEquals(renovation, u.getRenovation());
        }
    }

    @Test
    void editTask_ownerChangesTaskFromCompletedState_savesTaskChangeFromCompletedActivity() throws Exception {
        mockMvc.perform(post("/myRenovations/{renoId}/editTask/{taskId}", renovation.getId(), completedTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(ownerUser.getId())).password(ownerUser.getPassword()).roles("USER"))
                        .param("taskName", "Task")
                        .param("taskDescription", "plz release")
                        .param("state", "IN_PROGRESS")
                        .param("dateInvalid", "")
                        .param("pageNumber", "1"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(2)).saveLiveUpdate(captor.capture());

        List<LiveUpdate> updates = captor.getAllValues();

        Set<ActivityType> types = updates.stream()
                .map(LiveUpdate::getActivityType)
                .collect(Collectors.toSet());

        assertTrue(types.contains(ActivityType.TASK_EDITED));
        assertTrue(types.contains(ActivityType.TASK_CHANGED_FROM_COMPLETED));

        for (LiveUpdate u : updates) {
            assertEquals(ownerUser, u.getUser());
            assertEquals(renovation, u.getRenovation());
        }
    }

    @Test
    void editTask_ownerChangesTaskFromBlockedState_savesTaskChangeFromCompletedActivity() throws Exception {
        mockMvc.perform(post("/myRenovations/{renoId}/editTask/{taskId}", renovation.getId(), blockedTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(ownerUser.getId())).password(ownerUser.getPassword()).roles("USER"))
                        .param("taskName", "Task")
                        .param("taskDescription", "plz release")
                        .param("state", "IN_PROGRESS")
                        .param("dateInvalid", "")
                        .param("pageNumber", "1"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(2)).saveLiveUpdate(captor.capture());

        List<LiveUpdate> updates = captor.getAllValues();

        Set<ActivityType> types = updates.stream()
                .map(LiveUpdate::getActivityType)
                .collect(Collectors.toSet());

        assertTrue(types.contains(ActivityType.TASK_EDITED));
        assertTrue(types.contains(ActivityType.TASK_CHANGED_FROM_BLOCKED));

        for (LiveUpdate u : updates) {
            assertEquals(ownerUser, u.getUser());
            assertEquals(renovation, u.getRenovation());
        }
    }

    @Test
    void editTask_ownerChangesTaskFromCancelledState_savesTaskChangeFromCancelledActivity() throws Exception {
        mockMvc.perform(post("/myRenovations/{renoId}/editTask/{taskId}", renovation.getId(), cancelledTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(ownerUser.getId())).password(ownerUser.getPassword()).roles("USER"))
                        .param("taskName", "Task")
                        .param("taskDescription", "plz release")
                        .param("state", "IN_PROGRESS")
                        .param("dateInvalid", "")
                        .param("pageNumber", "1"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(2)).saveLiveUpdate(captor.capture());

        List<LiveUpdate> updates = captor.getAllValues();

        Set<ActivityType> types = updates.stream()
                .map(LiveUpdate::getActivityType)
                .collect(Collectors.toSet());

        assertTrue(types.contains(ActivityType.TASK_EDITED));
        assertTrue(types.contains(ActivityType.TASK_CHANGED_FROM_CANCELLED));

        for (LiveUpdate u : updates) {
            assertEquals(ownerUser, u.getUser());
            assertEquals(renovation, u.getRenovation());
        }
    }

    @Test
    void editTask_memberChangesTaskFromNotStartedState_savesTaskChangeFromNotStartedActivity() throws Exception {
        mockMvc.perform(post("/myRenovations/{renoId}/editTask/{taskId}", renovation.getId(), notStartedTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(memberUser.getId())).password(memberUser.getPassword()).roles("USER"))
                        .param("taskName", "Task")
                        .param("taskDescription", "plz release")
                        .param("state", "IN_PROGRESS")
                        .param("dateInvalid", "")
                        .param("pageNumber", "1"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(2)).saveLiveUpdate(captor.capture());

        List<LiveUpdate> updates = captor.getAllValues();

        // GPT'd this bit here to get the activity types into a set
        Set<ActivityType> types = updates.stream()
                .map(LiveUpdate::getActivityType)
                .collect(Collectors.toSet());

        assertTrue(types.contains(ActivityType.TASK_EDITED));
        assertTrue(types.contains(ActivityType.TASK_CHANGED_FROM_NOT_STARTED));

        for (LiveUpdate u : updates) {
            assertEquals(memberUser, u.getUser());
            assertEquals(renovation, u.getRenovation());
        }
    }

    @Test
    void editTask_memberChangesTaskFromInProgressState_savesTaskChangeFromInProgressActivity() throws Exception {
        mockMvc.perform(post("/myRenovations/{renoId}/editTask/{taskId}", renovation.getId(), inProgressTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(memberUser.getId())).password(memberUser.getPassword()).roles("USER"))
                        .param("taskName", "Task")
                        .param("taskDescription", "plz release")
                        .param("state", "COMPLETED")
                        .param("dateInvalid", "")
                        .param("pageNumber", "1"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(2)).saveLiveUpdate(captor.capture());

        List<LiveUpdate> updates = captor.getAllValues();

        Set<ActivityType> types = updates.stream()
                .map(LiveUpdate::getActivityType)
                .collect(Collectors.toSet());

        assertTrue(types.contains(ActivityType.TASK_EDITED));
        assertTrue(types.contains(ActivityType.TASK_CHANGED_FROM_IN_PROGRESS));

        for (LiveUpdate u : updates) {
            assertEquals(memberUser, u.getUser());
            assertEquals(renovation, u.getRenovation());
        }
    }

    @Test
    void editTask_memberChangesTaskFromCompletedState_savesTaskChangeFromCompletedActivity() throws Exception {
        mockMvc.perform(post("/myRenovations/{renoId}/editTask/{taskId}", renovation.getId(), completedTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(memberUser.getId())).password(memberUser.getPassword()).roles("USER"))
                        .param("taskName", "Task")
                        .param("taskDescription", "plz release")
                        .param("state", "IN_PROGRESS")
                        .param("dateInvalid", "")
                        .param("pageNumber", "1"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(2)).saveLiveUpdate(captor.capture());

        List<LiveUpdate> updates = captor.getAllValues();

        Set<ActivityType> types = updates.stream()
                .map(LiveUpdate::getActivityType)
                .collect(Collectors.toSet());

        assertTrue(types.contains(ActivityType.TASK_EDITED));
        assertTrue(types.contains(ActivityType.TASK_CHANGED_FROM_COMPLETED));

        for (LiveUpdate u : updates) {
            assertEquals(memberUser, u.getUser());
            assertEquals(renovation, u.getRenovation());
        }
    }

    @Test
    void editTask_memberChangesTaskFromBlockedState_savesTaskChangeFromCompletedActivity() throws Exception {
        mockMvc.perform(post("/myRenovations/{renoId}/editTask/{taskId}", renovation.getId(), blockedTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(memberUser.getId())).password(memberUser.getPassword()).roles("USER"))
                        .param("taskName", "Task")
                        .param("taskDescription", "plz release")
                        .param("state", "IN_PROGRESS")
                        .param("dateInvalid", "")
                        .param("pageNumber", "1"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(2)).saveLiveUpdate(captor.capture());

        List<LiveUpdate> updates = captor.getAllValues();

        Set<ActivityType> types = updates.stream()
                .map(LiveUpdate::getActivityType)
                .collect(Collectors.toSet());

        assertTrue(types.contains(ActivityType.TASK_EDITED));
        assertTrue(types.contains(ActivityType.TASK_CHANGED_FROM_BLOCKED));

        for (LiveUpdate u : updates) {
            assertEquals(memberUser, u.getUser());
            assertEquals(renovation, u.getRenovation());
        }
    }

    @Test
    void editTask_memberChangesTaskFromCancelledState_savesTaskChangeFromCancelledActivity() throws Exception {
        mockMvc.perform(post("/myRenovations/{renoId}/editTask/{taskId}", renovation.getId(), cancelledTask.getId())
                        .with(csrf())
                        .with(user(String.valueOf(memberUser.getId())).password(memberUser.getPassword()).roles("USER"))
                        .param("taskName", "Task")
                        .param("taskDescription", "plz release")
                        .param("state", "IN_PROGRESS")
                        .param("dateInvalid", "")
                        .param("pageNumber", "1"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(2)).saveLiveUpdate(captor.capture());

        List<LiveUpdate> updates = captor.getAllValues();

        Set<ActivityType> types = updates.stream()
                .map(LiveUpdate::getActivityType)
                .collect(Collectors.toSet());

        assertTrue(types.contains(ActivityType.TASK_EDITED));
        assertTrue(types.contains(ActivityType.TASK_CHANGED_FROM_CANCELLED));

        for (LiveUpdate u : updates) {
            assertEquals(memberUser, u.getUser());
            assertEquals(renovation, u.getRenovation());
        }
    }
}

