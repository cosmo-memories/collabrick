package nz.ac.canterbury.seng302.homehelper.validation.user;

import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserValidation {
    /* Maximum allowed length for a first or last name. */
    public static final int NAME_MAX_LENGTH = 64;
    /* Name error messages as per the acceptance criteria */
    public static final String NAME_TOO_LONG = " name must be 64 characters long or less";
    public static final String NAME_EMPTY = " name cannot be empty";
    public static final String NAME_INVALID_CHARACTERS = " name must only include letters, spaces, hyphens, or apostrophes, and must contain at least one letter";
    /* Email and password error messages as per the acceptance criteria */
    public static final String EMAIL_INVALID_FORMAT = "Email address must be in the form 'jane@doe.nz'";
    public static final String EMAIL_IN_USE = "This email address is already in use";
    public static final String EMAIL_UNKNOWN_OR_PASSWORD_INVALID = "The email address is unknown, or the password is invalid";
    public static final String PASSWORD_LOW_STRENGTH = "Your password must be at least 8 characters long and include at " +
            "least one uppercase letter, one lowercase letter, one number, and one special character.";
    public static final String PASSWORD_RETYPE_NO_MATCH = "Passwords do not match";
    public static final String OLD_PASSWORD_INCORRECT = "Your old password is incorrect";
    /* User activation error. Not in AC but still an issue that needs correcting */
    public static final String USER_NOT_ACTIVATED = "User is not activated";
    /**
     * Modified from https://stackoverflow.com/a/201336
     */
    public static Pattern EMAIL_PATTERN = Pattern.compile("^\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.([A-Za-z]{2,})$");
    /**
     * Found using chat gpt
     * This regex accepts empty string because it needs to accept empty last names
     */
    public static Pattern NAME_PATTERN = Pattern.compile("^$|^[\\p{L}\\s'-]*\\p{L}[\\p{L}\\s'-]*$");
    Logger logger = LoggerFactory.getLogger(UserValidation.class);

    /**
     * Check validity of submitted name
     *
     * @param name     String to check
     * @param position Should read "First" or "Last" depending on what is being checked
     * @return Appropriate error string if checks fail, otherwise returns an empty string
     */
    public static String validateName(String name, String position) {
        // Check name length
        name = name.trim();
        if (name.length() > NAME_MAX_LENGTH) {
            return position + NAME_TOO_LONG;
        }
        if (name.isEmpty() && position.equals("First")) {
            return position + NAME_EMPTY;
        }
        // Check for invalid characters (anything not an alphabet character or whitespace)
        Matcher matcher = NAME_PATTERN.matcher(name);
        if (!matcher.matches()) {
            return position + NAME_INVALID_CHARACTERS;
        }
        // return empty error message if all checks pass
        return "";
    }

    /**
     * Validate email format (must be of form "name@domain.tld"); must not be an empty string
     *
     * @param email Email string to validate
     * @return Boolean result of evaluation
     */
    public static String validateEmailFormat(String email) {
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (!matcher.matches()) {
            return EMAIL_INVALID_FORMAT;
        }
        return "";
    }

    /**
     * Checks that a user with a given email is part of the database
     *
     * @param email email of user
     * @return bool stating if a user has been found or not
     */
    private static boolean checkUserExists(UserRepository userRepository, String email) {
        List<User> users = userRepository.findByEmail(email);
        return !users.isEmpty();
    }

    /**
     * Checks if account with given email exists in the database and if not returns the required error message
     *
     * @param userRepository UserRepository to access database
     * @param email          email address to check
     * @return an error message if the user doesn't exist, else returns and empty string
     */
    public static String checkLoginEmail(UserRepository userRepository, String email) {
        if (!checkUserExists(userRepository, email)) {
            return EMAIL_UNKNOWN_OR_PASSWORD_INVALID;
        }
        return "";
    }

    /**
     * Returns an error message if the email is already being used
     *
     * @param userRepository UserRepository to access database
     * @param email          email address to check
     * @return an error message if the email is already in use, else returns and empty string
     */
    public static String checkRegisterEmail(UserRepository userRepository, String email) {
        if (checkUserExists(userRepository, email)) {
            return EMAIL_IN_USE;
        } else {
            return "";
        }
    }

    /**
     * Check strength of user's password; must be 8+ characters, have at least one upper and one lowercase letter,
     * have at least one number, and at least one special character
     *
     * @param password Password to check
     * @return Boolean result of check
     */
    public static String validatePasswordStrength(String password) {
        // Check overall length
        int lowercase = 0;
        int uppercase = 0;
        int numbers = 0;
        int specials = 0;
        for (int i = 0; i < password.length(); i++) {
            // For each character, check:
            if (Character.isLowerCase(password.charAt(i))) {
                // If character is lowercase
                lowercase += 1;
            }
            if (Character.isUpperCase(password.charAt(i))) {
                // If character is uppercase
                uppercase += 1;
            }
            if (Character.isDigit(password.charAt(i))) {
                // If character is a number
                numbers += 1;
            }
            if (!Character.isLetterOrDigit(password.charAt(i))) {
                // If character is 'special' (NOT a letter or number)
                specials += 1;
            }
        }
        // Return true if ALL values are nonzero
        if (password.length() < 8 || lowercase == 0 || uppercase == 0 || numbers == 0 || specials == 0) {
            return PASSWORD_LOW_STRENGTH;
        }
        return "";
    }

    /**
     * Check user retyped their password correctly
     *
     * @param passwordA User's password
     * @param passwordB User's retyped password
     * @return Boolean result of check
     */
    public static String validatePasswordMatch(String passwordA, String passwordB) {
        if (!passwordA.equals(passwordB)) {
            return PASSWORD_RETYPE_NO_MATCH;
        }
        return "";
    }

    /**
     * Check if the user has been activated by verification code
     *
     * @param user user from database to check activated
     * @return String with error message if user is not activated, returns an empty string otherwise
     */
    public static String validateUserActivated(User user) {
        if (!user.isActivated()) {
            return USER_NOT_ACTIVATED;
        } else {
            return "";
        }
    }
}
