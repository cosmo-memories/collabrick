package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatMessageException;
import nz.ac.canterbury.seng302.homehelper.validation.chat.ChatValidation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class ChatValidationTests {

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "\t",
            "\n",
            "    \t  \n  ",
    })
    void testValidateChatMessage_GivenBlankMessage_ThrowsException() {
        ChatMessageException exception = assertThrows(
                ChatMessageException.class,
                () -> ChatValidation.validateChatMessage("")
        );
        assertEquals(ChatValidation.CHAT_MESSAGE_BLANK, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.ChatMessageTestData#tooLongChatMessages")
    void testValidateChatMessage_GivenTooLongMessage_ThrowsException(String message) {
        ChatMessageException exception = assertThrows(
                ChatMessageException.class,
                () -> ChatValidation.validateChatMessage(message)
        );
        assertEquals(ChatValidation.CHAT_MESSAGE_TOO_LONG, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.ChatMessageTestData#validChatMessages")
    void validateChatMessage_GivenValidMessage_doesNotThrow(String message) {
        assertDoesNotThrow(() -> ChatValidation.validateChatMessage(message));
    }
}
