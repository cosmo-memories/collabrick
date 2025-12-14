package nz.ac.canterbury.seng302.homehelper.validation;

import nz.ac.canterbury.seng302.homehelper.entity.Location;

import java.util.regex.Pattern;

/**
 * Validation for location inputs as per U18 ACs 6-10.
 */
public class LocationValidation {

    // Enforce a maximum allowed length of inputs
    public static final int MAXIMUM_LENGTH = 128;
    public static final String STREET_LENGTH_EXCEEDED_MESSAGE = "Street address cannot be more than " + MAXIMUM_LENGTH + " characters.";
    public static final String SUBURB_LENGTH_EXCEEDED_MESSAGE = "Suburb cannot be more than " + MAXIMUM_LENGTH + " characters.";
    public static final String CITY_LENGTH_EXCEEDED_MESSAGE = "City cannot be more than " + MAXIMUM_LENGTH + " characters.";
    public static final String REGION_LENGTH_EXCEEDED_MESSAGE = "Region cannot be more than " + MAXIMUM_LENGTH + " characters.";
    public static final String POSTCODE_LENGTH_EXCEEDED_MESSAGE = "Postcode cannot be more than " + MAXIMUM_LENGTH + " characters.";
    public static final String COUNTRY_LENGTH_EXCEEDED_MESSAGE = "Country cannot be more than " + MAXIMUM_LENGTH + " characters.";

    // Error messages for invalid characters as specified in ACs 6-10 respectively
    public static final String STREET_INVALID_CHARACTERS_MESSAGE = "Street address contains invalid characters.";
    public static final String SUBURB_INVALID_CHARACTERS_MESSAGE = "Suburb contains invalid characters.";
    public static final String CITY_INVALID_CHARACTERS_MESSAGE = "City contains invalid characters.";
    public static final String REGION_INVALID_CHARACTERS_MESSAGE = "Region contains invalid characters.";
    public static final String POSTCODE_INVALID_CHARACTERS_MESSAGE = "Postcode contains invalid characters.";
    public static final String COUNTRY_INVALID_CHARACTERS_MESSAGE = "Country contains invalid characters.";

    // Error messages for missing fields; location is optional but if provided requires all mentioned fields
    public static final String STREET_EMPTY_MESSAGE = "Street address cannot be empty.";
    public static final String SUBURB_EMPTY_MESSAGE = "Suburb contains invalid characters.";    // Suburb is optional?
    public static final String CITY_EMPTY_MESSAGE = "City cannot be empty.";
    public static final String REGION_EMPTY_MESSAGE = "Region cannot be empty.";                  // Optional?
    public static final String POSTCODE_EMPTY_MESSAGE = "Postcode cannot be empty.";
    public static final String COUNTRY_EMPTY_MESSAGE = "Country cannot be empty.";

