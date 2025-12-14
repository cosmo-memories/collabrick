package nz.ac.canterbury.seng302.homehelper.validation.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;

import java.time.LocalDate;
import java.util.regex.Pattern;

import static nz.ac.canterbury.seng302.homehelper.validation.Validation.*;

/**
 * A utility class for validating renovation records and room names. This class provides methods to validate the
 * attributes of a renovation record, including renovation names, room names, and descriptions, based on defined
 * constraints.
 */
public class RenovationValidation {
    /* Renovation validation and messages as per acceptance criteria */
    public static final String RENOVATION_NAME_EMPTY_MESSAGE = "Renovation record name cannot be empty";
    public static final String RENOVATION_NAME_NOT_UNIQUE_MESSAGE = "Renovation record name is not unique";
    public static final String RENOVATION_NAME_INVALID_MESSAGE = "Renovation record name must only include letters, numbers, spaces, dots, hyphens, or apostrophes, and must contain at least one letter or number";
    /* Room validation messages as per acceptance criteria */
    public static final String ROOM_NAME_EMPTY_MESSAGE = "Renovation record room name cannot be empty";
    public static final String ROOM_NAME_INVALID_MESSAGE = "Renovation record room names must only include letters, numbers, spaces, dots, commas, hyphens, or apostrophes, and must contain at least one letter or number";
    /* Task validation messages as per acceptance criteria */
    public static final String TASK_NAME_INVALID_MESSAGE = "Task name must only include letters, numbers, spaces, dots, hyphens, or apostrophes, and must contain at least one letter or number";
    public static final String TASK_DESCRIPTION_EMPTY_MESSAGE = "Task description cannot be empty";
    public static final String TASK_DESCRIPTION_TOO_LONG_MESSAGE = "Task description must be 512 characters or less";
    public static final String TASK_DUE_DATE_IN_PAST_MESSAGE = "Due date must be in the future";
    public static final String TASK_DUE_DATE_INVALID = "Date is not in valid format, DD/MM/YYYY";
    /* Expense validation messages as per acceptance criteria */
    public static final String EXPENSE_NAME_EMPTY_MESSAGE = "Expense name cannot be empty";
    public static final String EXPENSE_NAME_INVALID_MESSAGE = "Expense name must only include letters, numbers, spaces, dots, hyphens, or apostrophes, and must contain at least one letter or number";
    public static final String EXPENSE_PRICE_INVALID_MESSAGE = "Price must be a positive number in the form '5.99'";
    public static final String EXPENSE_PRICE_EMPTY_MESSAGE = "Price cannot be empty";
    public static final String EXPENSE_PRICE_TOO_LARGE_MESSAGE = "Price must be less than $10,000,000.00";
    public static final String EXPENSE_DATE_FUTURE_MESSAGE = "Expense date must be in the past";
    public static final String EXPENSE_DATE_INVALID_MESSAGE = "Date is not in valid format, DD/MM/YYYY";
    /* Maximum allowed length for a renovation description. */
    public static int RENOVATION_DESCRIPTION_CHAR_LIMIT = 512;
    public static final String RENOVATION_DESCRIPTION_TOO_LONG_MESSAGE = "Renovation record description must be " + RENOVATION_DESCRIPTION_CHAR_LIMIT + " characters or less";
    public static int RENOVATION_TITLES_CHAR_LIMIT = 64;
    public static final String RENOVATION_NAME_TOO_LONG_MESSAGE = "Renovation record name must be " + RENOVATION_TITLES_CHAR_LIMIT + " characters or less";
    public static final String RENOVATION_ROOM_NAME_TOO_LONG_MESSAGE = "Renovation record room names must be " + RENOVATION_TITLES_CHAR_LIMIT + " characters or less";
    public static final String TASK_NAME_TOO_LONG_MESSAGE = "Task name must be " + RENOVATION_TITLES_CHAR_LIMIT + " characters or less";
    /* Room name validation (allows commas whereas general names do not) */
    public static final Pattern ROOM_NAME_VALIDATION_PATTERN = Pattern.compile("^(?=.*[\\p{L}\\p{N}])[\\p{L}\\p{N} .,'-]+$", Pattern.UNICODE_CHARACTER_CLASS);
    public static final String EXPENSE_NAME_TOO_LONG_MESSAGE = "Expense name must be " + RENOVATION_TITLES_CHAR_LIMIT + " characters or less";

    /**
     * Returns an error message if the room name is not valid.
     *
     * @param roomName name of the room
     */
    public static String validateRoomName(String roomName) {
        if (roomName.trim().isEmpty()) {
            return ROOM_NAME_EMPTY_MESSAGE;
        }
        if (!isRoomNameValid(roomName)) {
            return ROOM_NAME_INVALID_MESSAGE;
        }
        if (roomName.length() > RENOVATION_TITLES_CHAR_LIMIT) {
            return RENOVATION_ROOM_NAME_TOO_LONG_MESSAGE;
        }
        return "";
    }

    /**
     * Returns an error message if the renovation record description is not valid (too long)
     *
     * @param renovationDescription the renovation description to be validated
     * @return a string containing the error message
     */
    public static String validateRenovationDescription(String renovationDescription) {
        if (renovationDescription.length() > RENOVATION_DESCRIPTION_CHAR_LIMIT) {
            return RENOVATION_DESCRIPTION_TOO_LONG_MESSAGE;
        }
        return "";
    }

