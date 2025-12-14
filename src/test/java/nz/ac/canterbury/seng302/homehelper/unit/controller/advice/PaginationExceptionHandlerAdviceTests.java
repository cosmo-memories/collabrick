package nz.ac.canterbury.seng302.homehelper.unit.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.controller.advice.PaginationExceptionHandler;
import nz.ac.canterbury.seng302.homehelper.exceptions.PaginationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaginationExceptionHandlerAdviceTests {

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private PaginationExceptionHandler paginationExceptionHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Test
    void testHandlePaginationException_GivenGotoPageError_ShouldRedirectWithGotoPageParamRemovedAndError() {
        PaginationException exception = new PaginationException("Invalid page", true);
        when(appConfig.buildUriFromRequest(request)).thenReturn(UriComponentsBuilder
                .fromUriString("https://home-helper.nz/items")
                .query("page=1&gotoPage=3"));

        String redirect = paginationExceptionHandler.handlePaginationException(exception, request, redirectAttributes);
        assertEquals("redirect:https://home-helper.nz/items?page=1", redirect);
        verify(redirectAttributes).addFlashAttribute("pageError", "Invalid page");
    }

    @Test
    void testHandlePaginationException_GivenPageError_ShouldRedirectWithPageParamRemoved() {
        PaginationException exception = new PaginationException("Page out of bounds", false);
        when(appConfig.buildUriFromRequest(request)).thenReturn(UriComponentsBuilder
                .fromUriString("https://home-helper.nz/items")
                .query("page=99"));

        String redirect = paginationExceptionHandler.handlePaginationException(exception, request, redirectAttributes);
        assertEquals("redirect:https://home-helper.nz/items", redirect);
    }
}
