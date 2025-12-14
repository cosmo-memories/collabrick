package nz.ac.canterbury.seng302.homehelper.unit.entity;

import nz.ac.canterbury.seng302.homehelper.model.calendar.Calendar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalendarTests {
    @Test
    void nextMonth_GivenDecemberToJanuary_CorrectMonthAndIncrementsYear() {
        Month currentMonth = Month.DECEMBER;
        Month nextMonth = Month.JANUARY;
        Month previousMonth = Month.NOVEMBER;
        Calendar calendar = new Calendar(List.of(), currentMonth, previousMonth, nextMonth, 2024);
        assertEquals(nextMonth, calendar.nextMonth());
        assertEquals(nextMonth.getValue(), calendar.nextMonthId());
        assertEquals("January", calendar.nextMonthName());
        assertEquals(2025, calendar.nextMonthYear());
    }

    @ParameterizedTest
    @CsvSource({
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
    })
    void nextMonthLink_GivenJanuaryToNovember_CorrectMonthAndDoesNotChangeYear(
            Month previousMonth,
            Month currentMonth,
            Month nextMonth) {
        Calendar calendar = new Calendar(List.of(), currentMonth, previousMonth, nextMonth, 2024);
        assertEquals(nextMonth, calendar.nextMonth());
        assertEquals(nextMonth.getValue(), calendar.nextMonthId());
        assertEquals(nextMonth.getDisplayName(TextStyle.FULL, Locale.ENGLISH), calendar.nextMonthName());
        assertEquals(2024, calendar.nextMonthYear());
    }

    @Test
    void previousMonthLink_GivenJanuaryToDecember_CorrectMonthAndDecrementsYear() {
        Month currentMonth = Month.JANUARY;
        Month nextMonth = Month.FEBRUARY;
        Month previousMonth = Month.DECEMBER;
        Calendar calendar = new Calendar(List.of(), currentMonth, previousMonth, nextMonth, 2024);
        assertEquals(previousMonth, calendar.previousMonth());
        assertEquals(previousMonth.getValue(), calendar.previousMonthId());
        assertEquals("December", calendar.previousMonthName());
        assertEquals(2023, calendar.previousMonthYear());
    }

    @ParameterizedTest
    @CsvSource({
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
    void previousMonthLink_GivenFebruaryToDecember_CorrectMonthAndDoesNotChangeYear(
            Month previousMonth,
            Month currentMonth,
            Month nextMonth) {
        Calendar calendar = new Calendar(List.of(), currentMonth, previousMonth, nextMonth, 2024);
        assertEquals(previousMonth, calendar.previousMonth());
        assertEquals(previousMonth.getValue(), calendar.previousMonthId());
        assertEquals(previousMonth.getDisplayName(TextStyle.FULL, Locale.ENGLISH), calendar.previousMonthName());
        assertEquals(2024, calendar.previousMonthYear());
    }
}
