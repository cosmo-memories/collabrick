package nz.ac.canterbury.seng302.homehelper.exceptions;

/**
 * Exception thrown when there is an issue with generating a calendar.
 */
public class CalendarException extends RuntimeException {
    private final boolean yearInvalid;
    private final boolean monthInvalid;
    private final boolean isDateInvalid;

    public CalendarException(boolean yearInvalid, boolean monthInvalid, boolean isDateInvalid) {
        this.yearInvalid = yearInvalid;
        this.monthInvalid = monthInvalid;
        this.isDateInvalid = isDateInvalid;
    }

    public boolean isYearInvalid() {
        return yearInvalid;
    }

    public boolean isMonthInvalid() {
        return monthInvalid;
    }

    public boolean isDateInvalid() {
        return isDateInvalid;
    }
}
