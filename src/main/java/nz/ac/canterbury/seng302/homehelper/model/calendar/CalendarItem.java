package nz.ac.canterbury.seng302.homehelper.model.calendar;

import java.time.LocalDate;

/**
 * Represents an item that can be placed on a calendar.
 */
public interface CalendarItem {
    /**
     * Gets the date on which this item occurs.
     *
     * @return the LocalDate of the calendar item.
     */
    LocalDate getDueDate();

    /**
     * Gets the name or description of this calendar item.
     *
     * @return the name or description of this calendar item.
     */
    String getName();
}
