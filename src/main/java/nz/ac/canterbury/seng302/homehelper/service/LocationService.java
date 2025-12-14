package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.exceptions.LocationException;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.RenovationDetailsException;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UserDetailsInvalidException;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;

import static nz.ac.canterbury.seng302.homehelper.validation.LocationValidation.*;

/**
 * Location service class
 */
@Service
public class LocationService {

    /**
     * Constructor
     */
    public LocationService() {
    }

    /**
     * Run validation for user-submitted Renovation location details.
     * Throws RenovationDetailsException if any checks fail.
     *
     * @param location                User-submitted location data
     * @param nameErrorMessage        Renovation name error
     * @param descriptionErrorMessage Renovation description error
     * @throws RenovationDetailsException Error validating user data; contains relevant error messages
     */
    public void validateRenovationLocation(Location location, String nameErrorMessage, String descriptionErrorMessage, ArrayList<String> roomErrorMessages) throws RenovationDetailsException {

        RenovationDetailsException locationException = new RenovationDetailsException(nameErrorMessage, descriptionErrorMessage, roomErrorMessages);
        boolean valid = true;

        // Check if user actually submitted anything
        if (allFieldsEmpty(location)) {
            return;
        }

        // Check all required fields were provided
        if (!allRequiredFields(location)) {
            locationException.setFieldError("When providing an address, all fields except Suburb and Region are required.");
            valid = false;
        }

        // Check contents of each field:

        String streetResult = validateStreet(location.getStreetAddress());
        if (!streetResult.isEmpty()) {
            locationException.setStreetError(streetResult);
            valid = false;
        }

        String suburbResult = validateSuburb(location.getSuburb());
        if (!suburbResult.isEmpty()) {
            locationException.setSuburbError(suburbResult);
            valid = false;
        }

        String cityResult = validateCity(location.getCity());
        if (!cityResult.isEmpty()) {
            locationException.setCityError(cityResult);
            valid = false;
        }

        String regionResult = validateRegion(location.getRegion());
        if (!regionResult.isEmpty()) {
            locationException.setRegionError(regionResult);
            valid = false;
        }

        String postcodeResult = validatePostcode(location.getPostcode());
        if (!postcodeResult.isEmpty()) {
            locationException.setPostcodeError(postcodeResult);
            valid = false;
        }

        String countryResult = validateCountry(location.getCountry());
        if (!countryResult.isEmpty()) {
            locationException.setCountryError(countryResult);
            valid = false;
        }

        if (!valid) {
            // Problems validating user's data
            throw locationException;
        }

        // User's data was validated successfully
    }

    /**
     * Run validation for user-submitted User location details.
     * Throws UserDetailsInvalidException if any checks fail.
     *
     * @param location       User-submitted location data
     * @param firstNameError User first name error message
     * @param lastNameError  User last name error message
     * @param emailError     Use email error message
     * @param passwordError  User password error message
     * @throws UserDetailsInvalidException Error validating user data; contains relevant error messages
     */
    public void validateUserLocation(Location location, String firstNameError, String lastNameError, String emailError, String passwordError) throws UserDetailsInvalidException {

        UserDetailsInvalidException locationException = new UserDetailsInvalidException();
        locationException.setFirstNameError(firstNameError);
        locationException.setLastNameError(lastNameError);
        locationException.setEmailError(emailError);
        locationException.setPasswordError(passwordError);
        boolean valid = true;

        // Check if user actually submitted anything
        if (allFieldsEmpty(location)) {
            return;
        }

        // Check all required fields were provided
        if (!allRequiredFields(location)) {
            locationException.setFieldError("When providing an address, all fields except Suburb and Region are required.");
            valid = false;
        }

        // Check contents of each field:

        String streetResult = validateStreet(location.getStreetAddress());
        if (!streetResult.isEmpty()) {
            locationException.setStreetError(streetResult);
            valid = false;
        }

        String suburbResult = validateSuburb(location.getSuburb());
        if (!suburbResult.isEmpty()) {
            locationException.setSuburbError(suburbResult);
            valid = false;
        }

        String cityResult = validateCity(location.getCity());
        if (!cityResult.isEmpty()) {
            locationException.setCityError(cityResult);
            valid = false;
        }

        String regionResult = validateRegion(location.getRegion());
        if (!regionResult.isEmpty()) {
            locationException.setRegionError(regionResult);
            valid = false;
        }

        String postcodeResult = validatePostcode(location.getPostcode());
        if (!postcodeResult.isEmpty()) {
            locationException.setPostcodeError(postcodeResult);
            valid = false;
        }

        String countryResult = validateCountry(location.getCountry());
        if (!countryResult.isEmpty()) {
            locationException.setCountryError(countryResult);
            valid = false;
        }

        if (!valid) {
            // Problems validating user's data
            throw locationException;
        }

        // User's data was validated successfully

    }

    /**
     * Add error messages to model if there are problems with the create/edit Renovation forms.
     *
     * @param location Location object
     * @param model    Model object
     * @param e        RenovationDetailsException containing error messages
     */
    public void populateLocationErrors(@ModelAttribute("location") Location location, Model model, LocationException e) {
        model.addAttribute("location", location);
        if (!e.getStreetError().isEmpty()) {
            model.addAttribute("streetError", e.getStreetError());
        }
        if (!e.getSuburbError().isEmpty()) {
            model.addAttribute("suburbError", e.getSuburbError());
        }
        if (!e.getCityError().isEmpty()) {
            model.addAttribute("cityError", e.getCityError());
        }
        if (!e.getRegionError().isEmpty()) {
            model.addAttribute("regionError", e.getRegionError());
        }
        if (!e.getPostcodeError().isEmpty()) {
            model.addAttribute("postcodeError", e.getPostcodeError());
        }
        if (!e.getCountryError().isEmpty()) {
            model.addAttribute("countryError", e.getCountryError());
        }
        if (!e.getFieldError().isEmpty()) {
            model.addAttribute("fieldError", e.getFieldError());
        }
    }

}
