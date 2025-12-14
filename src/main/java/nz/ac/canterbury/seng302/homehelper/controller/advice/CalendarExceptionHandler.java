package nz.ac.canterbury.seng302.homehelper.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.exceptions.CalendarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Global exception handler for pagination related errors.
 */
@ControllerAdvice
public class CalendarExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AppConfig appConfig;

    @Autowired
    public CalendarExceptionHandler(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * Handles a PaginationException by redirecting the user to a safe fallback page with a default month id or year
     * depending on if the month and/or year is invalid.
     *
     * @param exception the calendar exception that was thrown.
     * @param request   the current HTTP request.
     * @return a redirect URL to a safe fallback page.
     */
    @ExceptionHandler(CalendarException.class)
    public String handleCalendarException(CalendarException exception, HttpServletRequest request) {
        UriComponentsBuilder builder = appConfig
                .buildUriFromRequest(request);

        if (exception.isYearInvalid()) {
            builder.replaceQueryParam("year");
        }
        if (exception.isMonthInvalid()) {
            builder.replaceQueryParam("month");
        }
        if (exception.isDateInvalid()) {
            builder.replaceQueryParam("dateStr");
        }

        String url = builder.toUriString();
        logger.info("Handling calendar exception by redirecting to [{}]. Invalid year: {}, Invalid month: {}",
                url, exception.isYearInvalid(), exception.isMonthInvalid());
        return "redirect:" + url;
    }
}
