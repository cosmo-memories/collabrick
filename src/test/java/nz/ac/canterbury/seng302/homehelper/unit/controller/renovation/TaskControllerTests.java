package nz.ac.canterbury.seng302.homehelper.unit.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.controller.renovation.TaskController;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationMemberService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTests {
    @Mock
    private RenovationService renovationService;

    @Mock
    private RenovationMemberService renovationMemberService;

    @Mock
    private Renovation renovation;

    @Mock
    private Task task;

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        when(renovationService.getRenovation(1L)).thenReturn(Optional.of(renovation));
        when(renovationService.getTask(1L)).thenReturn(task);
        when(task.getState()).thenReturn(TaskState.COMPLETED);
    }

    @Test
    void userCameFromTaskTab_userClicksSubmitOnEditTaskPage_returnsToTaskTab() throws NoResourceFoundException {
        User user = mock(User.class);
        when(renovationMemberService.checkMembership(any(User.class), any(Renovation.class))).thenReturn(true);

        try (MockedStatic<UserUtil> userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(() -> UserUtil.getUserFromHttpServletRequest(any(), any()))
                    .thenReturn(user);

            String redirectUrl = taskController.submitEditTaskForm(1L, 1L, "TestTask", "This is a task",
                    null, null, 1, "", "tasks", "NOT_STARTED", model, request);


            assertEquals("redirect:/renovation/1/tasks/1", redirectUrl);
        }

    }

    @Test
    void userCameFromCalendarTab_userClicksSubmitOnEditTaskPage_returnsToCalendarTab() throws NoResourceFoundException {
        User user = mock(User.class);
        when(renovationMemberService.checkMembership(any(User.class), any(Renovation.class))).thenReturn(true);

        try (MockedStatic<UserUtil> userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(() -> UserUtil.getUserFromHttpServletRequest(any(), any()))
                    .thenReturn(user);

            String redirectUrl = taskController.submitEditTaskForm(1L, 1L, "TestTask", "This is a task",
                    "2025-06-27", null, 1, "", "calendar", "NOT_STARTED", model, request);
            assertEquals("redirect:/renovation/1/calendar?dateStr=2025-06-27", redirectUrl);
        }
    }

}
