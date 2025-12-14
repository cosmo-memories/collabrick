package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.exceptions.CalendarException;
import nz.ac.canterbury.seng302.homehelper.model.calendar.Calendar;
import nz.ac.canterbury.seng302.homehelper.model.calendar.CalendarCell;
import nz.ac.canterbury.seng302.homehelper.model.calendar.CalendarItem;
import nz.ac.canterbury.seng302.homehelper.service.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CalendarServiceTests {

    private CalendarService calendarService;

    @BeforeEach
    void setUp() {
        calendarService = new CalendarService();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "  ",
            "nan",
            "-10",
            "0",
            "1969",
            "2101",
            "100000",
            "2147483648000",   // greater than Integer.MAX_VALUE
            "-2147483648000"   // less than Integer.MIN_VALUE
    })
    void parseAndGenerateCalendar_GivenInvalidYearString_ThrowCalendarExceptionWithYearInvalidTrue(String yearStr) {
        CalendarException exception = assertThrows(CalendarException.class, () ->
                calendarService.parseAndGenerateCalendar(
                        yearStr,
                        null,
                        null,
                        (start, end) -> List.of()));
        assertTrue(exception.isYearInvalid());
        assertFalse(exception.isMonthInvalid());
        assertFalse(exception.isDateInvalid());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "  ",
            "nan",
            "-1",
            "0",
            "13",
            "20",
            "2147483648000",   // greater than Integer.MAX_VALUE
            "-2147483648000"   // less than Integer.MIN_VALUE
    })
    void parseAndGenerateCalendar_GivenInvalidMonthString_ThrowCalendarExceptionWithMonthInvalidTrue(String monthStr) {
        CalendarException exception = assertThrows(CalendarException.class, () ->
                calendarService.parseAndGenerateCalendar(
                        null,
                        monthStr,
                        null,
                        (start, end) -> List.of()));
        assertFalse(exception.isYearInvalid());
        assertTrue(exception.isMonthInvalid());
        assertFalse(exception.isDateInvalid());
    }

    @Test
    void parseAndGenerateCalendar_GivenInvalidMonthYearString_ThrowCalendarExceptionWithMonthYearInvalidTrue() {
        CalendarException exception = assertThrows(CalendarException.class, () ->
                calendarService.parseAndGenerateCalendar(
                        "-1",
                        "-1",
                        null,
                        (start, end) -> List.of()));
        assertTrue(exception.isYearInvalid());
        assertTrue(exception.isMonthInvalid());
        assertFalse(exception.isDateInvalid());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "2025-02-30",       // February 30 doesn't exist
            "2025-13-01",
            "2025-00-10",
            "2025-01-00",
            "2025-01-32",       // day out of range
            "abcd-ef-gh",
            "notadate"
    })
    void parseAndGenerateCalendar_GivenInvalidDateStr_ThrowsCalendarExceptionWithBothFlagsTrue(String dateStr) {
        CalendarException exception = assertThrows(CalendarException.class, () ->
                calendarService.parseAndGenerateCalendar(
                        null,
                        null,
                        dateStr,
                        (start, end) -> List.of()));
        assertFalse(exception.isYearInvalid());
        assertFalse(exception.isMonthInvalid());
        assertTrue(exception.isDateInvalid());
    }

    @Test
    void parseAndGenerateCalendar_GivenValidYearAndMonth_ReturnsCalendar() {
        Calendar calendar = calendarService.parseAndGenerateCalendar(
                "2025",
                "6",
                null,
                (start, end) -> List.of());
        assertEquals(2025, calendar.currentYear());
        assertEquals(Month.JUNE, calendar.currentMonth());
    }

    @Test
    void parseAndGenerateCalendar_GivenValidDate_ReturnsCalendar() {
        Calendar calendar = calendarService.parseAndGenerateCalendar(
                null,
                null,
                "2024-02-25",
                (start, end) -> List.of());
        assertEquals(2024, calendar.currentYear());
        assertEquals(Month.FEBRUARY, calendar.currentMonth());
    }

    @ParameterizedTest
    @CsvSource({
            "1, January, December, February",
            "2, February, January, March",
            "3, March, February, April",
            "4, April, March, May",
            "5, May, April, June",
            "6, June, May, July",
            "7, July, June, August",
            "8, August, July, September",
            "9, September, August, October",
            "10, October, September, November",
            "11, November, October, December",
            "12, December, November, January"
    })
    void generateCalendar_GivenMonth_ReturnsCorrectMonthNames(int month, String expectedCurrent, String expectedPrev, String expectedNext) {
        Calendar calendar = calendarService.generateCalendar(2025, month, (start, end) -> List.of());
        assertEquals(expectedPrev, calendar.previousMonthName());
        assertEquals(expectedCurrent, calendar.currentMonthName());
        assertEquals(expectedNext, calendar.nextMonthName());
    }

    @Test
    void generateCalendar_GivenLeapYearFebruary2024_ReturnsCorrectNumberOfDays() {
        Calendar calendar = calendarService.generateCalendar(2024, 2, (start, end) -> List.of());
        long numberOfDays = calendar.cells().stream()
                .filter(Objects::nonNull) // filter out empty days for start of calendar
                .count();
        assertEquals(29, numberOfDays);
    }

    @Test
    void generateCalendar_GivenNonLeapYearFebruary2025_ReturnsCorrectNumberOfDays() {
        Calendar calendar = calendarService.generateCalendar(2025, 2, (start, end) -> List.of());
        long numberOfDays = calendar.cells().stream()
                .filter(Objects::nonNull) // filter out empty days for start of calendar
                .count();
        assertEquals(28, numberOfDays);
    }

    @ParameterizedTest
    @CsvSource({
            "2025, 6",
            "2024, 2",
            "2025, 1",
            "2025, 12"
    })
    void generateCalendar_HasCompleteWeeks(int year, int month) {
        Calendar calendar = calendarService.generateCalendar(year, month, (start, end) -> List.of());
        assertEquals(0, calendar.cells().size() % 7);
    }

    @ParameterizedTest
    @CsvSource({
            "DECEMBER, JANUARY, FEBRUARY",
            "JANUARY, FEBRUARY, MARCH",
            "FEBRUARY, MARCH, APRIL",
            "MARCH, APRIL, MAY",
            "APRIL, MAY, JUNE",
            "MAY, JUNE, JULY",
            "JUNE, JULY, AUGUST",
            "JULY, AUGUST, SEPTEMBER",
            "AUGUST, SEPTEMBER, OCTOBER",
            "SEPTEMBER, OCTOBER, NOVEMBER",
            "OCTOBER, NOVEMBER, DECEMBER",
            "NOVEMBER, DECEMBER, JANUARY",
    })
    void generateCalendar_HasCorrectNextAndPreviousMonths(Month expectedPreviousMonth, Month currentMonth, Month expectedNextMonth) {
        Calendar calendar = calendarService.generateCalendar(2025, currentMonth.getValue(), (start, end) -> List.of());
        assertEquals(expectedPreviousMonth, calendar.previousMonth());
        assertEquals(expectedNextMonth, calendar.nextMonth());
    }

    @Test
    void generateCalendar_GivenEmptyItemsList_ReturnsCalendarWithNoItems() {
        Calendar calendar = calendarService.generateCalendar(2025, 6, (start, end) -> List.of());
        List<CalendarItem> allItems = calendar.cells().stream()
                .filter(Objects::nonNull)
                .map(CalendarCell::items)
                .flatMap(List::stream)
                .toList();
        assertTrue(allItems.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "2025, 6, 2025-06-01, 0",  // June 2025 starts on Sunday
            "2024, 2, 2024-02-01, 4",  // February 2024 starts on Thursday
            "2025, 1, 2025-01-01, 3",  // January 2025 starts on Wednesday
            "2025, 12, 2025-12-01, 1"  // December 2025 starts on Monday
    })
    void generateCalendar_GivenYearAndMonth_StartsOnCorrectDate(int year, int month, String expectedFirstDateStr, int expectedIndex) {
        LocalDate expectedFirstDate = LocalDate.parse(expectedFirstDateStr);
        Calendar calendar = calendarService.generateCalendar(year, month, (start, end) -> List.of());
        LocalDate dayOne = calendar.cells().get(expectedIndex).date();
        assertEquals(expectedFirstDate, dayOne);
    }

    @ParameterizedTest
    @CsvSource({
            "2025, 6, 2025-06-30, 29", // June 2025 ends on Monday - starts on sunday, index = (0 + 30 - 1)
            "2024, 2, 2024-02-29, 32", // February 2024 ends on Thursday - starts on thursday, index = (4 + 29 - 1)
            "2025, 1, 2025-01-31, 33", // January 2025 ends on Friday - starts on wednesday, index = (3 + 31 - 1)
            "2025, 12, 2025-12-31, 31" // December 2025 ends on Wednesday - starts on monday, index = (1 + 31 - 1)
    })
    void generateCalendar_GivenYearAndMonth_EndsOnCorrectDate(int year, int month, String expectedLastDateStr, int expectedIndex) {
        LocalDate expectedLastDate = LocalDate.parse(expectedLastDateStr);
        Calendar calendar = calendarService.generateCalendar(year, month, (start, end) -> List.of());
        LocalDate lastDay = calendar.cells().get(expectedIndex).date();
        assertEquals(expectedLastDate, lastDay);
    }

    @Test
    void generateCalendar_GivenMultipleItemsOnSameDate_ReturnsCalendarWithAllItems() {
        LocalDate date = LocalDate.of(2025, 6, 5);
        List<CalendarItem> items = List.of(
                new CalendarTaskImpl(date, "Task 1"),
                new CalendarTaskImpl(date, "Task 2"),
                new CalendarTaskImpl(date, "Task 3"));

        Calendar calendar = calendarService.generateCalendar(2025, 6, (start, end) -> items);
        CalendarCell dateCells = calendar.cells().stream()
                .filter(cell -> cell != null && cell.date().equals(date))
                .findFirst()
                .orElseThrow();
        List<CalendarItem> itemsAtDate = dateCells.items();

        assertEquals(3, itemsAtDate.size());
        assertEquals(List.of("Task 1", "Task 2", "Task 3"), itemsAtDate.stream()
                .map(CalendarItem::getName)
                .collect(Collectors.toList()));
    }

    @Test
    void generateCalendar_GivenMultipleItemsOnDifferentDates_ReturnsCalendarWithAllItems() {
        LocalDate date1 = LocalDate.of(2025, 6, 5);
        LocalDate date2 = LocalDate.of(2025, 6, 10);
        List<CalendarItem> items = List.of(
                new CalendarTaskImpl(date1, "Task 1"),
                new CalendarTaskImpl(date1, "Task 2"),
                new CalendarTaskImpl(date1, "Task 3"),
                new CalendarTaskImpl(date2, "Task 4"),
                new CalendarTaskImpl(date2, "Task 5"),
                new CalendarTaskImpl(date2, "Task 6"));

        Calendar calendar = calendarService.generateCalendar(2025, 6, (start, end) -> items);
        CalendarCell date1Cells = calendar.cells().stream()
                .filter(cell -> cell != null && cell.date().equals(date1))
                .findFirst()
                .orElseThrow();

        CalendarCell date2Cells = calendar.cells().stream()
                .filter(cell -> cell != null && cell.date().equals(date2))
                .findFirst()
                .orElseThrow();

        List<CalendarItem> itemsAtDate1 = date1Cells.items();
        assertEquals(3, itemsAtDate1.size());
        assertEquals(List.of("Task 1", "Task 2", "Task 3"), itemsAtDate1.stream()
                .map(CalendarItem::getName)
                .collect(Collectors.toList()));

        List<CalendarItem> itemsAtDate2 = date2Cells.items();
        assertEquals(3, itemsAtDate2.size());
        assertEquals(List.of("Task 4", "Task 5", "Task 6"), itemsAtDate2.stream()
                .map(CalendarItem::getName)
                .collect(Collectors.toList()));
    }

    @Test
    void generateCalendar_GivenItemWithNoDate_ReturnsCalendarWithNoItems() {
        List<CalendarItem> items = List.of(new CalendarTaskImpl(null, "Task 1"));
        Calendar calendar = calendarService.generateCalendar(2025, 6, (start, end) -> items);

        List<CalendarItem> allItems = calendar.cells().stream()
                .filter(Objects::nonNull)
                .map(CalendarCell::items)
                .flatMap(List::stream)
                .toList();
        assertTrue(allItems.isEmpty());
    }

    @Test
    void generateCalendar_GivenItemWithMonthDifferentThanCalendarCurrentMonth_ReturnsCalendarWithNoItems() {
        List<CalendarItem> items = List.of(
                new CalendarTaskImpl(LocalDate.of(2025, 4, 1), "Task 1"));
        Calendar calendar = calendarService.generateCalendar(2025, 6, (start, end) -> items);

        List<CalendarItem> allItems = calendar.cells().stream()
                .filter(Objects::nonNull)
                .map(CalendarCell::items)
                .flatMap(List::stream)
                .toList();
        assertTrue(allItems.isEmpty());
    }

    @Test
    void generateCalendar_GivenItemOutsideOfMonth_ExcludedFromCalendar() {
        List<CalendarItem> items = List.of(
                new CalendarTaskImpl(LocalDate.of(2025, 5, 31), "Too Early"),
                new CalendarTaskImpl(LocalDate.of(2025, 7, 1), "Too Late"));

        Calendar calendar = calendarService.generateCalendar(2025, 6, (start, end) -> items);
        List<String> allItemNames = calendar.cells().stream()
                .filter(Objects::nonNull)
                .flatMap(cell -> cell.items().stream())
                .map(CalendarItem::getName)
                .toList();

        assertFalse(allItemNames.contains("Too Early"));
        assertFalse(allItemNames.contains("Too Late"));
    }

    @Test
    void generateCalendar_GivenItemWithNullDate_IgnoresItem() {
        List<CalendarItem> items = List.of(
                new CalendarTaskImpl(null, "No Date"));

        Calendar calendar = calendarService.generateCalendar(2025, 6, (start, end) -> items);
        List<String> allItemNames = calendar.cells().stream()
                .filter(Objects::nonNull)
                .flatMap(cell -> cell.items().stream())
                .map(CalendarItem::getName)
                .toList();

        assertFalse(allItemNames.contains("No Date"));
    }

    @Test
    void generateCalendar_CellForToday_IsMarkedCorrectly() {
        LocalDate today = LocalDate.now();
        List<CalendarItem> items = List.of(new CalendarTaskImpl(today, "Today Task"));

        Calendar calendar = calendarService.generateCalendar(today.getYear(), today.getMonthValue(), (start, end) -> items);
        CalendarCell todayCell = calendar.cells().stream()
                .filter(cell -> cell != null && today.equals(cell.date()))
                .findFirst()
                .orElseThrow();

        assertTrue(todayCell.isCurrentDay());
        assertTrue(todayCell.isCurrentMonth());
    }

    // test implementation of a CalendarTask
    private record CalendarTaskImpl(LocalDate date, String name) implements CalendarItem {
        @Override
        public LocalDate getDueDate() {
            return date;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
