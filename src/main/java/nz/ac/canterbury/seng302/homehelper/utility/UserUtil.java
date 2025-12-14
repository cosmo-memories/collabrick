package nz.ac.canterbury.seng302.homehelper.utility;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UnauthenticatedException;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Utility class for user-related operations.
 */
public class UserUtil {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM, yyyy");

    /**
     * Retrieves the {@link User} object associated with the current HTTP request.
     *
     * @param userService The {@link UserService} used to fetch user details.
     * @param request     The {@link HttpServletRequest} containing the user principal.
     * @return The {@link User} object corresponding to the authenticated user.
     * @throws UnauthenticatedException if the user is unauthenticated.
     */
    public static User getUserFromHttpServletRequest(UserService userService, HttpServletRequest request) {
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null) {
            long id = Long.parseLong(userPrincipal.getName());
            return userService.findUserById(id);
        }
        throw new UnauthenticatedException();
    }

    /**
     * Retrieves the {@link User} object associated with the current HTTP request as an optional.
     *
     * @param userService The {@link UserService} used to fetch user details.
     * @param request     The {@link HttpServletRequest} containing the user principal.
     * @return The optional {@link User} object corresponding to the authenticated user, otherwise an empty optional.
     * @throws UnauthenticatedException if the user is unauthenticated.
     */
    public static Optional<User> getOptionalUserFromHttpServletRequest(UserService userService, HttpServletRequest request) {
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null) {
            long id = Long.parseLong(userPrincipal.getName());
            return Optional.ofNullable(userService.findUserById(id));
        }
        return Optional.empty();
    }

    /**
     * Formats a local date time in format 02 May, 2024
     *
     * @param date LocateDateTime to be formatted
     * @return String with the formatted date
     */
    public static String formatDate(LocalDateTime date) {
        return formatter.format(date);
    }

    /**
     * Formats a local date in format 02 May, 2024
     *
     * @param date LocateDateTime to be formatted
     * @return String with the formatted date
     */
    public static String formatDate(LocalDate date) {
        return formatter.format(date);
    }

    /**
     * Formats a local date in format 02 May, without a year
     *
     * @param date LocateDate to be formatted
     * @return String with the formatted date
     */
    public static String formatDateWithoutYear(LocalDate date) {
        DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("dd MMMM");
        return shortFormatter.format(date);
    }
}