    // ChatGPT assisted in understanding/modifying regex and correcting my mistakes
    private static final Pattern STREET_VALIDATION_PATTERN = Pattern.compile("^(?=.*[\\p{L}\\p{N}])[\\p{L}\\p{N} .'/\\-]+$", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern SUBURB_VALIDATION_PATTERN = Pattern.compile("^(?=.*[\\p{L}\\p{N}])[\\p{L}\\p{N} '\\-]+$", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern CITY_VALIDATION_PATTERN = Pattern.compile("^(?=.*\\p{L})[\\p{L} '\\-]+$", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern REGION_VALIDATION_PATTERN = Pattern.compile("^(?=.*\\p{L})[\\p{L} '\\-]+$", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern POSTCODE_VALIDATION_PATTERN = Pattern.compile("^(?!.* {2})[\\p{L}\\p{N}]+( [\\p{L}\\p{N}]+)*$", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern COUNTRY_VALIDATION_PATTERN = Pattern.compile("^(?!.* {2})(?=.*\\p{L})[\\p{L}\\p{N}'\\-]+( [\\p{L}\\p{N}'\\-]+)*$", Pattern.UNICODE_CHARACTER_CLASS);

    /**
     * Validate street address input.
     *
     * @param street User's address input
     * @return Error string if input is invalid; empty string if valid
     */
    public static String validateStreet(String street) {
        if (street == null || street.isEmpty()) {
            return STREET_EMPTY_MESSAGE;
        }
        if (street.length() > MAXIMUM_LENGTH) {
            return STREET_LENGTH_EXCEEDED_MESSAGE;
        }
        if (!STREET_VALIDATION_PATTERN.matcher(street).matches()) {
            return STREET_INVALID_CHARACTERS_MESSAGE;
        }
        // Return empty string if input is valid
        return "";
    }

    /**
     * Validate suburb input. May optionally be empty.
     *
     * @param suburb User's suburb input
     * @return Error string if input is invalid; empty string if valid
     */
    public static String validateSuburb(String suburb) {
        if (suburb == null || suburb.isEmpty()) {
            // May be empty
            return "";
        }
        if (suburb.length() > MAXIMUM_LENGTH) {
            return SUBURB_LENGTH_EXCEEDED_MESSAGE;
        }
        if (!SUBURB_VALIDATION_PATTERN.matcher(suburb).matches()) {
            return SUBURB_INVALID_CHARACTERS_MESSAGE;
        }
        // Return empty string if input is valid
        return "";
    }

    /**
     * Validate city input.
     *
     * @param city User's city input
     * @return Error string if input is invalid; empty string if valid
     */
    public static String validateCity(String city) {
        if (city == null || city.isEmpty()) {
            return CITY_EMPTY_MESSAGE;
        }
        if (city.length() > MAXIMUM_LENGTH) {
            return CITY_LENGTH_EXCEEDED_MESSAGE;
        }
        if (!CITY_VALIDATION_PATTERN.matcher(city).matches()) {
            return CITY_INVALID_CHARACTERS_MESSAGE;
        }
        // Return empty string if input is valid
        return "";
    }

    /**
     * Validate region input.
     *
     * @param region User's region input
     * @return Error string if input is invalid; empty string if valid
     */
    public static String validateRegion(String region) {
        if (region == null || region.isEmpty()) {
            // May be empty
            return "";
        }
        if (region.length() > MAXIMUM_LENGTH) {
            return REGION_LENGTH_EXCEEDED_MESSAGE;
        }
        if (!REGION_VALIDATION_PATTERN.matcher(region).matches()) {
            return REGION_INVALID_CHARACTERS_MESSAGE;
        }
        // Return empty string if input is valid
        return "";
    }

    /**
     * Validate postcode input.
     *
     * @param postcode User's postcode input
     * @return Error string if input is invalid; empty string if valid
     */
    public static String validatePostcode(String postcode) {
        if (postcode == null || postcode.isEmpty()) {
            return POSTCODE_EMPTY_MESSAGE;
        }
        if (postcode.length() > MAXIMUM_LENGTH) {
            return POSTCODE_LENGTH_EXCEEDED_MESSAGE;
        }
        if (!POSTCODE_VALIDATION_PATTERN.matcher(postcode).matches()) {
            return POSTCODE_INVALID_CHARACTERS_MESSAGE;
        }
        // Return empty string if input is valid
        return "";
    }

    /**
     * Validate country input.
     *
     * @param country User's country input
     * @return Error string if input is invalid; empty string if valid
     */
    public static String validateCountry(String country) {
        if (country == null || country.isEmpty()) {
            return COUNTRY_EMPTY_MESSAGE;
        }
        if (country.length() > MAXIMUM_LENGTH) {
            return COUNTRY_LENGTH_EXCEEDED_MESSAGE;
        }
        if (!COUNTRY_VALIDATION_PATTERN.matcher(country).matches()) {
            return COUNTRY_INVALID_CHARACTERS_MESSAGE;
        }
        // Return empty string if input is valid
        return "";
    }

    /**
     * Checks no required fields are missing. Check this if the user included at least one nonempty field.
     *
     * @param location Location object to check
     * @return Boolean result of check
     */
    public static Boolean allRequiredFields(Location location) {
        return !location.getStreetAddress().isEmpty() && !location.getCity().isEmpty()
                && !location.getPostcode().isEmpty() && !location.getCountry().isEmpty();
    }

    /**
     * Checks if all fields in the Location are empty; if True, then user did not supply a Location.
     *
     * @param location Location object to check
     * @return Boolean result of check
     */
    public static Boolean allFieldsEmpty(Location location) {
        return location.getStreetAddress().isEmpty() && location.getSuburb().isEmpty()
                && location.getCity().isEmpty() && location.getPostcode().isEmpty()
                && location.getCountry().isEmpty() && location.getRegion().isEmpty();
    }
}
