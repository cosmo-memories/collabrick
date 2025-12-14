package nz.ac.canterbury.seng302.homehelper.model.calendar;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a single cell in the calendar grid. This class is used to organize calendar items for rendering in a
 * calendar view, where each cell represents a day in the month.
 */
public record CalendarCell(
        LocalDate date,
        List<CalendarItem> items,
        boolean isCurrentMonth,
        boolean isCurrentDay) {

    /**
     * Returns the day of the month for the current date.
     *
     * @return the day of the month.
     */
    public int dayOfMonth() {
        return date.getDayOfMonth();
    }

    /**
     * Returns a string version of the date for a cell
     *
     * @return a string version of the date for a cell
     */
    public String cellDate() {
        return date.toString();
    }
}
