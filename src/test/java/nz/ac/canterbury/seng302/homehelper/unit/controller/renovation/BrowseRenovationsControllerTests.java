package nz.ac.canterbury.seng302.homehelper.unit.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.controller.renovation.BrowseRenovationsController;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.model.Pagination;
import nz.ac.canterbury.seng302.homehelper.service.CalendarService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationMemberService;
import nz.ac.canterbury.seng302.homehelper.service.PaginationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BrowseRenovationsControllerTests {
    @Mock
    private RenovationService renovationService;

    @Mock
    private PaginationService paginationService;

    @Mock
    private RenovationMemberService renovationMemberService;

    @Mock
    private CalendarService calendarService;

    @InjectMocks
    private BrowseRenovationsController controller;

    @Mock
    private Model model;


    @Mock
    private HttpServletRequest request;

    @Mock
    private Pagination<Renovation> pagination;

    @Mock
    private Function<Pageable, Page<Renovation>> pageSupplier;

    @SuppressWarnings("unchecked")
    @Test
    void testGetBrowseRenovations() {
        when(paginationService.paginate(
                eq(1),
                eq(null),
                (Function<Pageable, Page<Renovation>>) any(Function.class),
                eq(request))
        ).thenReturn(pagination);

        String result = controller.getBrowseRenovations(1, null, "", null, false, model, request);

        assertEquals("pages/renovation/browseRenovationsPage", result);
        verify(model).addAttribute("activeLink", "Browse Renovations");
    }
}
