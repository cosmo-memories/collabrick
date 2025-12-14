package nz.ac.canterbury.seng302.homehelper.unit.model.chat.fragment;

import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentText;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatMessageFragmentTextTests {

    @Test
    void testGetType_WhenCalled_ThenReturnsTextType() {
        ChatMessageFragmentText fragment = new ChatMessageFragmentText("Hello");
        assertEquals(ChatMessageFragmentType.TEXT, fragment.getType());
    }
}
