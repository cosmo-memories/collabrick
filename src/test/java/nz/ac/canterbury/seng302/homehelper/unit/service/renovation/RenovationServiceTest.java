package nz.ac.canterbury.seng302.homehelper.unit.service.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Room;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.TaskDetailsExceptions;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole;
import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RoomRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.TaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BrickAiService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.validation.renovation.RenovationValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState.COMPLETED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RenovationServiceTest {

    @Mock
    private RenovationRepository renovationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BrickAiService brickAiService;

    @Mock
    private ChatChannelService chatChannelService;

    @InjectMocks
    private RenovationService renovationService;


    private Renovation renovation;

    private Room room;

    private User user;

    private User aiUser;

    private ChatChannel generalChannel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        renovation = new Renovation("Kitchen Renovation", "Renovating the kitchen");
        renovation.setId(1L);
        room = new Room(renovation, "Renovation Room");
        renovation.addRoom(room);

        brickAiService.createAiChannel(renovation, user);
        brickAiService.createAiChannel(renovation, user);

        user = new User("fname", "lname", "email@email.com", "password", "password");
        user.setActivated(true);
        userRepository.save(user);
        user.setId(6L);
        renovation.setOwner(user);

        aiUser = mock(User.class);
        aiUser.setId(10L);
        generalChannel = new ChatChannel("general", renovation);
        generalChannel.setId(11L);
        renovation.getChatChannels().add(generalChannel);
        renovation.getChatChannels().getFirst().addMember(user);
        renovation.getChatChannels().getFirst().addMember(aiUser);
        renovation.getChatChannels().add(new ChatChannel("brickAI", renovation));
        renovation.getChatChannels().getLast().addMember(user);
        renovation.getChatChannels().getLast().addMember(aiUser);

        // Set up mock behavior for renovationRepository
        when(renovationRepository.findById(1L)).thenReturn(java.util.Optional.of(renovation));
    }

    @Test
    void testSaveTask() {
        Task task = new Task(renovation, "Task Name", "Description", "image");
        renovationService.saveTask(1L, task, null);
        verify(renovationRepository, times(1)).save(renovation);
    }

    @Test
    void testSaveTaskWithRoom() {
        Task task = new Task(renovation, "Task Name", "Description", "image");
        task.addRoom(room);
        renovationService.saveTask(1L, task, null);
        verify(renovationRepository, times(1)).save(renovation);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "@Invalid",
            "John!",
            "#Room",
            "Mary_Jane",
            "Jean*Luc",
            "123@abc",
            "---",
            "...",
            "' -",
            "John\nDoe"
    })
    void testSaveTask_InvalidNames_ThrowsTaskDetailsException(String invalidName) {
        Task task = new Task(renovation, invalidName, "Valid description", "image");

        TaskDetailsExceptions exception = assertThrows(TaskDetailsExceptions.class, () -> {
            renovationService.saveTask(1L, task, null);
        });
        verify(renovationRepository, times(0)).save(renovation);
        assertEquals(RenovationValidation.TASK_NAME_INVALID_MESSAGE,
                exception.getNameErrorMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void testSaveTask_InvalidDescription_ThrowsTaskDetailsExceptions(String invalidDescription) {
        Task task = new Task(renovation, "Valid Name", invalidDescription, "image");

        TaskDetailsExceptions exception = assertThrows(TaskDetailsExceptions.class, () -> {
            renovationService.saveTask(1L, task, null);
        });
        verify(renovationRepository, times(0)).save(renovation);
        assertEquals("Task description cannot be empty", exception.getDescriptionErrorMessage());
    }

    @Test
    void testSaveTask_NullDescription_ThrowsTaskDetailsExceptions() {
        Task task = new Task(renovation, "Valid Name", null, "image");

        TaskDetailsExceptions exception = assertThrows(TaskDetailsExceptions.class, () -> {
            renovationService.saveTask(1L, task, null);
        });
        verify(renovationRepository
                , times(0)).save(renovation);
        assertEquals("Task description cannot be empty", exception.getDescriptionErrorMessage());
    }

    @Test
    void testSaveTask_TooLongDescription_ThrowsTaskDetailsExceptions() {
        String longDescription = "a".repeat(513);
        Task task = new Task(renovation, "Valid Name", longDescription, "image");

        TaskDetailsExceptions exception = assertThrows(TaskDetailsExceptions.class, () -> {
            renovationService.saveTask(1L, task, null);
        });
        verify(renovationRepository, times(0)).save(renovation);
        assertEquals("Task description must be 512 characters or less", exception.getDescriptionErrorMessage());
    }

    @Test
    void testSaveTask_512CharDesc_TaskSaved() {
        String longDescription = "a".repeat(512);
        Task task = new Task(renovation, "Valid Name", longDescription, "image");
        renovationService.saveTask(1L, task, null);
        verify(renovationRepository, times(1)).save(renovation);
    }

    @Test
    void filterTaskStates_noStates_returnsAllTasks() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        Task mockTask = new Task(renovation, "Task Name", "Description", COMPLETED, "image");
        when(taskRepository.findTasksByDateRange(startDate, endDate, renovation.getId()))
                .thenReturn(List.of(mockTask));

        List<Task> result = renovationService.getTasksByDateRangeAndStates(startDate, endDate, renovation.getId(), null);

        assertEquals(1, result.size());
        assertEquals(mockTask, result.get(0));
        verify(taskRepository).findTasksByDateRange(startDate, endDate, renovation.getId());
        verify(taskRepository, never()).findTasksByDateRangeAndStates(any(), any(), any(), any());
    }

    @Test
    void filterTaskStates_completeStates_returnsAllTasks() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        List<TaskState> states = List.of(TaskState.IN_PROGRESS, COMPLETED);
        Task mockTask = new Task(renovation, "Task Name", "Description", COMPLETED, "image");

        when(taskRepository.findTasksByDateRangeAndStates(startDate, endDate, renovation.getId(), states)).thenReturn(List.of(mockTask));
        List<Task> result = renovationService.getTasksByDateRangeAndStates(startDate, endDate, renovation.getId(), states);

        assertEquals(mockTask, result.getFirst());
        verify(taskRepository).findTasksByDateRangeAndStates(startDate, endDate, renovation.getId(), states);
        verify(taskRepository, never()).findTasksByDateRange(any(), any(), any());

    }

    @Test
    void filterTaskStates_irrelevantStates_returnsEmptyList() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        List<TaskState> states = List.of(TaskState.IN_PROGRESS);

        when(taskRepository.findTasksByDateRangeAndStates(startDate, endDate, renovation.getId(), states))
                .thenReturn(List.of());

        List<Task> result = renovationService.getTasksByDateRangeAndStates(startDate, endDate, renovation.getId(), states);

        assertTrue(result.isEmpty());
        verify(taskRepository).findTasksByDateRangeAndStates(startDate, endDate, renovation.getId(), states);
        verify(taskRepository, never()).findTasksByDateRange(any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0000-00-00",
            "00-00-0000",
            "123-2323-123",
            "ab-brb-brrg",
            "biirtjihgrtihjjoitjhrt",
            "32-03-3000"
    })
    void saveNewTask_InvalidDate_throwsError(String date) {
        Task mockTask = new Task(renovation, "Task Name", "Description", COMPLETED, "image");
        assertThrows(TaskDetailsExceptions.class, () -> {
            renovationService.saveTask(renovation.getId(), mockTask, date);
        });
        Optional<Task> savedTask = taskRepository.findTaskById(mockTask.getId());
        assertFalse(savedTask.isPresent());
    }

    @Test
    void toggleAllowBrickAI_turnPermissionsOff_brickAIRemovedFromAllChannelsAndAIChannelRemoved() {
        User member1 = mock(User.class);
        renovation.addMember(member1, RenovationMemberRole.MEMBER);
        renovation.getChatChannels().getFirst().addMember(member1);
        renovation.getChatChannels().add(new ChatChannel("brickAI", renovation));
        renovation.getChatChannels().getLast().addMember(member1);
        renovation.getChatChannels().getLast().addMember(aiUser);
        User member2 = mock(User.class);
        renovation.addMember(member2, RenovationMemberRole.MEMBER);
        renovation.getChatChannels().getFirst().addMember(member2);
        renovation.getChatChannels().add(new ChatChannel("brickAI", renovation));
        renovation.getChatChannels().getLast().addMember(member2);
        renovation.getChatChannels().getLast().addMember(aiUser);

        when(brickAiService.getAiUser()).thenReturn(aiUser);

        renovationService.toggleAllowBrickAI(renovation);

        verify(chatChannelService, times(1)).removeMemberFromChatChannel(generalChannel, aiUser);
        verify(renovationRepository, times(1)).save(renovation);
        assertFalse(renovation.isAllowBrickAI());
        assertEquals(1, renovation.getChatChannels().size());
        assertEquals(generalChannel, renovation.getChatChannels().getFirst());
    }

    @Test
    void toggleAllowBrickAI_turnPermissionsOn_brickAIAddedAsMemberToChannelsAndAIChannelCreatedForAllUsers() {
        User member1 = mock(User.class);
        renovation.addMember(member1, RenovationMemberRole.MEMBER);
        renovation.getChatChannels().getFirst().addMember(member1);
        User member2 = mock(User.class);
        renovation.addMember(member2, RenovationMemberRole.MEMBER);
        renovation.getChatChannels().getFirst().addMember(member2);
        renovation.getChatChannels().removeIf(c -> c.getName().equals("brickAI"));
        ChatChannel newChatChannel = new ChatChannel("New Channel", renovation);
        renovation.getChatChannels().add(newChatChannel);
        System.out.println(renovation.getMembers());
        renovation.setAllowBrickAI(false);


        when(brickAiService.getAiUser()).thenReturn(aiUser);
        System.out.println(renovation.getMembers());

        renovationService.toggleAllowBrickAI(renovation);
        verify(brickAiService, times(1)).createAiChannel(renovation, user);
        verify(brickAiService, times(1)).createAiChannel(renovation, member1);
        verify(brickAiService, times(1)).createAiChannel(renovation, member2);
        verify(chatChannelService, times(1)).addMemberToChatChannel(generalChannel.getId(), aiUser.getId());
        verify(chatChannelService, times(1)).addMemberToChatChannel(newChatChannel.getId(), aiUser.getId());
        verify(renovationRepository, times(1)).save(renovation);
        assertTrue(renovation.isAllowBrickAI());

    }



}
