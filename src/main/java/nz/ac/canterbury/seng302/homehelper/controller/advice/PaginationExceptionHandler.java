package nz.ac.canterbury.seng302.homehelper.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.exceptions.PaginationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Global exception handler for pagination related errors.
 */
@ControllerAdvice
public class PaginationExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AppConfig appConfig;

    @Autowired
    public PaginationExceptionHandler(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * Handles a PaginationException by redirecting the user to a safe fallback page with
     * a default page number.
     * If the exception was triggered by a goto page input, the user is redirected back to the
     * page they were on, otherwise the user is redirected to the default page.
     *
     * @param exception          the pagination exception that was thrown.
     * @param request            the current HTTP request.
     * @param redirectAttributes attributes used to pass flash messages to the redirected view.
     * @return a redirect URL to a safe fallback page.
     */
    @ExceptionHandler(PaginationException.class)
    public String handlePaginationException(
            PaginationException exception,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        UriComponentsBuilder builder = appConfig
                .buildUriFromRequest(request)
                .replaceQueryParam("gotoPage");

        // If the error came from a "gotoPage" input, redirect back with an error message.
        if (exception.isGotoPage()) {
            redirectAttributes.addFlashAttribute("pageError", exception.getMessage());
            builder.replaceQueryParam("gotoPage");
        } else {
            // otherwise, redirect to a valid default page
            builder.replaceQueryParam("page");
        }

        String url = builder.toUriString();
        logger.info("Handling pagination exception by redirecting to {}", url);
        return "redirect:" + url;
    }
}