    /**
     * Returns an error message if the renovation record name is not valid (too long)
     *
     * @param renovationName the renovation name to be validated
     * @param renovationId   the id of the renovation to be validated so we can check if there is a renovation with the same name,
     *                       other than the renovation it is already associated with
     * @param user           the user who owns the renovation being checked
     * @return a string containing the error message
     */
    public static String validateRenovationName(RenovationRepository renovationRepository, String renovationName, long renovationId, User user) {
        if (renovationName == null || renovationName.trim().isEmpty()) {
            return RENOVATION_NAME_EMPTY_MESSAGE;
        } else if (!isNameValid(renovationName)) {
            return RENOVATION_NAME_INVALID_MESSAGE;
        } else if (!renovationRepository.findByNameAndUser(renovationName.trim().replaceAll("\s\s+", " "), user).stream()
                .filter(r -> r.getId() != renovationId).toList().isEmpty()) {
            return RENOVATION_NAME_NOT_UNIQUE_MESSAGE;
        } else if (renovationName.length() > RENOVATION_TITLES_CHAR_LIMIT) {
            return RENOVATION_NAME_TOO_LONG_MESSAGE;
        }
        return "";
    }

    /**
     * Returns an error message if the task name is not valid
     *
     * @param taskName the task name to be validated
     */
    public static String validateTaskName(String taskName) {
        if (taskName == null || taskName.trim().isEmpty() || !isNameValid(taskName)) {
            return TASK_NAME_INVALID_MESSAGE;
        }
        if (taskName.length() > RENOVATION_TITLES_CHAR_LIMIT) {
            return TASK_NAME_TOO_LONG_MESSAGE;
        }
        return "";
    }

    /**
     * Returns an error message if the task description is not valid
     *
     * @param taskDescription the task description to be validated
     */
    public static String validateTaskDescription(String taskDescription) {
        if (taskDescription == null || taskDescription.trim().isEmpty()) {
            return TASK_DESCRIPTION_EMPTY_MESSAGE;
        } else if (taskDescription.length() > RENOVATION_DESCRIPTION_CHAR_LIMIT) {
            return TASK_DESCRIPTION_TOO_LONG_MESSAGE;
        }
        return "";
    }

    /**
     * Returns an error message if the task due date is not valid
     *
     * @param taskDueDate the task due date to be validated
     */
    public static String validateTaskDueDate(String taskDueDate) {
        if (taskDueDate.equals("Date Invalid")) {
            return TASK_DUE_DATE_INVALID;
        }
        if (!isDateValid(taskDueDate)) {
            return TASK_DUE_DATE_INVALID;
        }
        LocalDate taskLocalDate = LocalDate.parse(taskDueDate);
        if (taskLocalDate.isBefore(LocalDate.now())) {
            return TASK_DUE_DATE_IN_PAST_MESSAGE;
        }
        return "";
    }

    /**
     * Checks the validity of the expenses name
     *
     * @param expenseName the name of the expense
     * @return a string of an error message if an error occurs, or an empty string if name is valid
     */
    public static String validateExpenseName(String expenseName) {
        if (expenseName == null || expenseName.trim().isEmpty()) {
            return EXPENSE_NAME_EMPTY_MESSAGE;
        }
        if (!isNameValid(expenseName)) {
            return EXPENSE_NAME_INVALID_MESSAGE;
        }
        if (expenseName.length() > RENOVATION_TITLES_CHAR_LIMIT) {
            return EXPENSE_NAME_TOO_LONG_MESSAGE;
        }
        return "";
    }

    /**
     * Checks the validity of the expenses price
     *
     * @param expensePrice the price of the expense
     * @return a string of an error message if an error occurs, or an empty string if price is valid
     */
    public static String validateExpensePrice(String expensePrice) {
        if (expensePrice == null || expensePrice.trim().isEmpty()) {
            return EXPENSE_PRICE_EMPTY_MESSAGE;
        }
        if (!isPriceValid(expensePrice)) {
            return EXPENSE_PRICE_INVALID_MESSAGE;
        }
        if (Double.parseDouble(expensePrice) < 0) {
            return EXPENSE_PRICE_INVALID_MESSAGE;
        }
        if (Double.parseDouble(expensePrice) >= 10000000) {
            return EXPENSE_PRICE_TOO_LARGE_MESSAGE;
        }
        return "";
    }

    /**
     * Checks the validity of the expense date
     *
     * @param expenseDate the date of the expense
     * @return a string of an error message if an error occurs, or an empty string if date is valid
     */
    public static String validateExpenseDate(String expenseDate) {
        if (!isDateValid(expenseDate)) {
            return EXPENSE_DATE_INVALID_MESSAGE;
        }
        LocalDate taskLocalDate = LocalDate.parse(expenseDate);
        if (taskLocalDate.isAfter(LocalDate.now())) {
            return EXPENSE_DATE_FUTURE_MESSAGE;
        }
        return "";
    }

    private static boolean isRoomNameValid(String roomName) {
        return ROOM_NAME_VALIDATION_PATTERN.matcher(roomName).matches();
    }

}
