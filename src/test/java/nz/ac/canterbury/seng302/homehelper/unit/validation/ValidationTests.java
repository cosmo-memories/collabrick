package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidationTests {
    @Test
    void testValidNameWithLettersAndSpaces() {
        assertTrue(Validation.isNameValid("John Doe"));
    }

    @Test
    void testValidNameWithNumbers() {
        assertTrue(Validation.isNameValid("Room 101"));
    }

    @Test
    void testValidNameWithAllowedPunctuation() {
        assertTrue(Validation.isNameValid("John Doe's Room-2"));
    }

    @Test
    void testValidNameWithPunctuationFollowedByCharacters() {
        assertTrue(Validation.isNameValid(".James' Room"));
    }

    @Test
    void testValidNameWithAllAllowedPunctuation() {
        assertFalse(Validation.isNameValid(". ,-'"));
    }

    @Test
    void testInvalidNameWithSpecialCharacters() {
        assertFalse(Validation.isNameValid("Invalid@Name!"));
    }

    @Test
    void testValidNameWithExcessiveSpaces() {
        assertTrue(Validation.isNameValid("John   Doe"));
    }

    @Test
    void testValidNameWithUnicodeCharacters() {
        assertTrue(Validation.isNameValid("Éléonore Müller"));
    }

    @Test
    void testValidNameWithMaoriCharacters() {
        assertTrue(Validation.isNameValid("Whānau"));
    }
}
