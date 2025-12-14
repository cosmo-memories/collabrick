package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ExtendWith(MockitoExtension.class)
public class TaskServiceIntegrationTests {

    @Autowired
    private TaskService taskService;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationService renovationService;

    User user;
    Renovation renovation;

    @BeforeEach
    void setUp() {
        user = new User("Susan", "Susanson", "susan@gmail.com", "password", "password");
        userRepository.save(user);

        renovation = new Renovation("Reno", "");
        renovation.setOwner(user);
        renovationRepository.save(renovation);
    }

    @Test
    void countUncompletedTasksByRenovationId_noTasks_returnsZero() {
        assertEquals(0, taskService.countUncompletedTasksByRenovationId(renovation.getId()));
    }

    @Test
    void countUncompletedTasksByRenovationId_onlyCancelledAndCompletedTasks_returnsZero() {
        Task task1 = new Task(renovation, "Cancelled Task", "Description", "icon.png");
        task1.setState(TaskState.CANCELLED);
        renovation.addTask(task1);

        Task task2 = new Task(renovation, "Completed Task", "Description", "icon.png");
        task2.setState(TaskState.COMPLETED);
        renovation.addTask(task2);

        renovationRepository.save(renovation);

        assertEquals(0, taskService.countUncompletedTasksByRenovationId(renovation.getId()));
    }

    @Test
    void countUncompletedTasksByRenovationId_NotStartedTask_returnsOne() {
        Task task1 = new Task(renovation, "Not Started Task", "Description", "icon.png");
        task1.setState(TaskState.NOT_STARTED);
        renovation.addTask(task1);
        renovationRepository.save(renovation);

        assertEquals(1, taskService.countUncompletedTasksByRenovationId(renovation.getId()));
    }

    @Test
    void countUncompletedTasksByRenovationId_InProgressTask_returnsOne() {
        Task task1 = new Task(renovation, "In Progress Task", "Description", "icon.png");
        task1.setState(TaskState.IN_PROGRESS);
        renovation.addTask(task1);
        renovationRepository.save(renovation);

        assertEquals(1, taskService.countUncompletedTasksByRenovationId(renovation.getId()));
    }

    @Test
    void countUncompletedTasksByRenovationId_BlockedTask_returnsOne() {
        Task task1 = new Task(renovation, "Blocked Task", "Description", "icon.png");
        task1.setState(TaskState.BLOCKED);
        renovation.addTask(task1);
        renovationRepository.save(renovation);

        assertEquals(1, taskService.countUncompletedTasksByRenovationId(renovation.getId()));
    }

    @Test
    void countUncompletedTasksByRenovationId_CombinationOfTasks_returnsThree() {
        Task task1 = new Task(renovation, "Cancelled Task", "Description", "icon.png");
        task1.setState(TaskState.CANCELLED);
        renovation.addTask(task1);

        Task task2 = new Task(renovation, "Completed Task", "Description", "icon.png");
        task2.setState(TaskState.COMPLETED);
        renovation.addTask(task2);

        Task task3 = new Task(renovation, "Not Started Task", "Description", "icon.png");
        task3.setState(TaskState.NOT_STARTED);
        renovation.addTask(task3);

        Task task4 = new Task(renovation, "In Progress Task", "Description", "icon.png");
        task4.setState(TaskState.IN_PROGRESS);
        renovation.addTask(task4);

        Task task5 = new Task(renovation, "Blocked Task", "Description", "icon.png");
        task5.setState(TaskState.BLOCKED);
        renovation.addTask(task5);

        renovationRepository.save(renovation);

        assertEquals(3, taskService.countUncompletedTasksByRenovationId(renovation.getId()));
    }

    @Test
    void upcomingTasks_NoTasks_ReturnsEmptyList() {
        assertTrue(taskService.findUpcomingTasks(user.getId()).isEmpty());
    }

    @Test
    void upcomingTasks_OneTask_ReturnsTask() {
        Task task = new Task(renovation, "Some Task", "Some Description", "icon.png");
        task.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task, String.valueOf(LocalDate.now().plusDays(1)));

