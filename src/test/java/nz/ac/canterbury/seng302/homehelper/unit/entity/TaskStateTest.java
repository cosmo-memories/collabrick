package nz.ac.canterbury.seng302.homehelper.unit.entity;

import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskStateTest {

    @Test
    void testFromString_ValidStates() {
        assertEquals(Optional.of(TaskState.NOT_STARTED), TaskState.fromString("NOT_STARTED"));
        assertEquals(Optional.of(TaskState.IN_PROGRESS), TaskState.fromString("IN_PROGRESS"));
        assertEquals(Optional.of(TaskState.BLOCKED), TaskState.fromString("BLOCKED"));
        assertEquals(Optional.of(TaskState.COMPLETED), TaskState.fromString("COMPLETED"));
        assertEquals(Optional.of(TaskState.CANCELLED), TaskState.fromString("CANCELLED"));
    }

    @Test
    void testFromString_CaseInsensitive() {
        assertEquals(Optional.of(TaskState.NOT_STARTED), TaskState.fromString("not_started"));
        assertEquals(Optional.of(TaskState.IN_PROGRESS), TaskState.fromString("in_progress"));
        assertEquals(Optional.of(TaskState.BLOCKED), TaskState.fromString("BLOCKED"));
        assertEquals(Optional.of(TaskState.COMPLETED), TaskState.fromString("completed"));
        assertEquals(Optional.of(TaskState.CANCELLED), TaskState.fromString("CaNcElLeD"));
    }

    @Test
    void testFromString_SpacesInsteadOfUnderscore() {
        assertEquals(Optional.of(TaskState.NOT_STARTED), TaskState.fromString("not started"));
        assertEquals(Optional.of(TaskState.IN_PROGRESS), TaskState.fromString("in progress"));
    }

    @Test
    void testFromString_InvalidState() {
        assertEquals(Optional.empty(), TaskState.fromString("INVALID_STATE"));
        assertEquals(Optional.empty(), TaskState.fromString(""));
        assertEquals(Optional.empty(), TaskState.fromString("123"));
    }
}
