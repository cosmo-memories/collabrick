package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.BudgetException;
import nz.ac.canterbury.seng302.homehelper.model.renovation.BudgetDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static nz.ac.canterbury.seng302.homehelper.validation.renovation.RenovationBudgetValidation.validateBudgetField;
import static org.junit.jupiter.api.Assertions.*;

public class BudgetValidationTests {

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#invalidPriceInputs")
    void validateBudgetField_GivenInvalidMiscellaneousBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withMiscellaneousBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Miscellaneous budget must be a positive number in the form 5.99", errors.get("Miscellaneous"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#longInputs")
    void validateBudgetField_GivenLongMiscellaneousBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withMiscellaneousBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Miscellaneous budget must be less than $10,000,000", errors.get("Miscellaneous"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#validPriceInputs")
    void validateBudgetField_GivenValidMiscellaneousBudget_ShouldAccept(String price) {
        BudgetDto dto = new BudgetDto.Builder().withMiscellaneousBudget(price).build();
        assertDoesNotThrow(() -> validateBudgetField(dto));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#invalidPriceInputs")
    void validateBudgetField_GivenInvalidMaterialBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withMaterialBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Material budget must be a positive number in the form 5.99", errors.get("Material"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#longInputs")
    void validateBudgetField_GivenLongMaterialBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withMaterialBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Material budget must be less than $10,000,000", errors.get("Material"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#validPriceInputs")
    void validateBudgetField_GivenValidMaterialBudget_ShouldAccept(String price) {
        BudgetDto dto = new BudgetDto.Builder().withMaterialBudget(price).build();
        assertDoesNotThrow(() -> validateBudgetField(dto));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#invalidPriceInputs")
    void validateBudgetField_GivenInvalidLabourBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withLabourBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Labour budget must be a positive number in the form 5.99", errors.get("Labour"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#longInputs")
    void validateBudgetField_GivenLongLabourBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withLabourBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Labour budget must be less than $10,000,000", errors.get("Labour"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#validPriceInputs")
    void validateBudgetField_GivenValidLabourBudget_ShouldAccept(String price) {
        BudgetDto dto = new BudgetDto.Builder().withLabourBudget(price).build();
        assertDoesNotThrow(() -> validateBudgetField(dto));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#invalidPriceInputs")
    void validateBudgetField_GivenInvalidEquipmentBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withEquipmentBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Equipment budget must be a positive number in the form 5.99", errors.get("Equipment"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#longInputs")
    void validateBudgetField_GivenLongEquipmentBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withEquipmentBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Equipment budget must be less than $10,000,000", errors.get("Equipment"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#validPriceInputs")
    void validateBudgetField_GivenValidEquipmentBudget_ShouldAccept(String price) {
        BudgetDto dto = new BudgetDto.Builder().withEquipmentBudget(price).build();
        assertDoesNotThrow(() -> validateBudgetField(dto));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#invalidPriceInputs")
    void validateBudgetField_GivenInvalidProfessionalServiceBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withProfessionalServiceBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Professional Service budget must be a positive number in the form 5.99", errors.get("Professional Service"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#longInputs")
    void validateBudgetField_GivenLongProfessionalServiceBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withProfessionalServiceBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Professional Service budget must be less than $10,000,000", errors.get("Professional Service"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#validPriceInputs")
    void validateBudgetField_GivenValidProfessionalServiceBudget_ShouldAccept(String price) {
        BudgetDto dto = new BudgetDto.Builder().withProfessionalServiceBudget(price).build();
        assertDoesNotThrow(() -> validateBudgetField(dto));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#invalidPriceInputs")
    void validateBudgetField_GivenInvalidPermitBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withPermitBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Permit budget must be a positive number in the form 5.99", errors.get("Permit"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#longInputs")
    void validateBudgetField_GivenLongPermitBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withPermitBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Permit budget must be less than $10,000,000", errors.get("Permit"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#validPriceInputs")
    void validateBudgetField_GivenValidPermitBudget_ShouldAccept(String price) {
        BudgetDto dto = new BudgetDto.Builder().withPermitBudget(price).build();
        assertDoesNotThrow(() -> validateBudgetField(dto));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#invalidPriceInputs")
    void validateBudgetField_GivenInvalidCleanupBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withCleanupBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Cleanup budget must be a positive number in the form 5.99", errors.get("Cleanup"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#longInputs")
    void validateBudgetField_GivenLongCleanupBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withCleanupBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Cleanup budget must be less than $10,000,000", errors.get("Cleanup"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#validPriceInputs")
    void validateBudgetField_GivenValidCleanupBudget_ShouldAccept(String price) {
        BudgetDto dto = new BudgetDto.Builder().withCleanupBudget(price).build();
        assertDoesNotThrow(() -> validateBudgetField(dto));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#invalidPriceInputs")
    void validateBudgetField_GivenInvalidDeliveryBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withDeliveryBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Delivery budget must be a positive number in the form 5.99", errors.get("Delivery"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#longInputs")
    void validateBudgetField_GivenLongDeliveryBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withDeliveryBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("Delivery budget must be less than $10,000,000", errors.get("Delivery"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#validPriceInputs")
    void validateBudgetField_GivenValidDeliveryBudget_ShouldAccept(String price) {
        BudgetDto dto = new BudgetDto.Builder().withDeliveryBudget(price).build();
        assertDoesNotThrow(() -> validateBudgetField(dto));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#invalidPriceInputs")
    void validateMultipleFields_GivenInvalidBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withMiscellaneousBudget(price).withCleanupBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(2, errors.size());
        assertEquals("Miscellaneous budget must be a positive number in the form 5.99", errors.get("Miscellaneous"));
        assertEquals("Cleanup budget must be a positive number in the form 5.99", errors.get("Cleanup"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#longInputs")
    void validateMultipleFields_GivenLongBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withMiscellaneousBudget(price).withDeliveryBudget(price).build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(2, errors.size());
        assertEquals("Miscellaneous budget must be less than $10,000,000", errors.get("Miscellaneous"));
        assertEquals("Delivery budget must be less than $10,000,000", errors.get("Delivery"));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.PriceTestData#invalidPriceInputs")
    void validateMultipleFields_GivenInvalidLongBudget_ShouldThrowException(String price) {
        BudgetDto dto = new BudgetDto.Builder().withMiscellaneousBudget(price).withCleanupBudget("10000000000000000").build();
        BudgetException exception = assertThrows(BudgetException.class, () -> validateBudgetField(dto));
        Map<String, String> errors = exception.getFieldErrors();
        assertEquals(2, errors.size());
        assertEquals("Miscellaneous budget must be a positive number in the form 5.99", errors.get("Miscellaneous"));
        assertEquals("Cleanup budget must be less than $10,000,000", errors.get("Cleanup"));
    }

}
