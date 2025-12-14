package nz.ac.canterbury.seng302.homehelper.model;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

/**
 * Generic pagination help class that wraps a Page object from Spring Data and provides pagination utilities for
 * rendering the page navigation.
 *
 * @param <T> The type of items in the paginated page.
 */
public class Pagination<T> {

    /**
     * The number of pages to show before and after the current page in pagination.
     */
    public static final int DEFAULT_PAGES_AROUND_CURRENT = 2;

    /**
     * The minimum number of total pages required to display jump controls in the pagination UI.
     */
    public static final int MIN_TOTAL_PAGES_FOR_JUMP_CONTROLS = 11;

    /**
     * Default number of items to display per page.
     */
    public static final int DEFAULT_PAGE_SIZE = 3;

    private final Page<T> page;
    private final IntFunction<String> pageUrlSupplier;

    /**
     * Constructs a new Pagination instance that wraps a @link Page object
     * and provides a way to generate URLs for each page index.
     *
     * @param page            the Page object containing the paginated data and metadata.
     * @param pageUrlSupplier a function that takes a page number and returns the corresponding URL as a String.
     */
    public Pagination(Page<T> page, IntFunction<String> pageUrlSupplier) {
        this.page = page;
        this.pageUrlSupplier = pageUrlSupplier;
    }

    /**
     * Returns the current page number (1-based indexing).
     *
     * @return the current page number.
     */
    public int getCurrentPage() {
        return page.getNumber() + 1;
    }

    /**
     * Returns the total number of pages.
     *
     * @return the number of pages.
     */
    public int getTotalPages() {
        return Math.max(1, page.getTotalPages());
    }

    /**
     * Returns whether there are multiple pages.
     *
     * @return true if there are pages, false otherwise.
     */
    public boolean hasPages() {
        return getTotalPages() > 1;
    }

    /**
     * Indicates whether there is a next page.
     *
     * @return true if there is a next page, false otherwise.
     */
    public boolean hasNextPage() {
        return page.hasNext();
    }

    /**
     * Indicates whether there is a previous page.
     *
     * @return true if there is a previous page, false otherwise.
     */
    public boolean hasPreviousPage() {
        return page.hasPrevious();
    }

    /**
     * Indicates whether the pagination UI should show jump controls.
     *
     * @return true if jump buttons should be displayed, false otherwise.
     */
    public boolean showJumpPageButtons() {
        return getTotalPages() >= MIN_TOTAL_PAGES_FOR_JUMP_CONTROLS;
    }

    /**
     * Returns the total number of items available for pagination.
     *
     * @return the total number of items across all pages.
     */
    public long getTotalItems() {
        return page.getTotalElements();
    }

    /**
     * Returns the list of items on the current page.
     *
     * @return a list of items on the current page.
     */
    public List<T> getItems() {
        return page.getContent();
    }

    /**
     * Returns the URL for the first page.
     *
     * @return a URL string pointing to the first page.
     */
    public String getFirstPageUrl() {
        return pageUrlSupplier.apply(1);
    }

    /**
     * Returns the URL for the last page.
     *
     * @return a URL string pointing to the last page.
     */
    public String getLastPageUrl() {
        return pageUrlSupplier.apply(getTotalPages());
    }

    /**
     * Returns the URL for the next page relative to the current page.
     *
     * @return a URL string pointing to the next page.
     */
    public String getNextPageUrl() {
        return pageUrlSupplier.apply(getCurrentPage() + 1);
    }

    /**
     * Returns the URL for the previous page relative to the current page.
     *
     * @return a URL string pointing to the previous page.
     */
    public String getPreviousPageUrl() {
        return pageUrlSupplier.apply(getCurrentPage() - 1);
    }

    /**
     * Returns a list of PageLink objects representing visible page numbers (1-based indexing) and their URLs.
     * Shows up to 5 pages: the current page, previous two, and next two, clamped to the valid page range.
     *
     * @return a list of PageLink objects where each contains the page number and the corresponding URL
     */
    public List<PageLink> getVisiblePageLinks() {
        int minPage = Math.max(0, getCurrentPage() - DEFAULT_PAGES_AROUND_CURRENT - 1);
        int maxPage = Math.min(getTotalPages(), getCurrentPage() + DEFAULT_PAGES_AROUND_CURRENT);

        return IntStream.range(minPage, maxPage)
                .mapToObj(i ->
                        // create PageLink with 1-based index and URL
                        new PageLink(i + 1, pageUrlSupplier.apply(i + 1)))
                .toList();
    }

    /**
     * A record that represents a page in a paginated list, containing the page number and the corresponding URL for
     * that page.
     *
     * @param pageNumber The 1-based index of the page.
     * @param url        The URL that corresponds to the page.
     */
    public record PageLink(int pageNumber, String url) {
    }
}