        List<Task> result = taskService.findUpcomingTasks(user.getId());
        assertEquals(1, result.size());
        assertEquals(task.getId(), result.getFirst().getId());
    }

    @Test
    void upcomingTasks_TwoTasks_ReturnsSingleValidTask() {
        Task task = new Task(renovation, "Some Task", "Some Description", "icon.png");
        task.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task, String.valueOf(LocalDate.now().plusDays(1)));

        Task task2 = new Task(renovation, "Some Task 2", "Some Description 2", "icon.png");
        task2.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task2, String.valueOf(LocalDate.now().plusDays(10)));

        List<Task> result = taskService.findUpcomingTasks(user.getId());
        assertEquals(1, result.size());
        assertEquals(task.getId(), result.getFirst().getId());
    }

    @Test
    void upcomingTasks_TwoTasks_ReturnsBothInOrder() {
        Task task = new Task(renovation, "Some Task", "Some Description", "icon.png");
        task.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task, String.valueOf(LocalDate.now().plusDays(1)));

        Task task2 = new Task(renovation, "Some Task 2", "Some Description 2", "icon.png");
        task2.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task2, String.valueOf(LocalDate.now().plusDays(2)));

        List<Task> result = taskService.findUpcomingTasks(user.getId());
        assertEquals(2, result.size());
        assertEquals(task.getId(), result.getFirst().getId());
        assertEquals(task2.getId(), result.getLast().getId());
    }

    @Test
    void upcomingTasks_ValidDateEdgeCases_ReturnsBothTasks() {
        Task task = new Task(renovation, "Some Task", "Some Description", "icon.png");
        task.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task, String.valueOf(LocalDate.now()));
        Task task2 = new Task(renovation, "Some Task 2", "Some Description 2", "icon.png");
        task2.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task2, String.valueOf(LocalDate.now().plusDays(7)));

        List<Task> result = taskService.findUpcomingTasks(user.getId());
        assertEquals(2, result.size());
        assertEquals(task.getId(), result.getFirst().getId());
        assertEquals(task2.getId(), result.getLast().getId());
    }

    @Test
    void upcomingTasks_InValidDateEdgeCases_ReturnsNothing() {
        Task task = new Task(renovation, "Some Task", "Some Description", "icon.png");
        task.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task, String.valueOf(LocalDate.now().plusDays(8)));

        Task task2 = new Task(renovation, "Some Task 2", "Some Description 2", "icon.png");
        task2.setState(TaskState.IN_PROGRESS);
        task2.setDueDate(LocalDate.now().minusDays(1));
        renovationRepository.save(renovation);

        assertTrue(taskService.findUpcomingTasks(user.getId()).isEmpty());
    }

    @Test
    void upcomingTasks_ThreeTasks_ReturnsAllInCorrectOrder() {
        Task task = new Task(renovation, "Some Task", "Some Description", "icon.png");
        task.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task, String.valueOf(LocalDate.now().plusDays(1)));

        Task task2 = new Task(renovation, "Some Task 2", "Some Description 2", "icon.png");
        task2.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task2, String.valueOf(LocalDate.now().plusDays(3)));

        Task task3 = new Task(renovation, "Some Task 3", "Some Description 3", "icon.png");
        task3.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task3, String.valueOf(LocalDate.now().plusDays(2)));

        List<Task> result = taskService.findUpcomingTasks(user.getId());
        assertEquals(3, result.size());
        assertEquals(task.getId(), result.getFirst().getId());
        assertEquals(task3.getId(), result.get(1).getId());
        assertEquals(task2.getId(), result.getLast().getId());
    }

    @Test
    void upcomingTasks_InvalidDates_ReturnsValidInCorrectOrder() {
        Task task = new Task(renovation, "Some Task", "Some Description", "icon.png");
        task.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task, String.valueOf(LocalDate.now().plusDays(1)));

        Task task2 = new Task(renovation, "Some Task 2", "Some Description 2", "icon.png");
        task2.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task2, String.valueOf(LocalDate.now().plusDays(3)));

        Task task3 = new Task(renovation, "Some Task 3", "Some Description 3", "icon.png");
        task3.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task3, String.valueOf(LocalDate.now().plusDays(2)));

        Task task4 = new Task(renovation, "Some Task 4", "Some Description 4", "icon.png");
        task4.setState(TaskState.IN_PROGRESS);
        task4.setDueDate(LocalDate.now().minusDays(2));
        renovationRepository.save(renovation);

        Task task5 = new Task(renovation, "Some Task 5", "Some Description 5", "icon.png");
        task5.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task5, String.valueOf(LocalDate.now().plusDays(10)));

        Task task6 = new Task(renovation, "Some Task 6", "Some Description 6", "icon.png");
        task6.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task6, String.valueOf(LocalDate.now().plusDays(6)));

        List<Task> result = taskService.findUpcomingTasks(user.getId());
        assertEquals(4, result.size());
        assertEquals(task.getId(), result.getFirst().getId());
        assertEquals(task3.getId(), result.get(1).getId());
        assertEquals(task2.getId(), result.get(2).getId());
        assertEquals(task6.getId(), result.getLast().getId());
    }

    @Test
    void upcomingTasks_InvalidState_ReturnsAllValidInCorrectOrder() {
        Task task = new Task(renovation, "Some Task", "Some Description", "icon.png");
        task.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task, String.valueOf(LocalDate.now().plusDays(1)));

        Task task2 = new Task(renovation, "Some Task 2", "Some Description 2", "icon.png");
        task2.setState(TaskState.CANCELLED);
        renovationService.saveTask(renovation.getId(), task2, String.valueOf(LocalDate.now().plusDays(2)));

        Task task3 = new Task(renovation, "Some Task 3", "Some Description 3", "icon.png");
        task3.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task3, String.valueOf(LocalDate.now().plusDays(3)));

        List<Task> result = taskService.findUpcomingTasks(user.getId());
        assertEquals(2, result.size());
        assertEquals(task.getId(), result.getFirst().getId());
        assertEquals(task3.getId(), result.getLast().getId());
    }

    @Test
    void upcomingTasks_MultipleInvalidStates_ReturnsAllValidInCorrectOrder() {
        Task task = new Task(renovation, "Some Task", "Some Description", "icon.png");
        task.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task, String.valueOf(LocalDate.now().plusDays(1)));

        Task task2 = new Task(renovation, "Some Task 2", "Some Description 2", "icon.png");
        task2.setState(TaskState.CANCELLED);
        renovationService.saveTask(renovation.getId(), task2, String.valueOf(LocalDate.now().plusDays(2)));

        Task task3 = new Task(renovation, "Some Task 3", "Some Description 3", "icon.png");
        task3.setState(TaskState.COMPLETED);
        renovationService.saveTask(renovation.getId(), task3, String.valueOf(LocalDate.now().plusDays(3)));

        List<Task> result = taskService.findUpcomingTasks(user.getId());
        assertEquals(1, result.size());
        assertEquals(task.getId(), result.getFirst().getId());
    }

    @Test
    void upcomingTasks_InvalidTasksOnOtherRenovations_ReturnsAllValidInCorrectOrder() {
        User user2 = new User("Sam", "Susanson", "sam@gmail.com", "password", "password");
        userRepository.save(user2);

        Renovation renovation2 = new Renovation("Reno 2", "");
        renovation2.setOwner(user2);
        renovationRepository.save(renovation2);

        Task newTask = new Task(renovation2, "Some Task", "Some Description", "icon.png");
        newTask.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation2.getId(), newTask, String.valueOf(LocalDate.now().plusDays(1)));

        Task task = new Task(renovation, "Some Task", "Some Description", "icon.png");
        task.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task, String.valueOf(LocalDate.now().plusDays(1)));

        Task task2 = new Task(renovation, "Some Task 2", "Some Description 2", "icon.png");
        task2.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task2, String.valueOf(LocalDate.now().plusDays(2)));

        Task task3 = new Task(renovation, "Some Task 3", "Some Description 3", "icon.png");
        task3.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task3, String.valueOf(LocalDate.now().plusDays(3)));

        List<Task> result = taskService.findUpcomingTasks(user.getId());
        assertEquals(3, result.size());
        assertEquals(task.getId(), result.getFirst().getId());
        assertEquals(task2.getId(), result.get(1).getId());
        assertEquals(task3.getId(), result.getLast().getId());
    }

    @Test
    void upcomingTasks_ValidTasksOnOtherRenovations_ReturnsAllValidInCorrectOrder() {
        Renovation renovation2 = new Renovation("Reno 2", "");
        renovation2.setOwner(user);
        renovationRepository.save(renovation2);

        Task newTask = new Task(renovation2, "Some Task", "Some Description", "icon.png");
        newTask.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation2.getId(), newTask, String.valueOf(LocalDate.now().plusDays(2)));

        Task task = new Task(renovation, "Some Task", "Some Description", "icon.png");
        task.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task, String.valueOf(LocalDate.now().plusDays(1)));

        Task task2 = new Task(renovation, "Some Task 2", "Some Description 2", "icon.png");
        task2.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task2, String.valueOf(LocalDate.now().plusDays(3)));

        Task task3 = new Task(renovation, "Some Task 3", "Some Description 3", "icon.png");
        task3.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task3, String.valueOf(LocalDate.now().plusDays(4)));

        List<Task> result = taskService.findUpcomingTasks(user.getId());
        assertEquals(4, result.size());
        assertEquals(task.getId(), result.getFirst().getId());
        assertEquals(newTask.getId(), result.get(1).getId());
        assertEquals(task2.getId(), result.get(2).getId());
        assertEquals(task3.getId(), result.getLast().getId());
    }

    @Test
    void upcomingTasks_InvalidStateOnOtherRenovations_ReturnsAllValidInCorrectOrder() {
        Renovation renovation2 = new Renovation("Reno 2", "");
        renovation2.setOwner(user);
        renovationRepository.save(renovation2);

        Task newTask = new Task(renovation2, "Some Task", "Some Description", "icon.png");
        newTask.setState(TaskState.COMPLETED);
        renovationService.saveTask(renovation2.getId(), newTask, String.valueOf(LocalDate.now().plusDays(2)));

        Task task = new Task(renovation, "Some Task", "Some Description", "icon.png");
        task.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task, String.valueOf(LocalDate.now().plusDays(1)));

        Task task2 = new Task(renovation, "Some Task 2", "Some Description 2", "icon.png");
        task2.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task2, String.valueOf(LocalDate.now().plusDays(3)));

        Task task3 = new Task(renovation, "Some Task 3", "Some Description 3", "icon.png");
        task3.setState(TaskState.IN_PROGRESS);
        renovationService.saveTask(renovation.getId(), task3, String.valueOf(LocalDate.now().plusDays(4)));

        List<Task> result = taskService.findUpcomingTasks(user.getId());
        assertEquals(3, result.size());
        assertEquals(task.getId(), result.getFirst().getId());
        assertEquals(task2.getId(), result.get(1).getId());
        assertEquals(task3.getId(), result.getLast().getId());
    }

}
