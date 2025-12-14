package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.validation.renovation.RenovationValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

public class ExpenseValidationTests {

    /**
     * Empty values for testing
     *
     * @return a stream of empty values
     */
    static Stream<String> emptyInputs() {
        return Stream.of(
                "",
                null,
                "          "
        );
    }

    /**
     * A long input of exactly 65 characters
     *
     * @return a stream of long inputs
     */
    static Stream<String> longInput() {
        return Stream.of(
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        );
    }

    /**
     * Short inputs, including one with exactly 64 characters
     *
     * @return a stream of short inputs
     */
    static Stream<String> shortInputs() {
        return Stream.of(
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "a"
        );
    }

    /**
     * Names that are valid for an expense
     *
     * @return a stream of valid expense names
     */
    static Stream<String> validNameInputs() {
        return Stream.of(
                "Expense",
                "Expense1",
                "Expense ",
                "Expense.",
                "Expense-",
                "Expense'",
                "Éxpense"
        );
    }

    /**
     * Names that are invalid for an expense
     *
     * @return a stream of invalid expense names
     */
    static Stream<String> invalidNameInputs() {
        return Stream.of(
                "$$$",
                "%",
                "\uD83D\uDE0A"
        );
    }

    /**
     * Dates that are invalid for an expense
     *
     * @return a stream of invalid dates
     */
    static Stream<String> invalidDateInputs() {
        return Stream.of(
                "date",
                "2320-2335-234545",
                "3245-35",
                "12-9",
                "eberg-seven-eight",
                "2024-02-30"
        );
    }

    /**
     * Dates that are valid for an expense
     *
     * @return a stream of valid dates
     */
    static Stream<String> validDateInputs() {
        return Stream.of(
                "2024-09-12",
                "2025-01-17"
        );
    }

    @ParameterizedTest
    @MethodSource("emptyInputs")
    public void validateExpenseName_EmptyInput_ReturnsEmptyNameMessage(String expenseName) {
        Assertions.assertEquals("Expense name cannot be empty", RenovationValidation.validateExpenseName(expenseName));
    }

    @ParameterizedTest
    @MethodSource("longInput")
    public void validateExpenseName_LongInput_ReturnsLongNameMessage(String expenseName) {
        Assertions.assertEquals("Expense name must be 64 characters or less", RenovationValidation.validateExpenseName(expenseName));
    }

    @ParameterizedTest
    @MethodSource("shortInputs")
    public void validateExpenseName_ShortInput_ReturnsEmptyString(String expenseName) {
        Assertions.assertEquals("", RenovationValidation.validateExpenseName(expenseName));
    }

    @ParameterizedTest
    @MethodSource("validNameInputs")
    public void validateExpenseName_ValidNameInput_ReturnsEmptyString(String expenseName) {
        Assertions.assertEquals("", RenovationValidation.validateExpenseName(expenseName));
    }

    @ParameterizedTest
    @MethodSource("invalidNameInputs")
    public void validateExpenseName_InvalidNameInput_ReturnsInvalidNameMessage(String expenseName) {
        Assertions.assertEquals("Expense name must only include letters, numbers, spaces, dots, hyphens, or apostrophes, and must contain at least one letter or number", RenovationValidation.validateExpenseName(expenseName));
    }

    @ParameterizedTest
    @MethodSource("emptyInputs")
    public void validatePrice_EmptyInput_ReturnsEmptyPriceMessage(String price) {
        Assertions.assertEquals("Price cannot be empty", RenovationValidation.validateExpensePrice(price));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#invalidPriceInputs")
    public void validatePrice_InvalidPriceInput_ReturnsInvalidPriceMessage(String price) {
        Assertions.assertEquals("Price must be a positive number in the form '5.99'", RenovationValidation.validateExpensePrice(price));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#longInputs")
    public void validatePrice_LongPriceInput_ReturnsInvalidPriceMessage(String price) {
        Assertions.assertEquals("Price must be less than $10,000,000.00", RenovationValidation.validateExpensePrice(price));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#validPriceInputs")
    public void validatePrice_ValidPriceInput_ReturnsEmptyString(String price) {
        Assertions.assertEquals("", RenovationValidation.validateExpensePrice(price));
    }

    @ParameterizedTest
    @MethodSource("invalidDateInputs")
    public void validateDate_InvalidDateInput_ReturnsInvalidDateMessage(String date) {
        Assertions.assertEquals("Date is not in valid format, DD/MM/YYYY", RenovationValidation.validateExpenseDate(date));
    }

    @ParameterizedTest
    @MethodSource("validDateInputs")
    public void validateDate_ValidDateInput_ReturnsEmptyString(String date) {
        Assertions.assertEquals("", RenovationValidation.validateExpenseDate(date));
    }

    @Test
    public void validateExpenseDate_TodaysDate_ReturnsValidDateMessage() {
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = String.format("%d-%02d-%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        Assertions.assertEquals("", RenovationValidation.validateExpenseDate(formattedDate));
    }

}
