package nz.ac.canterbury.seng302.homehelper.data;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class ChatMessageTestData {

    public static Stream<Arguments> validChatMessages() {
        return Stream.of(
                "a",
                "a".repeat(1024),
                "a".repeat(2048),
                "😀",
                "😀".repeat(2048),
                "🧑‍💻".repeat(2048), // grapheme cluster emoji
                "🏳️‍🌈".repeat(2048), // multi codepoint sequence
                "漢字".repeat(1024),
                "مرحبا".repeat(409), // length 409 * 5 = 2045
                "a".repeat(1024) + "😀".repeat(1024)
        ).map(Arguments::of);
    }

    public static Stream<Arguments> tooLongChatMessages() {
        return Stream.of(
                "a".repeat(2049),
                "😀".repeat(2049),
                "🧑‍💻".repeat(2049),
                "🏳️‍🌈".repeat(2049),
                "漢字".repeat(1025),
                "مرحبا".repeat(410), // length 410 * 5 = 2050
                "a".repeat(1025) + "😀".repeat(1024)
        ).map(Arguments::of);
    }
}
