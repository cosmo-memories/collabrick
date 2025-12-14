package nz.ac.canterbury.seng302.homehelper.integration.controller.renovation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Room;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseDto;
import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskIntegrationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RenovationRepository renovationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession authenticationSession;
    private Renovation renovation;
    private Task task;
    private long renovationId;
    private long taskId;

    @BeforeEach
    void setUpDatabase() throws Exception {

        renovationRepository.deleteAll();
        userRepository.deleteAll();

        String plaintextPassword = "Password123!";
        String encryptedPassword = (passwordEncoder.encode(plaintextPassword));
        User user = new User("Test", "user", "user@test.com", encryptedPassword, encryptedPassword);
        user.setActivated(true);
        userRepository.save(user);

        renovation = new Renovation("TestRenovationName", "TestRenovationDescription");
        task = new Task(renovation, "Task Name", "Task Description", "/test/filename");

        renovation.addTask(task);
        renovation.setOwner(user);
        renovationRepository.save(renovation);

        renovationId = renovation.getId();
        taskId = task.getId();

        MvcResult result = mockMvc.perform(post("/do_login")
                        .param("email", "user@test.com")
                        .param("password", "Password123!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        authenticationSession = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(authenticationSession);
        assertFalse(authenticationSession.isInvalid());
    }

    @Test
    void submitTaskForm_ValidDetails_CreatesNewTask() throws Exception {
        int numTasksBefore = renovation.getTasks().size();
        MvcResult mvcResult = mockMvc.perform(post("/myRenovations/{id}/newTask", renovationId).session(authenticationSession).with(csrf())
                        .param("taskName", "Test Task 2")
                        .param("taskDescription", "Test Description")
                        .param("taskDueDate", "")
                        .param("dateInvalid", "")
                        .param("state", "In_Progress"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertThat(renovation.getTasks().size()).isEqualTo(numTasksBefore + 1);

        Task findTask = renovation.getTasks().stream().filter(t -> "Test Task 2".equals(t.getName())).findFirst()
                .orElseThrow(() -> new IllegalStateException("Task 'Test Task 2' not found"));

        MockHttpServletResponse response = mvcResult.getResponse();

        String redirectedLocation = response.getRedirectedUrl();
        assertEquals("/renovation/" + renovationId + "/tasks/" + findTask.getId(), redirectedLocation);
        assertEquals("Test Task 2", findTask.getName());
        assertEquals("Test Description", findTask.getDescription());
        assertEquals(TaskState.IN_PROGRESS, findTask.getState());
    }

    @Test
    void submitEditTaskForm_ValidDetails_UpdatesExistingTask() throws Exception {
        int numTasksBefore = renovation.getTasks().size();
        mockMvc.perform(post("/myRenovations/{id}/editTask/{taskId}", renovationId, taskId).session(authenticationSession).with(csrf())
                        .param("taskName", "New Name")
                        .param("taskDescription", "New Description")
                        .param("taskDueDate", "2026-07-01")
                        .param("dateInvalid", "")
                        .param("state", "Blocked")
                        .param("pageNumber", "2")
                        .param("tab", "tasks"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovation/" + renovationId + "/tasks/" + taskId));

        assertThat(renovation.getTasks().size()).isEqualTo(numTasksBefore);

        Task findTask = renovation.getTasks().stream().filter(t -> taskId == t.getId()).findFirst()
                .orElseThrow(() -> new IllegalStateException("Task " + taskId + " not found"));

        assertEquals("New Name", findTask.getName());
        assertEquals("New Description", findTask.getDescription());
        assertEquals(TaskState.BLOCKED, findTask.getState());
        assertEquals(LocalDate.of(2026, 7, 1), findTask.getDueDate());
    }

    @Test
    void getTaskForm_WhenCreateTaskClicked_ThenLoadsCreateTaskPage() throws Exception {
        mockMvc.perform(get("/myRenovations/{id}/newTask", renovationId).session(authenticationSession))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/createEditTaskPage"))
                .andExpect(model().attributeExists("renovation", "isNewTask", "formTitle", "buttonName", "taskState"))
                .andExpect(model().attribute("isNewTask", true))
                .andExpect(model().attribute("taskState", TaskState.NOT_STARTED));
    }

    @Test
    void getEditTaskForm_WhenEditClicked_ThenLoadsEditTaskPage() throws Exception {
        mockMvc.perform(get("/myRenovations/{id}/editTask/{taskId}", renovationId, taskId).session(authenticationSession))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/renovation/createEditTaskPage"))
                .andExpect(model().attributeExists("task", "taskName", "taskDescription", "roomIds", "isNewTask", "pageNumber", "taskState"))
                .andExpect(model().attribute("isNewTask", false));
    }

    @Test
    void submitEditTaskForm_RemoveAllRooms_UpdatesExistingTask() throws Exception {
        int numTasksBefore = renovation.getTasks().size();
        Room room = new Room(renovation, "Room 1");
        renovation.addRoom(room);
        task.addRoom(room);
        renovationRepository.save(renovation);

        mockMvc.perform(post("/myRenovations/{id}/editTask/{taskId}", renovationId, taskId).session(authenticationSession).with(csrf())
                        .param("taskName", "taskName")
                        .param("taskDescription", "taskDescription")
                        .param("dateInvalid", "")
                        .param("state", "Blocked")
                        .param("pageNumber", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovation/" + renovationId + "/tasks/" + taskId));

        assertThat(renovation.getTasks().size()).isEqualTo(numTasksBefore);

        Task findTask = renovation.getTasks().stream().filter(t -> taskId == t.getId()).findFirst()
                .orElseThrow(() -> new IllegalStateException("Task " + taskId + " not found"));
        assertNull(findTask.getRooms());
    }

    @Test
    void submitEditTaskForm_AddOneRoom_UpdatesExistingTask() throws Exception {
        int numTasksBefore = renovation.getTasks().size();
        Room room = new Room(renovation, "Room 1");
        renovation.addRoom(room);
        renovationRepository.save(renovation);
        long roomId = room.getId();

        mockMvc.perform(post("/myRenovations/{id}/editTask/{taskId}", renovationId, taskId).session(authenticationSession).with(csrf())
                        .param("taskName", "taskName")
                        .param("taskDescription", "taskDescription")
                        .param("roomId", String.valueOf(roomId))
                        .param("dateInvalid", "")
                        .param("state", "Blocked")
                        .param("pageNumber", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovation/" + renovationId + "/tasks/" + taskId));

        assertThat(renovation.getTasks().size()).isEqualTo(numTasksBefore);

        Task findTask = renovation.getTasks().stream().filter(t -> taskId == t.getId()).findFirst()
                .orElseThrow(() -> new IllegalStateException("Task " + taskId + " not found"));
        assertEquals(1, findTask.getRooms().size());
    }

    @Test
    void submitEditTaskForm_AddTwoRooms_UpdatesExistingTask() throws Exception {
        int numTasksBefore = renovation.getTasks().size();
        Room room1 = new Room(renovation, "Room 1");
        renovation.addRoom(room1);
        Room room2 = new Room(renovation, "Room 2");
        renovation.addRoom(room2);
        renovationRepository.save(renovation);
        long room1Id = room1.getId();
        long room2Id = room2.getId();

        mockMvc.perform(post("/myRenovations/{id}/editTask/{taskId}", renovationId, taskId).session(authenticationSession).with(csrf())
                        .param("taskName", "taskName")
                        .param("taskDescription", "taskDescription")
                        .param("roomId", String.valueOf(room1Id))
                        .param("roomId", String.valueOf(room2Id))
                        .param("dateInvalid", "")
                        .param("state", "Blocked")
                        .param("pageNumber", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovation/" + renovationId + "/tasks/" + taskId));

        assertThat(renovation.getTasks().size()).isEqualTo(numTasksBefore);

        Task findTask = renovation.getTasks().stream().filter(t -> taskId == t.getId()).findFirst()
                .orElseThrow(() -> new IllegalStateException("Task " + taskId + " not found"));
        assertEquals(2, findTask.getRooms().size());
    }

    @Test
    void submitEditTaskForm_AddRoomAndRemoveRoom_UpdatesExistingTask() throws Exception {
        int numTasksBefore = renovation.getTasks().size();
        Room room1 = new Room(renovation, "Room 1");
        renovation.addRoom(room1);
        Room room2 = new Room(renovation, "Room 2");
        renovation.addRoom(room2);
        task.addRoom(room1);
        renovationRepository.save(renovation);
        long room2Id = room2.getId();

        mockMvc.perform(post("/myRenovations/{id}/editTask/{taskId}", renovationId, taskId).session(authenticationSession).with(csrf())
                        .param("taskName", "taskName")
                        .param("taskDescription", "taskDescription")
                        .param("roomId", String.valueOf(room2Id))
                        .param("dateInvalid", "")
                        .param("state", "Blocked")
                        .param("pageNumber", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovation/" + renovationId + "/tasks/" + taskId));

        assertThat(renovation.getTasks().size()).isEqualTo(numTasksBefore);

        Task findTask = renovation.getTasks().stream().filter(t -> taskId == t.getId()).findFirst()
                .orElseThrow(() -> new IllegalStateException("Task " + taskId + " not found"));
        assertEquals(1, findTask.getRooms().size());
    }

    @Test
    void submitAddExpenseForm_AddSingleExpense_UpdatesExistingTaskTotalCost() throws Exception {
        List<ExpenseDto> expense = List.of(new ExpenseDto("expenseName", "Equipment", "2025-01-03", 123.50));
        mockMvc.perform(post("/myRenovations/{id}/addTaskExpenses/{taskId}", renovationId, taskId)
                        .session(authenticationSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isOk());

        assertThat(task.getTotalCost()).isEqualTo(new BigDecimal("123.5"));
    }

    @Test
    void submitAddExpenseForm_AddMultipleExpenses_UpdatesExistingTaskTotalCost() throws Exception {
        List<ExpenseDto> expenses = List.of(new ExpenseDto("expenseName", "Equipment", "2025-01-03", 23.5),
                new ExpenseDto("expenseName1", "Equipment", "2025-05-03", 26.5),
                new ExpenseDto("expenseName2", "Equipment", "2025-01-09", 30.0));
        mockMvc.perform(post("/myRenovations/{id}/addTaskExpenses/{taskId}", renovationId, taskId)
                        .session(authenticationSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenses)))
                .andExpect(status().isOk());

        assertThat(task.getTotalCost()).isEqualTo(new BigDecimal("80.0"));
    }

    @Test
    void submitAddExpenseForm_AddExpenseWithToLongName_CorrectErrorReturned() throws Exception {
        List<ExpenseDto> expense = List.of(new ExpenseDto("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Equipment", "2025-01-03", 123.50));
        MvcResult result = mockMvc.perform(post("/myRenovations/{id}/addTaskExpenses/{taskId}", renovationId, taskId)
                        .session(authenticationSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().is4xxClientError())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Expense not added");
    }

    @Test
    void submitAddExpenseForm_AddExpenseWithNoName_CorrectErrorReturned() throws Exception {
        List<ExpenseDto> expense = List.of(new ExpenseDto("", "Equipment", "2025-01-03", 123.50));
        MvcResult result = mockMvc.perform(post("/myRenovations/{id}/addTaskExpenses/{taskId}", renovationId, taskId)
                        .session(authenticationSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().is4xxClientError())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Expense not added");
    }

    @Test
    void submitAddExpenseForm_AddExpenseWithNegativePrice_CorrectErrorReturned() throws Exception {
        List<ExpenseDto> expense = List.of(new ExpenseDto("expenseName", "Equipment", "2025-01-03", -12.0));
        MvcResult result = mockMvc.perform(post("/myRenovations/{id}/addTaskExpenses/{taskId}", renovationId, taskId)
                        .session(authenticationSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().is4xxClientError())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Expense not added");
    }

    @Test
    void submitAddExpenseForm_AddExpenseWithDateInFuture_CorrectErrorReturned() throws Exception {
        List<ExpenseDto> expense = List.of(new ExpenseDto("expenseName", "Equipment", "2030-01-03", 123.50));
        MvcResult result = mockMvc.perform(post("/myRenovations/{id}/addTaskExpenses/{taskId}", renovationId, taskId)
                        .session(authenticationSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().is4xxClientError())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Expense not added");
    }

    @Test
    void submitAddExpenseForm_AddExpenseWithInvalidDateFormat_CorrectErrorReturned() throws Exception {
        List<ExpenseDto> expense = List.of(new ExpenseDto("expenseName", "Equipment", "01/01/2025", 123.50));
        MvcResult result = mockMvc.perform(post("/myRenovations/{id}/addTaskExpenses/{taskId}", renovationId, taskId)
                        .session(authenticationSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().is4xxClientError())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Expense not added");
    }

    @Test
    void submitAddExpenseForm_InvalidTaskId_CorrectErrorReturned() throws Exception {
        List<ExpenseDto> expense = List.of(new ExpenseDto("expenseName", "Equipment", "2030-01-03", 123.50));
        MvcResult result = mockMvc.perform(post("/myRenovations/{id}/addTaskExpenses/{taskId}", renovationId, -1)
                        .session(authenticationSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }
}
