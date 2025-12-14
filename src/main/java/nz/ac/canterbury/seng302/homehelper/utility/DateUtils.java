package nz.ac.canterbury.seng302.homehelper.utility;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utility class providing methods to format date and times suitable for displaying to the AI
 */
public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.ENGLISH);

    /**
     * Formats a LocalDateTime as: 2025-09-26T13:45 (Friday, September 26, 2025 at 1:45 PM)
     */
    public static String formatDateForAi(LocalDateTime dateTime) {
        return dateTime + " (" + dateTime.format(DATE_TIME_FORMATTER) + ")";
    }

    /**
     * Formats a LocalDate as: 2025-09-26 (Friday, September 26, 2025)
     */
    public static String formatDateForAi(LocalDate date) {
        return date + " (" + date.format(DATE_FORMATTER) + ")";
    }


    /**
     * Formats an Instant to a string in the local timezone as:
     * 2025-09-26T13:45:00Z (Friday, September 26, 2025 at 1:45 PM)
     *
     * @param instant the Instant to format
     * @return formatted string
     */
    public static String formatDateForAi(Instant instant) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return instant + " (" + localDateTime.format(DATE_TIME_FORMATTER) + ")";
    }
}
