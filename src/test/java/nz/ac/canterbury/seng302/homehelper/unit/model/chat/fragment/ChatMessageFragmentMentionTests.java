package nz.ac.canterbury.seng302.homehelper.unit.model.chat.fragment;

import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentMention;
import nz.ac.canterbury.seng302.homehelper.model.chat.fragment.ChatMessageFragmentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatMessageFragmentMentionTests {

    @Test
    void testGetType_WhenCalled_ThenReturnsTextType() {
        ChatMessageFragmentMention fragment = new ChatMessageFragmentMention(1, "Alice Smith");
        assertEquals(ChatMessageFragmentType.MENTION, fragment.getType());
    }
}
