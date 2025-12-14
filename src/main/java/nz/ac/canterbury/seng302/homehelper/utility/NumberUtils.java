package nz.ac.canterbury.seng302.homehelper.utility;

import java.util.Optional;

/**
 * Utility class for safe number parsing operations.
 */
public class NumberUtils {

    /**
     * Attempts to parse the provided string into an Integer. If the string is null or is not a valid integer, the
     * method returns an empty optional, instead of throwing an exception.
     *
     * @param string the string to parse into an integer
     * @return an Optional containing the parsed integer, or an empty optional if parsing fails.
     */
    public static Optional<Integer> tryParseInt(String string) {
        if (string == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(string));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
