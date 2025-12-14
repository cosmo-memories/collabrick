package nz.ac.canterbury.seng302.homehelper.exceptions;

/**
 * Exception thrown when there is an issue with pagination.
 */
public class PaginationException extends RuntimeException {
    private final boolean gotoPage;

    /**
     * Constructs a new PaginationException with the specified detail message
     * and an indicator for whether it was due to gotoPage input.
     *
     * @param message  the detail message
     * @param gotoPage true if the error resulted from gotoPage input, false otherwise.
     */
    public PaginationException(String message, boolean gotoPage) {
        super(message);
        this.gotoPage = gotoPage;
    }

    /**
     * Returns whether the error resulting from a gotoPage input.
     *
     * @return true if the error resulted from gotoPage input, false otherwise.
     */
    public boolean isGotoPage() {
        return gotoPage;
    }
}
