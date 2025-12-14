package nz.ac.canterbury.seng302.homehelper.utility;

import java.text.BreakIterator;
import java.util.Locale;

/**
 * Utility class for strings.
 */
public class StringUtils {

    /**
     * Counts the number of user-perceived characters (grapheme clusters) in a string.
     * A grapheme cluster represents a single visible character to a user, even if it is
     * composed of multiple Unicode code points (e.g. emojis like 🧑‍💻 or flags like 🏳️‍🌈).
     * taken from https://stackoverflow.com/a/76109241
     *
     * @param message the input string to count grapheme clusters for
     * @return the number of grapheme clusters in {@code message}
     */
    public static int countGraphemeClusters(String message) {
        BreakIterator it = BreakIterator.getCharacterInstance(Locale.ROOT);
        it.setText(message);
        int count = 0;
        while (it.next() != BreakIterator.DONE) {
            count++;
        }
        return count;
    }
}
