package nz.ac.canterbury.seng302.homehelper.validation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A utility class for validating names. This class provides a method to validate names based on the following
 * criteria: Must contain only Unicode letters, Unicode numbers, spaces, dots, commas, hyphens, and apostrophes.
 */
public class Validation {
    /*
     * \p{L} - matches any Unicode letter - supports all languages.
     * \p{N} - matches any Unicode number
     */
    private static final Pattern NAME_VALIDATION_PATTERN = Pattern.compile("^(?=.*[\\p{L}\\p{N}])[\\p{L}\\p{N} .'-]+$", Pattern.UNICODE_CHARACTER_CLASS);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final Pattern TAG_VALIDATION_PATTERN = Pattern.compile("^(?=.{1,32}$)(?=.*[A-Za-z]).*$");

    private static final Pattern PRICE_VALIDATION_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    /**
     * Validates a given name.
     *
     * @param name The name to validate.
     * @return true if the name is valid, false otherwise.
     */
    public static boolean isNameValid(String name) {
        return NAME_VALIDATION_PATTERN.matcher(name).matches();
    }

    /**
     * Validates a given date to have a year with only 4 digits
     *
     * @param date a string of the date to validate
     * @return true if the string was valid, false otherwise
     */
    public static boolean isDateValid(String date) {
        try {
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
            return localDate.getYear() < 10000 && localDate.getYear() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isTagValid(String tag) {
        return TAG_VALIDATION_PATTERN.matcher(tag).matches();
    }

    /**
     * Validates a price string is in the form '5.99' where there can be any amount of digits before the decimal point
     * and only 2 places after the decimal points, no decimal is also accepted
     *
     * @param price a string of the price
     * @return boolean of if the price matches the regex
     */
    public static boolean isPriceValid(String price) {
        return PRICE_VALIDATION_PATTERN.matcher(price).matches();
    }

    /**
     * Check if a user is a member of a renovation member list
     *
     * @param members members of a renovation
     * @param user    user to see if they're a member
     * @return true if member otherwise false
     */
    public static boolean isMember(List<RenovationMember> members, User user) {
        return members.stream()
                .map(RenovationMember::getUser)
                .filter(Objects::nonNull)
                .anyMatch(u -> u.getId().equals(user.getId()));
    }
}
