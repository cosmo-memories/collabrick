package nz.ac.canterbury.seng302.homehelper.unit.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.controller.advice.CalendarExceptionHandler;
import nz.ac.canterbury.seng302.homehelper.exceptions.CalendarException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CalendarExceptionHandlerTests {

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private CalendarExceptionHandler calendarExceptionHandler;

    @Mock
    private HttpServletRequest request;

    @Test
    void testHandleCalendarException_GivenYearInvalid_ShouldRedirectWithYearParamRemoved() {
        CalendarException calendarException = new CalendarException(true, false, false);
        when(appConfig.buildUriFromRequest(request)).thenReturn(UriComponentsBuilder
                .fromUriString("https://home-helper.nz/calendar")
                .queryParam("year", 1000)
                .queryParam("month", 12));

        String redirect = calendarExceptionHandler.handleCalendarException(calendarException, request);
        assertEquals("redirect:https://home-helper.nz/calendar?month=12", redirect);
    }

    @Test
    void testHandleCalendarException_GivenMonthInvalid_ShouldRedirectWithYearParamRemoved() {
        CalendarException calendarException = new CalendarException(false, true, false);
        when(appConfig.buildUriFromRequest(request)).thenReturn(UriComponentsBuilder
                .fromUriString("https://home-helper.nz/calendar")
                .queryParam("year", 2025)
                .queryParam("month", 13));

        String redirect = calendarExceptionHandler.handleCalendarException(calendarException, request);
        assertEquals("redirect:https://home-helper.nz/calendar?year=2025", redirect);
    }

    @Test
    void testHandleCalendarException_GivenYearAndMonthInvalid_ShouldRedirectWithYearParamRemoved() {
        CalendarException calendarException = new CalendarException(true, true, false);
        when(appConfig.buildUriFromRequest(request)).thenReturn(UriComponentsBuilder
                .fromUriString("https://home-helper.nz/calendar")
                .queryParam("year", 1000)
                .queryParam("month", 13));

        String redirect = calendarExceptionHandler.handleCalendarException(calendarException, request);
        assertEquals("redirect:https://home-helper.nz/calendar", redirect);
    }

    @Test
    void testHandleCalendarException_GivenDateInvalid_ShouldRedirectWithDateParamRemoved() {
        CalendarException calendarException = new CalendarException(false, false, true);
        when(appConfig.buildUriFromRequest(request)).thenReturn(UriComponentsBuilder
                .fromUriString("https://home-helper.nz/calendar")
                .queryParam("dateStr", "2022-2022"));

        String redirect = calendarExceptionHandler.handleCalendarException(calendarException, request);
        assertEquals("redirect:https://home-helper.nz/calendar", redirect);
    }
}
