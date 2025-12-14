package nz.ac.canterbury.seng302.homehelper.model.calendar;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Represents a calendar structure for a given month and year. It includes a list of date cells to display in the UI,
 * a list of items associated with dates, and the names of the current, previous and next months.
 *
 * @param cells         A cells to show in the calendar grid.
 * @param currentMonth  The current month.
 * @param previousMonth The previous month.
 * @param nextMonth     The next month.
 * @param currentYear   The current year.
 */
public record Calendar(
        List<CalendarCell> cells,
        Month currentMonth,
        Month previousMonth,
        Month nextMonth,
        int currentYear) {

    /**
     * Returns the name of the previous month.
     *
     * @return the name of the previous month in full.
     */
    public String previousMonthName() {
        return getMonthName(previousMonth);
    }

    /**
     * Returns the numerical value of the previous month.
     *
     * @return the numerical value of the previous month (1-12).
     */
    public int previousMonthId() {
        return previousMonth.getValue();
    }

    /**
     * Returns the year of the previous month. If the current month is January, the year will be decremented.
     *
     * @return the year of the previous month.
     */
    public int previousMonthYear() {
        return currentMonth == Month.JANUARY ? currentYear - 1 : currentYear;
    }

    /**
     * Returns the name of the next month.
     *
     * @return the name of the next month in full.
     */
    public String nextMonthName() {
        return getMonthName(nextMonth);
    }

    /**
     * Returns the numerical value of the next month.
     *
     * @return the numerical value of the previous month (1-12).
     */
    public int nextMonthId() {
        return nextMonth.getValue();
    }

    /**
     * Returns the year of the next month. If the current month is December, the year will be incremented.
     *
     * @return the year of the next month.
     */
    public int nextMonthYear() {
        return currentMonth == Month.DECEMBER ? currentYear + 1 : currentYear;
    }


    /**
     * Returns the name of the current month.
     *
     * @return the name of the current month in full.
     */
    public String currentMonthName() {
        return getMonthName(currentMonth);
    }

    /**
     * Returns the full name of a given month.
     *
     * @param month the month whose name is to be returned.
     * @return the full name of the given month.
     */
    private String getMonthName(Month month) {
        return month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }
}
