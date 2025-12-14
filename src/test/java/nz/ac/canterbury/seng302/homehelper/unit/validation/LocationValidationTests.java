package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.validation.LocationValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * Unit tests for LocationValidation class.
 */
public class LocationValidationTests {

    /**
     * Returns a stream of invalid empty inputs for testing.
     *
     * @return Stream of variously formatted "empty" inputs
     */
    static Stream<String> emptyInputs() {
        return Stream.of(
                "",
                null
        );
    }

    /**
     * Returns a stream of inputs longer than 64 characters, including one that is exactly 129.
     *
     * @return Stream of strings longer than maximum allowable length
     */
    static Stream<String> longInputs() {
        return Stream.of(
                "AaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        );
    }

    /**
     * Returns a stream of inputs less than 64 characters, including one that is exactly 128.
     *
     * @return Stream of strings less than or equal to maximum allowable length
     */
    static Stream<String> shortInputs() {
        return Stream.of(
                "AaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "Aaaaaaaaaaa",
                "A"
        );
    }


    /**
     * Returns a stream of valid street addresses for testing.
     *
     * @return Stream of valid street address strings
     */
    static Stream<String> validStreets() {
        return Stream.of(
                "123 Fake Street",
                "123 Fake St.",
                "69 Hyphenated-Road",
                "22B Forward/Slash Street",
                "A Street With No Numbers",
                "A Street With No Numbers 2",
                "1 José Lane",
                "2' Apostrophe Crescent",
                "'/-B2 Technically Valid Street",
                "NoSpaces",
                "42",
                "ā"
        );
    }

    /**
     * Returns a stream of invalid street addresses for testing.
     *
     * @return Stream of invalid street address strings
     */
    static Stream<String> invalidStreets() {
        return Stream.of(
                "---",
                "'-'-'-",
                "?!_&",
                "                     ",
                "123_Fake_Street",
                "!",
                "/",
                "22C Backslash\\Road",
                "^& Illegal Character Lane",
                "\"Quotation Alley\"",
                "{Bracket Land)",
                "\uD83E\uDD8E",
                "\uD83D\uDEAB Street",
                "😊",
                "Smile Station 😊"
        );
    }

    @ParameterizedTest
    @MethodSource("validStreets")
    public void validateStreet_ValidEntries_ReturnsEmptyString(String street) {
        Assertions.assertEquals("", LocationValidation.validateStreet(street));
    }

    @ParameterizedTest
    @MethodSource("invalidStreets")
    public void validateStreet_InvalidEntries_ReturnsInvalidCharacterError(String street) {
        Assertions.assertEquals(LocationValidation.STREET_INVALID_CHARACTERS_MESSAGE, LocationValidation.validateStreet(street));
    }

    @ParameterizedTest
    @MethodSource("emptyInputs")
    public void validateStreet_EmptyEntries_ReturnsEmptyStringError(String street) {
        Assertions.assertEquals(LocationValidation.STREET_EMPTY_MESSAGE, LocationValidation.validateStreet(street));
    }

    @ParameterizedTest
    @MethodSource("longInputs")
    public void validateStreet_LongEntries_ReturnsLengthError(String street) {
        Assertions.assertEquals(LocationValidation.STREET_LENGTH_EXCEEDED_MESSAGE, LocationValidation.validateStreet(street));
    }

    @ParameterizedTest
    @MethodSource("shortInputs")
    public void validateStreet_ShortEntries_ReturnsEmptyString(String street) {
        Assertions.assertEquals("", LocationValidation.validateStreet(street));
    }


    /**
     * Returns a stream of valid suburbs for testing.
     *
     * @return Stream of valid suburb strings
     */
    static Stream<String> validSuburbs() {
        return Stream.of(
                "OwO",
                "Hyphenated-Suburb",
                "Apostrophe'd Suburb",
                "District 9",
                "This-Is-A-Valid-Suburb-Somehow",
                "123ville",
                "",
                "42",
                "é",
                null
        );
    }

    /**
     * Returns a stream of invalid suburbs for testing.
     *
     * @return Stream of invalid suburb strings
     */
    static Stream<String> invalidSuburbs() {
        return Stream.of(
                "---",
                "Ampersand&Land",
                "!@#$%^",
                "No Dots.",
                "Forward/Slash",
                "é/Suburb",
                "_",
                "           ",
                "Dollar$",
                "\\Back\\Slash",
                "\\/\\/\\/\\/",
                "\uD83E\uDD8E",
                "\uD83D\uDEAB OwO",
                "😊",
                "Smile 😊"
        );
    }

    @ParameterizedTest
    @MethodSource("validSuburbs")
    public void validateSuburb_ValidEntries_ReturnsEmptyString(String suburb) {
        Assertions.assertEquals("", LocationValidation.validateSuburb(suburb));
    }

    @ParameterizedTest
    @MethodSource("invalidSuburbs")
    public void validateSuburb_InvalidEntries_ReturnsInvalidCharacterError(String suburb) {
        Assertions.assertEquals(LocationValidation.SUBURB_INVALID_CHARACTERS_MESSAGE, LocationValidation.validateSuburb(suburb));
    }

    @ParameterizedTest
    @MethodSource("emptyInputs")
    public void validateSuburb_EmptyEntries_ReturnsEmptyString(String suburb) {
        Assertions.assertEquals("", LocationValidation.validateSuburb(suburb));
    }

    @ParameterizedTest
    @MethodSource("longInputs")
    public void validateSuburb_LongEntries_ReturnsLengthError(String suburb) {
        Assertions.assertEquals(LocationValidation.SUBURB_LENGTH_EXCEEDED_MESSAGE, LocationValidation.validateSuburb(suburb));
    }

    @ParameterizedTest
    @MethodSource("shortInputs")
    public void validateSuburb_ShortEntries_ReturnsEmptyString(String suburb) {
        Assertions.assertEquals("", LocationValidation.validateSuburb(suburb));
    }


    /**
     * Returns a stream of valid cities for testing.
     *
     * @return Stream of valid city strings
     */
    static Stream<String> validCities() {
        return Stream.of(
                "Christchurch",
                "Hyphen-Apostrophe'",
                "Hyphenated-City",
                "Apostrophe'd City",
                "Los Angeles",
                "Los Angelés",
                "City With Many Spaces",
                "A",
                "ā"
        );
    }

    /**
     * Returns a stream of invalid cities for testing.
     *
     * @return Stream of invalid city strings
     */
    static Stream<String> invalidCities() {
        return Stream.of(
                "Not_Christchurch",
                "Los.Angeles",
                "Los_Angelés",
                "Los_āngeles",
                "Christchurch 2",
                "69",
                "Christ/Church",
                "Christ\\Church",
                "\\",
                "_",
                "😊",
                "Smileton 😊"
        );
    }

    @ParameterizedTest
    @MethodSource("validCities")
    public void validateCity_ValidEntries_ReturnsEmptyString(String city) {
        Assertions.assertEquals("", LocationValidation.validateCity(city));
    }

    @ParameterizedTest
    @MethodSource("invalidCities")
    public void validateCity_InvalidEntries_ReturnsInvalidCharacterError(String city) {
        Assertions.assertEquals(LocationValidation.CITY_INVALID_CHARACTERS_MESSAGE, LocationValidation.validateCity(city));
    }

    @ParameterizedTest
    @MethodSource("emptyInputs")
    public void validateCity_EmptyEntries_ReturnsEmptyString(String city) {
        Assertions.assertEquals(LocationValidation.CITY_EMPTY_MESSAGE, LocationValidation.validateCity(city));
    }

    @ParameterizedTest
    @MethodSource("longInputs")
    public void validateCity_LongEntries_ReturnsLengthError(String city) {
        Assertions.assertEquals(LocationValidation.CITY_LENGTH_EXCEEDED_MESSAGE, LocationValidation.validateCity(city));
    }

    @ParameterizedTest
    @MethodSource("shortInputs")
    public void validateCity_ShortEntries_ReturnsEmptyString(String city) {
        Assertions.assertEquals("", LocationValidation.validateCity(city));
    }

    /**
     * Returns a stream of valid regions for testing.
     *
     * @return Stream of valid region strings
     */
    static Stream<String> validRegions() {
        return Stream.of(
                "OwO",
                "Hyphenated-Region",
                "Apostrophe'd Region",
                "This-Is-A-Valid-Region-Somehow",
                "",
                "é",
                null
        );
    }

    /**
     * Returns a stream of invalid regions for testing.
     *
     * @return Stream of invalid region strings
     */
    static Stream<String> invalidRegions() {
        return Stream.of(
                "---",
                "Ampersand&Land",
                "!@#$%^",
                "No Dots.",
                "Forward/Slash",
                "é/Suburb",
                "_",
                "           ",
                "Dollar$",
                "\\Back\\Slash",
                "\\/\\/\\/\\/",
                "\uD83E\uDD8E",
                "\uD83D\uDEAB OwO",
                "😊",
                "Smile 😊"
        );
    }

    @ParameterizedTest
    @MethodSource("validRegions")
    public void validateRegion_ValidEntries_ReturnsEmptyString(String region) {
        Assertions.assertEquals("", LocationValidation.validateRegion(region));
    }

    @ParameterizedTest
    @MethodSource("invalidRegions")
    public void validateRegion_InvalidEntries_ReturnsInvalidCharacterError(String region) {
        Assertions.assertEquals(LocationValidation.REGION_INVALID_CHARACTERS_MESSAGE, LocationValidation.validateRegion(region));
    }

    @ParameterizedTest
    @MethodSource("emptyInputs")
    public void validateRegion_EmptyEntries_ReturnsEmptyString(String region) {
        Assertions.assertEquals("", LocationValidation.validateRegion(region));
    }

    @ParameterizedTest
    @MethodSource("longInputs")
    public void validateRegion_LongEntries_ReturnsLengthError(String region) {
        Assertions.assertEquals(LocationValidation.REGION_LENGTH_EXCEEDED_MESSAGE, LocationValidation.validateRegion(region));
    }

    @ParameterizedTest
    @MethodSource("shortInputs")
    public void validateRegion_ShortEntries_ReturnsEmptyString(String region) {
        Assertions.assertEquals("", LocationValidation.validateRegion(region));
    }


    /**
     * Returns a stream of valid postcodes for testing.
     *
     * @return Stream of valid postcode strings
     */
    static Stream<String> validPostcodes() {
        return Stream.of(
                "8022",
                "80 22",
                "ABCD",
                "A1 2B 3C",
                "2",
                "B",
                "é",
                "Postcodé",
                "Poāstcode"
        );
    }

    /**
     * Returns a stream of invalid postcodes for testing.
     *
     * @return Stream of invalid postcode strings
     */
    static Stream<String> invalidPostcodes() {
        return Stream.of(
                "Invalid_Postcode",
                "12.34",
                ".",
                "_",
                "😊",
                "Smile 😊",
                "Invālid&Postcode",
                "1234_",
                "-",
                "---",
                "Not-Allowéd",
                "AB\\CD",
                "AB/CD"
        );
    }

    @ParameterizedTest
    @MethodSource("validPostcodes")
    public void validatePostcode_ValidEntries_ReturnsEmptyString(String postcode) {
        Assertions.assertEquals("", LocationValidation.validatePostcode(postcode));
    }

    @ParameterizedTest
    @MethodSource("invalidPostcodes")
    public void validatePostcode_InvalidEntries_ReturnsInvalidCharacterError(String postcode) {
        Assertions.assertEquals(LocationValidation.POSTCODE_INVALID_CHARACTERS_MESSAGE, LocationValidation.validatePostcode(postcode));
    }

    @ParameterizedTest
    @MethodSource("emptyInputs")
    public void validatePostcode_EmptyEntries_ReturnsEmptyString(String postcode) {
        Assertions.assertEquals(LocationValidation.POSTCODE_EMPTY_MESSAGE, LocationValidation.validatePostcode(postcode));
    }

    @ParameterizedTest
    @MethodSource("longInputs")
    public void validatePostcode_LongEntries_ReturnsLengthError(String postcode) {
        Assertions.assertEquals(LocationValidation.POSTCODE_LENGTH_EXCEEDED_MESSAGE, LocationValidation.validatePostcode(postcode));
    }

    @ParameterizedTest
    @MethodSource("shortInputs")
    public void validatePostcode_ShortEntries_ReturnsEmptyString(String postcode) {
        Assertions.assertEquals("", LocationValidation.validatePostcode(postcode));
    }


    /**
     * Returns a stream of valid countries for testing.
     *
     * @return Stream of valid country strings
     */
    static Stream<String> validCountries() {
        return Stream.of(
                "New Zealand",
                "NewZealand",
                "AMERICA",
                "Eorzéā",
                "Hyphenated-Country",
                "Country With Many Spaces"
        );
    }

    /**
     * Returns a stream of invalid countries for testing.
     *
     * @return Stream of invalid country strings
     */
    static Stream<String> invalidCountries() {
        return Stream.of(
                "New_Zealand",
                "😊",
                "Smile 😊 Land",
                "Old.Zealand",
                "Old.Zéaland",
                "--",
                "-",
                "A___",
                "_",
                "New/Zeāland",
                "\\/\\/\\/\\/",
                "New\\Zealand",
                "\\",
                "/"
        );
    }

    @ParameterizedTest
    @MethodSource("validCountries")
    public void validateCountry_ValidEntries_ReturnsEmptyString(String country) {
        Assertions.assertEquals("", LocationValidation.validateCountry(country));
    }

    @ParameterizedTest
    @MethodSource("invalidCountries")
    public void validateCountry_InvalidEntries_ReturnsInvalidCharacterError(String country) {
        Assertions.assertEquals(LocationValidation.COUNTRY_INVALID_CHARACTERS_MESSAGE, LocationValidation.validateCountry(country));
    }

    @ParameterizedTest
    @MethodSource("emptyInputs")
    public void validateCountry_EmptyEntries_ReturnsEmptyString(String country) {
        Assertions.assertEquals(LocationValidation.COUNTRY_EMPTY_MESSAGE, LocationValidation.validateCountry(country));
    }

    @ParameterizedTest
    @MethodSource("longInputs")
    public void validateCountry_LongEntries_ReturnsLengthError(String country) {
        Assertions.assertEquals(LocationValidation.COUNTRY_LENGTH_EXCEEDED_MESSAGE, LocationValidation.validateCountry(country));
    }

    @ParameterizedTest
    @MethodSource("shortInputs")
    public void validateCountry_ShortEntries_ReturnsEmptyString(String country) {
        Assertions.assertEquals("", LocationValidation.validateCountry(country));
    }


    /**
     * Returns a stream of Location objects with all required fields filled.
     * Either all are empty, all are filled, or only suburb is unfilled.
     *
     * @return Stream of Location objects
     */
    static Stream<Location> validFields() {
        return Stream.of(
                new Location("123 Fake Street", "Old Zealand", "1234", "Auckland", "", ""),
                new Location("123 Fake Street", "Old Zealand", "1234", "Auckland", "Coolsville", "Somewhere")
        );
    }

    /**
     * Returns a stream of Location objects with invalid fields.
     * Suburb is provided while some other fields are not.
     *
     * @return Stream of Location objects
     */
    static Stream<Location> invalidFields() {
        return Stream.of(
                new Location("", "", "", "", "Coolsville", ""),
                new Location("", "", "", "", "", "Somewhere"),
                new Location("", "Old Zealand", "1234", "Auckland", "Coolsville", "Somewhere"),
                new Location("123 Fake Street", "", "1234", "Auckland", "Coolsville", "Somewhere"),
                new Location("123 Fake Street", "Old Zealand", "", "Auckland", "Coolsville", "Somewhere"),
                new Location("123 Fake Street", "Old Zealand", "1234", "", "Coolsville", "Somewhere"),
                new Location("", "Old Zealand", "1234", "Auckland", "", "Somewhere"),
                new Location("123 Fake Street", "", "1234", "Auckland", "", "Somewhere"),
                new Location("123 Fake Street", "Old Zealand", "", "Auckland", "", "Somewhere"),
                new Location("123 Fake Street", "Old Zealand", "1234", "", "", "Somewhere")
        );
    }

    @ParameterizedTest
    @MethodSource("validFields")
    public void validateFields_ValidEntries_ReturnsTrue(Location location) {
        Assertions.assertTrue(LocationValidation.allRequiredFields(location));
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    public void validateFields_InvalidEntries_ReturnsFalse(Location location) {
        Assertions.assertFalse(LocationValidation.allRequiredFields(location));
    }

    @Test
    public void validateFields_AllEmpty_ReturnsTrue() {
        Assertions.assertTrue(LocationValidation.allFieldsEmpty(new Location("", "", "", "", "", "")));
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    public void validateFields_NotEmpty_ReturnsFalse(Location location) {
        Assertions.assertFalse(LocationValidation.allFieldsEmpty(location));
    }
}
