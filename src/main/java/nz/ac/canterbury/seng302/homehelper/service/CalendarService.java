package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.exceptions.CalendarException;
import nz.ac.canterbury.seng302.homehelper.model.calendar.Calendar;
import nz.ac.canterbury.seng302.homehelper.model.calendar.CalendarCell;
import nz.ac.canterbury.seng302.homehelper.model.calendar.CalendarItem;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static nz.ac.canterbury.seng302.homehelper.utility.NumberUtils.tryParseInt;

/**
 * Service responsible for generating a Calendar object for a specific month and year.
 */
@Service
public class CalendarService {

    private static final int MAXIMUM_YEAR = 2100;
    private static final int MINIMUM_YEAR = 1970;

    /**
     * Generates a calendar based on string representations of year and month, or a single date string.
     *
     * @param yearStr       the year as a string.
     * @param monthStr      the month ID as a string.
     * @param dateStr       a date string used to derive the year and month.
     * @param itemsSupplier a function that accepts the first and last day of the month, and returns a list of items
     *                      for that range.
     * @return a Calendar object containing the calendar grid, month names, year, and items.
     * @throws CalendarException if the given year and month are out of bounds.
     */
    public Calendar parseAndGenerateCalendar(String yearStr, String monthStr, String dateStr, BiFunction<LocalDate, LocalDate, List<CalendarItem>> itemsSupplier) {
        if ((monthStr == null || yearStr == null) && dateStr != null) {
            try {
                LocalDate date = LocalDate.parse(dateStr);
                int year = date.getYear();
                int month = date.getMonthValue();
                return generateCalendar(year, month, itemsSupplier);
            } catch (DateTimeException e) {
                throw new CalendarException(false, false, true);
            }
        }

        Integer year = tryParseInt(yearStr).orElse(null);
        Integer month = tryParseInt(monthStr).orElse(null);
        boolean yearInvalid = yearStr != null && year == null;
        boolean monthInvalid = monthStr != null && month == null;
        if (yearInvalid || monthInvalid) {
            throw new CalendarException(yearInvalid, monthInvalid, false);
        }
        return generateCalendar(year, month, itemsSupplier);
    }

    /**
     * Generates a Calendar object for the given year, month, and items. The calendar is structured as a list of
     * LocalDate cells, padding with nulls at the beginning and end to ensure each row has 7 items. Only items which
     * occur in the specified year and month will be added.
     *
     * @param year          the year for which to generate the calendar.
     * @param month         the month for which to generate the calendar.
     * @param itemsSupplier a function that accepts the first and last day of the month, and returns a list of items
     *                      for that range.
     * @return a Calendar object containing the calendar grid, month names, year, and items.
     * @throws CalendarException if the given year and month are out of bounds.
     */
    public Calendar generateCalendar(Integer year, Integer month, BiFunction<LocalDate, LocalDate, List<CalendarItem>> itemsSupplier) throws CalendarException {
        // set defaults if given year and month are null
        if (month == null)
            month = LocalDate.now().getMonthValue();
        if (year == null)
            year = LocalDate.now().getYear();

        LocalDate firstOfMonth = parseFirstOfMonth(year, month);
        LocalDate today = LocalDate.now();
        int daysOfMonth = firstOfMonth.lengthOfMonth();
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        LocalDate lastOfMonth = firstOfMonth.withDayOfMonth(daysOfMonth);

        List<CalendarItem> items = itemsSupplier.apply(firstOfMonth, lastOfMonth);
        List<CalendarCell> cells = new ArrayList<>();
        Map<LocalDate, List<CalendarItem>> itemsByDate = items.stream()
                .filter(item -> item.getDueDate() != null)
                .collect(Collectors.groupingBy(CalendarItem::getDueDate));

        // padding before the first day
        for (int i = 0; i < firstDayOfWeek; i++) {
            cells.add(null);
        }

        // days of the month
        for (int i = 1; i <= daysOfMonth; i++) {
            LocalDate date = LocalDate.of(year, month, i);
            cells.add(new CalendarCell(
                    date,
                    itemsByDate.getOrDefault(date, List.of()),
                    date.getMonth() == today.getMonth() && date.getYear() == today.getYear(),
                    date.equals(today)));
        }

        // padding after to complete the last week
        while (cells.size() % 7 != 0) {
            cells.add(null);
        }

        Month currentMonth = firstOfMonth.getMonth();
        Month previousMonth = firstOfMonth.minusMonths(1).getMonth();
        Month nextMonth = firstOfMonth.plusMonths(1).getMonth();

        return new Calendar(
                cells,
                currentMonth,
                previousMonth,
                nextMonth,
                year
        );
    }

    /**
     * Parses the first day of a calendar month using the provided year and month. Validates both the year and month
     * against defined boundaries before constructing a LocalDate.
     *
     * @param year  the year component of the date (1970 - 2100)
     * @param month the month component of the date (1–12)
     * @return a LocalDate representing the first day of the given month and year.
     * @throws CalendarException if the year or month is out of bounds
     */
    private LocalDate parseFirstOfMonth(int year, int month) throws CalendarException {
        boolean yearInvalid = year < MINIMUM_YEAR || year > MAXIMUM_YEAR;
        boolean monthInvalid = month < 1 || month > 12;
        if (yearInvalid || monthInvalid) {
            throw new CalendarException(yearInvalid, monthInvalid, false);
        }

        return LocalDate.of(year, month, 1);
    }
}

