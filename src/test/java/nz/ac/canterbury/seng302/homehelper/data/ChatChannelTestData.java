package nz.ac.canterbury.seng302.homehelper.data;

import java.util.stream.Stream;

public class ChatChannelTestData {
    public static Stream<String> validChannelNames() {
        return Stream.of(
                "general",
                "renovations",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // 64 characters
                "1111111111111111111111111111111111111111111111111111111111111111",
                "1",
                "a",
                "second-chat",
                "things todo",
                "task.roof",
                "hayden's chat",
                "a1 .-'"
        );
    }

    public static Stream<String> invalidChannelNames() {
        return Stream.of(
                ".",
                "'",
                "-",
                ",",
                "this, that, and them"
        );
    }

    public static Stream<String> tooLongChannelNames() {
        return Stream.of(
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // 65 characters
                "11111111111111111111111111111111111111111111111111111111111111111"
        );
    }

    public static Stream<String> emptyChannelNames() {
        return Stream.of(
                "",
                "\n",
                "\t",
                " ",
                "    "
        );
    }


}
