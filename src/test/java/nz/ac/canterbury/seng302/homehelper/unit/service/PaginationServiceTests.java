package nz.ac.canterbury.seng302.homehelper.unit.service;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.exceptions.PaginationException;
import nz.ac.canterbury.seng302.homehelper.model.Pagination;
import nz.ac.canterbury.seng302.homehelper.service.PaginationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaginationServiceTests {

    @Mock
    private Function<Pageable, Page<String>> pageSupplier;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PaginationService paginationService;

    @Test
    void testPaginate_WhenPageIsValidAndFirstPageAndNoItemsOnPage_ThenReturnPagination() {
        int itemsPerPage = 3;
        int page = 1;
        String gotoPage = null;
        List<String> data = List.of();
        Page<String> firstPage = new PageImpl<>(data, Pageable.ofSize(itemsPerPage), 0);
        when(pageSupplier.apply(PageRequest.of(page - 1, itemsPerPage))).thenReturn(firstPage);

        Pagination<String> pagination = paginationService.paginate(page, gotoPage, pageSupplier, request);
        assertNotNull(pagination);
        assertTrue(pagination.getItems().isEmpty());
        assertEquals(1, pagination.getCurrentPage());
        assertFalse(pagination.hasNextPage());
    }

    @Test
    void testPaginate_WhenPageIsValidAndFirstPage_ThenReturnPagination() {
        int itemsPerPage = 3;
        int page = 1;
        String gotoPage = null;
        List<String> data = List.of("Item1", "Item2");
        Page<String> firstPage = new PageImpl<>(data, Pageable.ofSize(itemsPerPage), data.size());
        when(pageSupplier.apply(PageRequest.of(page - 1, itemsPerPage))).thenReturn(firstPage);

        Pagination<String> pagination = paginationService.paginate(page, gotoPage, pageSupplier, request);
        assertNotNull(pagination);
        assertEquals(data, pagination.getItems());
        assertEquals(1, pagination.getCurrentPage());
        assertEquals(1, pagination.getTotalPages());
        assertFalse(pagination.hasNextPage());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 2, 3, 10, 100})
    void testPaginate_WhenPageOutOfBounds_ThenThrowException(int page) {
        int itemsPerPage = 3;
        String gotoPage = null;
        Page<String> firstPage = new PageImpl<>(List.of("Item1", "Item2"), PageRequest.of(0, itemsPerPage), 2);
        when(pageSupplier.apply(PageRequest.of(0, itemsPerPage))).thenReturn(firstPage);

        PaginationException exception = assertThrows(PaginationException.class,
                () -> paginationService.paginate(page, gotoPage, pageSupplier, request));

        assertEquals(PaginationService.PAGE_OUT_OF_BOUNDS_MESSAGE, exception.getMessage());
        assertFalse(exception.isGotoPage());
    }

    /* goto page tests */

    @ParameterizedTest
    @ValueSource(strings = {"", "notanumbner", "nan", "$"})
    void testPaginate_WhenGotoPageIsInvalid_ThenThrowException(String gotoPage) {
        int itemsPerPage = 3;
        int page = 1;
        List<String> data = List.of("Item1", "Item2");
        Page<String> firstPage = new PageImpl<>(data, Pageable.ofSize(itemsPerPage), data.size());
        when(pageSupplier.apply(PageRequest.of(page - 1, itemsPerPage))).thenReturn(firstPage);

        PaginationException exception = assertThrows(PaginationException.class,
                () -> paginationService.paginate(page, gotoPage, pageSupplier, request));
        assertEquals(PaginationService.INVALID_PAGE_MESSAGE, exception.getMessage());
        assertTrue(exception.isGotoPage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "2", "25", "100", "9999999999999999999999999999999999999999999999999999999"})
    void testPaginate_WhenOnePageAndGotoPageIsOutOfBounds_ThenThrowException(String gotoPage) {
        int itemsPerPage = 3;
        int page = 1;
        List<String> data = List.of("Item1", "Item2");
        Page<String> firstPage = new PageImpl<>(data, Pageable.ofSize(itemsPerPage), data.size());
        when(pageSupplier.apply(PageRequest.of(page - 1, itemsPerPage))).thenReturn(firstPage);

        PaginationException exception = assertThrows(PaginationException.class,
                () -> paginationService.paginate(page, gotoPage, pageSupplier, request));
        assertEquals(PaginationService.PAGE_OUT_OF_BOUNDS_MESSAGE, exception.getMessage());
        assertTrue(exception.isGotoPage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "11", "25", "100", "9999999999999999999999999999999999999999999999999999999"})
    void testPaginate_WhenTenPagesAndGotoPageIsOutOfBounds_ThenThrowException(String gotoPage) {
        int itemsPerPage = 3;
        int page = 1;
        List<String> data = IntStream.range(0, 10 * itemsPerPage).mapToObj(i -> "Item " + i).toList();
        Page<String> firstPage = new PageImpl<>(data, Pageable.ofSize(itemsPerPage), data.size());
        when(pageSupplier.apply(PageRequest.of(page - 1, itemsPerPage))).thenReturn(firstPage);

        PaginationException exception = assertThrows(PaginationException.class,
                () -> paginationService.paginate(page, gotoPage, pageSupplier, request));
        assertEquals(PaginationService.PAGE_OUT_OF_BOUNDS_MESSAGE, exception.getMessage());
        assertTrue(exception.isGotoPage());
    }

    @Test
    void testPaginate_WhenTwoPagesAndGotoPageIsInBounds_ThenReturnCorrectPage() {
        int itemsPerPage = 3;
        int page = 1;
        String gotoPage = "2";
        List<String> firstPageData = List.of("Item1", "Item2", "Item3");
        List<String> secondPageData = List.of("Item4", "Item5", "Item6");
        Page<String> firstPage = new PageImpl<>(firstPageData, PageRequest.of(page - 1, itemsPerPage), 6);
        Page<String> secondPage = new PageImpl<>(secondPageData, PageRequest.of(page, itemsPerPage), 6);
        when(pageSupplier.apply(PageRequest.of(0, itemsPerPage))).thenReturn(firstPage);
        when(pageSupplier.apply(PageRequest.of(1, itemsPerPage))).thenReturn(secondPage);

        Pagination<String> pagination = paginationService.paginate(page, gotoPage, pageSupplier, request);

        assertEquals(2, pagination.getCurrentPage());
        assertEquals(secondPageData, pagination.getItems());
        assertEquals(2, pagination.getTotalPages());
        assertTrue(pagination.hasPreviousPage());
    }

    @Test
    void testPaginate_WithDefaultPageSize() {
        int page = 1;
        String gotoPage = null;
        List<String> data = List.of("Item1", "Item2");
        Page<String> firstPage = new PageImpl<>(data, Pageable.ofSize(Pagination.DEFAULT_PAGE_SIZE), data.size());
        when(pageSupplier.apply(PageRequest.of(0, Pagination.DEFAULT_PAGE_SIZE))).thenReturn(firstPage);

        Pagination<String> pagination = paginationService.paginate(page, gotoPage, pageSupplier, request);
        assertEquals(data, pagination.getItems());
    }
}
