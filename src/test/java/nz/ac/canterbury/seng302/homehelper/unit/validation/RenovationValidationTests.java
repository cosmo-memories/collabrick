package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.validation.renovation.RenovationValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class RenovationValidationTests {

    /* invalid renovation names */
    @Test
    void testInvalid_renovationName_empty() {
        // make the renovation repository return null to simulate a renovation with the same name not existing
        RenovationRepository renovationRepository = Mockito.mock(RenovationRepository.class);
        when(renovationRepository.findByName(Mockito.anyString()))
                .thenReturn(Optional.empty());
        User user = new User("Test", "user", "user@test.com", "Password123!", "Password123!");
        String errors = RenovationValidation.validateRenovationName(renovationRepository, "", -1, user);
        assertEquals(RenovationValidation.RENOVATION_NAME_EMPTY_MESSAGE, errors);
    }

    @Test
    void testInvalid_renovationName_whitespace() {
        // make the renovation repository return null to simulate a renovation with the same name not existing
        RenovationRepository renovationRepository = Mockito.mock(RenovationRepository.class);
        when(renovationRepository.findByName(Mockito.anyString()))
                .thenReturn(Optional.empty());
        User user = new User("Test", "user", "user@test.com", "Password123!", "Password123!");
        String errors = RenovationValidation.validateRenovationName(renovationRepository, "     ", -1, user);
        assertEquals(RenovationValidation.RENOVATION_NAME_EMPTY_MESSAGE, errors);
    }

    @Test
    void testInvalid_renovationName_specialCharacters() {
        // make the renovation repository return null to simulate a renovation with the same name not existing
        RenovationRepository renovationRepository = Mockito.mock(RenovationRepository.class);
        when(renovationRepository.findByName(Mockito.anyString()))
                .thenReturn(Optional.empty());
        User user = new User("Test", "user", "user@test.com", "Password123!", "Password123!");
        String errors = RenovationValidation.validateRenovationName(renovationRepository, "Invalid@Name!", -1, user);
        assertEquals(RenovationValidation.RENOVATION_NAME_INVALID_MESSAGE, errors);
    }

    @Test
    void testInvalid_renovationName_matchingExistingRenovation() {
        // make the renovation repository return a renovation to simulate a renovation with the same name existing
        String renovationName = "My Renovation";
        User user = new User("Test", "user", "user@test.com", "Password123!", "Password123!");
        RenovationRepository renovationRepository = Mockito.mock(RenovationRepository.class);
        when(renovationRepository.findByNameAndUser(Mockito.anyString(), Mockito.eq(user)))
                .thenReturn(List.of(new Renovation(renovationName, "Description")));

        String errors = RenovationValidation.validateRenovationName(renovationRepository, renovationName, -1, user);
        assertEquals(RenovationValidation.RENOVATION_NAME_NOT_UNIQUE_MESSAGE, errors);
    }

    @Test
    void testInvalid_renovationName_matchingExistingRenovationWithLeadingWhitespace() {
        // make the renovation repository return a renovation to simulate a renovation with the same name existing
        String renovationName = "My Renovation";
        User user = new User("Test", "user", "user@test.com", "Password123!", "Password123!");
        RenovationRepository renovationRepository = Mockito.mock(RenovationRepository.class);
        when(renovationRepository.findByNameAndUser(Mockito.anyString(), Mockito.eq(user)))
                .thenReturn(List.of(new Renovation(renovationName, "Description")));
        String errors = RenovationValidation.validateRenovationName(renovationRepository, "   " + renovationName, -1, user);
        assertEquals(RenovationValidation.RENOVATION_NAME_NOT_UNIQUE_MESSAGE, errors);
    }

    @Test
    void testInvalid_renovationName_matchingExistingRenovationWithTrailingWhitespace() {
        // make the renovation repository return a renovation to simulate a renovation with the same name existing
        String renovationName = "My Renovation";
        User user = new User("Test", "user", "user@test.com", "Password123!", "Password123!");
        RenovationRepository renovationRepository = Mockito.mock(RenovationRepository.class);
        when(renovationRepository.findByNameAndUser(Mockito.anyString(), Mockito.eq(user)))
                .thenReturn(List.of(new Renovation(renovationName, "Description")));
        String errors = RenovationValidation.validateRenovationName(renovationRepository, renovationName + "   ", -1, user);
        assertEquals(RenovationValidation.RENOVATION_NAME_NOT_UNIQUE_MESSAGE, errors);
    }

    @Test
    void testInvalid_renovationName_emojis() {
        // make the renovation repository return null to simulate a renovation with the same name not existing
        RenovationRepository renovationRepository = Mockito.mock(RenovationRepository.class);
        when(renovationRepository.findByName(Mockito.anyString()))
                .thenReturn(Optional.empty());
        User user = new User("Test", "user", "user@test.com", "Password123!", "Password123!");
        // Cool Reno 😎
        String errors = RenovationValidation.validateRenovationName(renovationRepository, "Cool Reno \uD83D\uDE0E", -1, user);
        assertEquals(RenovationValidation.RENOVATION_NAME_INVALID_MESSAGE, errors);
    }

    /* valid renovation names */
    @Test
    void testValidRenovationName_allowedPunctuation() {
        // make the renovation repository return null to simulate a renovation with the same name not existing
        RenovationRepository renovationRepository = Mockito.mock(RenovationRepository.class);
        when(renovationRepository.findByName(Mockito.anyString())).thenReturn(Optional.empty());
        User user = new User("Test", "user", "user@test.com", "Password123!", "Password123!");
        String errors = RenovationValidation.validateRenovationName(renovationRepository, ". ,-'", -1, user);
        assertEquals(RenovationValidation.RENOVATION_NAME_INVALID_MESSAGE, errors);
    }

    @Test
    void testValid_renovationName_notMatchingExistingRenovation() {
        // make the renovation repository return null to simulate a renovation with the same name not existing
        RenovationRepository renovationRepository = Mockito.mock(RenovationRepository.class);
        when(renovationRepository.findByName(Mockito.anyString()))
                .thenReturn(Optional.empty());
        User user = new User("Test", "user", "user@test.com", "Password123!", "Password123!");
        String errors = RenovationValidation.validateRenovationName(renovationRepository, "My Renovation 2", -1, user);
        assertEquals("", errors);
    }

    @Test
    void testValid_renovationName_allowedWithHyphensDotsCommas() {
        // make the renovation repository return null to simulate a renovation with the same name not existing
        RenovationRepository renovationRepository = Mockito.mock(RenovationRepository.class);
        when(renovationRepository.findByName(Mockito.anyString()))
                .thenReturn(Optional.empty());
        User user = new User("Test", "user", "user@test.com", "Password123!", "Password123!");
        String errors = RenovationValidation.validateRenovationName(renovationRepository, "my renovation.2-0", -1, user);
        assertEquals("", errors);
    }

    /* invalid room names */
    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "\t",
            "\n",
            "    \t  \n  ",
    })
    void validateRoomName_emptyOrWhitespaceRoomName_returnsErrorMessage(String roomName) {
        String errors = RenovationValidation.validateRoomName(roomName);
        assertEquals(RenovationValidation.ROOM_NAME_EMPTY_MESSAGE, errors);
    }


    @ParameterizedTest
    @ValueSource(strings = {
            // Most of these test cases were generated using ChatGPT
            "Cool Room \uD83D\uDE0E",
            "..",
            ",,",
            "--",
            "''",
            "...",
            ",,,",
            "---",
            "'''",
            "-.-",
            "-..",
            ".,.",
            "--..",
            "!@#$%^",
            "Room @123",
            "Room/1",
            "Room#1",
            "&Room2",
            " Room/101",
            "Ro@om 101",
            "Room 101!",
            "Room$123",
            "Room_101",
            "Room:101",
            "Room;101",
            "Room_101",
            "101-@Room",
    })
    void validateRoomName_invalidRoomName_returnsErrorMessage(String roomName) {

        String errors = RenovationValidation.validateRoomName(roomName);
        assertEquals(RenovationValidation.ROOM_NAME_INVALID_MESSAGE, errors);
    }

    /* valid room names */
    @ParameterizedTest
    @ValueSource(strings = {
            // Test cases generated using ChatGPT
            "Room 101",
            "Conference-A",
            "John's Office",
            "Main Hall",
            "Section 2B",
            "A1",
            "B-12",
            "Meeting Room, East",
            "Room No. 3",
            "Lab-Alpha",
            "Office.5",
            "Hallway 1",
            "Admin's Area",
            "Room, 'C'",
            "Section. 4A",
            "my renovation.2-0"
    })
    void validateRoomName_validRoomName_returnsEmptyString(String roomName) {

        String errors = RenovationValidation.validateRoomName(roomName);
        assertEquals("", errors);
    }

    /* invalid renovation description */
    @Test
    void testInvalid_renovationDescription_oneCharTooLong() {
        // description with 1 more character than the limit
        String description = "A".repeat(RenovationValidation.RENOVATION_DESCRIPTION_CHAR_LIMIT + 1);
        String error = RenovationValidation.validateRenovationDescription(description);
        assertEquals(RenovationValidation.RENOVATION_DESCRIPTION_TOO_LONG_MESSAGE, error);
    }

    @Test
    void testInvalid_renovationDescription_twiceCharTooLong() {
        // description with 1 more character than the limit
        String description = "A".repeat(RenovationValidation.RENOVATION_DESCRIPTION_CHAR_LIMIT * 2);
        String error = RenovationValidation.validateRenovationDescription(description);
        assertEquals(RenovationValidation.RENOVATION_DESCRIPTION_TOO_LONG_MESSAGE, error);
    }

    /* valid renovation description */
    @Test
    void testValid_renovationDescription_empty() {
        String error = RenovationValidation.validateRenovationDescription("");
        assertEquals("", error);
    }

    @Test
    void testValid_renovationDescription_exactLimit() {
        // description with number characters that match the limit
        String description = "A".repeat(RenovationValidation.RENOVATION_DESCRIPTION_CHAR_LIMIT);
        String error = RenovationValidation.validateRenovationDescription(description);
        assertEquals("", error);
    }

    @Test
    void testValid_renovationDescription_halfLimit() {
        // description with number characters that match the limit
        String description = "A".repeat(RenovationValidation.RENOVATION_DESCRIPTION_CHAR_LIMIT / 2);
        String error = RenovationValidation.validateRenovationDescription(description);
        assertEquals("", error);
    }

    @Test
    void testValid_taskName_Correct() {
        String taskName = "Cool Task";
        assertEquals("", RenovationValidation.validateTaskName(taskName));
    }

    @Test
    void testValid_taskName_hasComa() {
        String taskName = "Cool Task'";
        String error = RenovationValidation.validateTaskName(taskName);
        assertEquals("", error);
    }

    @Test
    void testValid_taskName_hasInvalidHash() {
        String taskName = "#Cool Task";
        String error = RenovationValidation.validateTaskName(taskName);
        assertEquals(RenovationValidation.TASK_NAME_INVALID_MESSAGE, error);
    }

    @Test
    void testValid_taskName_hasNumber() {
        String taskName = "Cool Task123";
        String error = RenovationValidation.validateTaskName(taskName);
        assertEquals("", error);
    }

    @Test
    void testValid_taskName_hasInvalidEmoji() {
        String taskName = "Cool Task";
        taskName += new String(Character.toChars(0x1F349));
        String error = RenovationValidation.validateTaskName(taskName);
        assertEquals(RenovationValidation.TASK_NAME_INVALID_MESSAGE, error);
    }

    @Test
    void testValid_taskName_empty() {
        String taskName = "";
        String error = RenovationValidation.validateTaskName(taskName);
        assertEquals(RenovationValidation.TASK_NAME_INVALID_MESSAGE, error);
    }

    @Test
    void testValid_taskName_hasDots() {
        String taskName = "Cool.Task";
        String error = RenovationValidation.validateTaskName(taskName);
        assertEquals("", error);
    }

    @Test
    void testValid_taskDescription_correct() {
        String taskDescription = "Cool Description";
        String error = RenovationValidation.validateTaskDescription(taskDescription);
        assertEquals("", error);
    }

    @Test
    void testValid_taskDescription_oneCharTooLong() {
        String taskDescription = "A".repeat(RenovationValidation.RENOVATION_DESCRIPTION_CHAR_LIMIT + 1);
        String error = RenovationValidation.validateTaskDescription(taskDescription);
        assertEquals(RenovationValidation.TASK_DESCRIPTION_TOO_LONG_MESSAGE, error);
    }

    @Test
    void testValid_taskDescription_exactCharCount() {
        String taskDescription = "A".repeat(RenovationValidation.RENOVATION_DESCRIPTION_CHAR_LIMIT);
        String error = RenovationValidation.validateTaskDescription(taskDescription);
        assertEquals("", error);
    }

    @Test
    void testValid_taskDescription_empty() {
        String taskDescription = "";
        String error = RenovationValidation.validateTaskDescription(taskDescription);
        assertEquals(RenovationValidation.TASK_DESCRIPTION_EMPTY_MESSAGE, error);
    }

    @Test
    void testValid_taskDescription_hasEmoji() {
        String taskDescription = "Cool Description";
        taskDescription += new String(Character.toChars(0x1F349));
        String error = RenovationValidation.validateTaskDescription(taskDescription);
        assertEquals("", error);
    }

    @Test
    void testValid_taskDueDate_correct() {
        LocalDate today = LocalDate.now(); // Get today's date
        LocalDate dueDate = today.plusDays(1);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = dueDate.format(dateTimeFormatter);// Add one day to today
        String error = RenovationValidation.validateTaskDueDate(formattedDate);
        assertEquals("", error);
    }

    @Test
    void testValid_taskDueDate_today() {
        LocalDate dueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = dueDate.format(dateTimeFormatter);
        System.out.println(formattedDate);// Get today's date
        String error = RenovationValidation.validateTaskDueDate(formattedDate);
        assertEquals("", error);
    }

    @Test
    void testValid_taskDueDate_past() {
        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.minusDays(1);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = dueDate.format(dateTimeFormatter);
        String error = RenovationValidation.validateTaskDueDate(formattedDate);
        assertEquals(RenovationValidation.TASK_DUE_DATE_IN_PAST_MESSAGE, error);
    }

    @Test
    void testValid_taskDueDate_yearHasFiveDigits() {
        String date = "10000-01-01";
        String error = RenovationValidation.validateTaskDueDate(date);
        assertEquals(RenovationValidation.TASK_DUE_DATE_INVALID, error);
    }

    @Test
    void testValid_taskDueDate_yearIsABunchOfZeros() {
        String date = "0000-00-00";
        String error = RenovationValidation.validateTaskDueDate(date);
        assertEquals(RenovationValidation.TASK_DUE_DATE_INVALID, error);
    }
}
