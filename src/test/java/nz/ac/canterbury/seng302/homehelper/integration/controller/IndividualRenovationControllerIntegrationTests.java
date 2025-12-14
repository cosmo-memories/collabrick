package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState.COMPLETED;
import static nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState.NOT_STARTED;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class IndividualRenovationControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private UserRepository userRepository;

    private Renovation renovation;
    private User user;

    @BeforeEach
    void setUpDatabase() {
        renovationRepository.deleteAll();
        userRepository.deleteAll();

        user = new User("Test", "user", "user@test.com", "Password123!", "Password123!");
        userRepository.save(user);

        renovation = new Renovation("TestRenovationName", "TestRenovationDescription");
        renovation.setOwner(user);
        renovationRepository.save(renovation);

        Task task1 = new Task(renovation, "Task1", "TestTaskDescription1", NOT_STARTED, "/test/filename");
        Task task2 = new Task(renovation, "Task2", "TestTaskDescription2", COMPLETED, "/test/filename");

        renovation.addTask(task1);
        renovation.addTask(task2);
    }

    static Stream<String> irrelevantStates() {
        return Stream.of(
                "IN_PROGRESS",
                "BLOCKED",
                "CANCELLED"
        );
    }

    @Test
    void getRenovation_NoStateFilter_ReturnsAllTasks() throws Exception {
        mockMvc.perform(get("/renovation/" + renovation.getId() + "/tasks")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pagination", hasProperty("items", hasSize(2))));
    }

    @Test
    void getRenovation_CompletedStateFilter_ReturnsTasks() throws Exception {
        mockMvc.perform(get("/renovation/" + renovation.getId() + "/tasks")
                        .param("states", "COMPLETED")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pagination", hasProperty("items", hasSize(1))));
    }

    @ParameterizedTest
    @MethodSource("irrelevantStates")
    void getRenovation_IrrelevantStates_ReturnsNoTasks(String state) throws Exception {
        mockMvc.perform(get("/renovation/" + renovation.getId() + "/tasks")
                        .param("states", state)
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pagination", hasProperty("items", hasSize(0))));
    }


}
