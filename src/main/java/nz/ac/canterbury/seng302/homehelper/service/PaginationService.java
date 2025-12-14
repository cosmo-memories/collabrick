package nz.ac.canterbury.seng302.homehelper.service;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.exceptions.PaginationException;
import nz.ac.canterbury.seng302.homehelper.model.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.function.Function;
import java.util.function.IntFunction;

import static nz.ac.canterbury.seng302.homehelper.model.Pagination.DEFAULT_PAGE_SIZE;

/**
 * Service responsible for handling pagination logic.
 */
@Service
public class PaginationService {
    public static final String PAGE_OUT_OF_BOUNDS_MESSAGE = "The page number is outside the range of available pages.";
    public static final String INVALID_PAGE_MESSAGE = "The page number is invalid.";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AppConfig appConfig;

    @Autowired
    public PaginationService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * Applies pagination to a data source and returns a Pagination object.
     *
     * @param pageNumber   the page number from the request query parameter (1-based).
     * @param gotoPage     the page number from user input, may be null.
     * @param pageSupplier a function that supplies a Page of data given a Pageable.
     * @param request      the current HTTP request, used to construct pagination URLs.
     * @param <T>          the type of elements in the paginated result.
     * @return a Pagination object containing the current page and URL metadata.
     * @throws PaginationException if the resolved page number is invalid or out of bounds.
     */
    public <T> Pagination<T> paginate(
            int pageNumber,
            String gotoPage,
            Function<Pageable, Page<T>> pageSupplier,
            HttpServletRequest request
    ) {
        Page<T> firstPage = pageSupplier.apply(PageRequest.of(0, DEFAULT_PAGE_SIZE));
        int totalPages = Math.max(1, firstPage.getTotalPages()); // when there are 0 pages, default to 1
        logger.info("Found {} total pages with {} total items available for pagination",
                totalPages,
                firstPage.getTotalElements());

        int resolvedPage = resolvePage(pageNumber, gotoPage, totalPages);
        logger.info("Resolved page {} to be used for pagination", resolvedPage);
        Pageable pageable = PageRequest.of(resolvedPage - 1, DEFAULT_PAGE_SIZE);
        Page<T> page = (resolvedPage == 1) ? firstPage : pageSupplier.apply(pageable);
        IntFunction<String> pageUrlSupplier = i -> appConfig
                .buildUriFromRequest(request)
                .replaceQueryParam("gotoPage")
                .replaceQueryParam("page", i)
                .build(true)
                .toUriString();
        return new Pagination<>(page, pageUrlSupplier);
    }

    /**
     * Resolves the target page number to use, based on input parameters.
     * Handles validation and throws exceptions for invalid or out-of-bounds pages.
     *
     * @param page       The page number from the request parameter.
     * @param gotoPage   The page number from user input or null.
     * @param totalPages The total number of pages available.
     * @return The resolved valid page number to use (1-based indexing).
     * @throws PaginationException if the page is out of range or gotoPage is invalid or out of range
     */

    private int resolvePage(int page, String gotoPage, int totalPages) {
        if (gotoPage != null) {
            try {
                BigInteger gotoPageInt = new BigInteger(gotoPage.trim());
                if (gotoPageInt.compareTo(BigInteger.ONE) < 0 || gotoPageInt.compareTo(BigInteger.valueOf(totalPages)) > 0) {
                    logger.info("The 'goToPage' parameter is out of bounds of available pages");
                    throw new PaginationException(PAGE_OUT_OF_BOUNDS_MESSAGE, true);
                }
                return gotoPageInt.intValue();
            } catch (NumberFormatException e) {
                logger.info("The 'goToPage' parameter is not numerical");
                throw new PaginationException(INVALID_PAGE_MESSAGE, true);
            } catch (ArithmeticException e) {
                logger.info("The 'goToPage' parameter is out of bounds of available pages");
                throw new PaginationException(PAGE_OUT_OF_BOUNDS_MESSAGE, true);
            }
        }
        if (page < 1 || page > totalPages) {
            logger.info("The 'page' parameter is out of bounds of available pages");
            throw new PaginationException(PAGE_OUT_OF_BOUNDS_MESSAGE, false);
        }
        return page;
    }
}
