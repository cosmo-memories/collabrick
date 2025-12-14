package nz.ac.canterbury.seng302.homehelper.exceptions;

/**
 * Exception for Location validation errors.
 */
public class LocationException extends IllegalArgumentException {

    private String streetError;
    private String suburbError;
    private String cityError;
    private String postcodeError;
    private String countryError;
    private String fieldError;
    private String regionError;

    public LocationException() {
        this.streetError = "";
        this.suburbError = "";
        this.cityError = "";
        this.postcodeError = "";
        this.countryError = "";
        this.fieldError = "";
        this.regionError = "";
    }

    public String getStreetError() {
        return streetError;
    }

    public void setStreetError(String streetError) {
        this.streetError = streetError;
    }

    public String getSuburbError() {
        return suburbError;
    }

    public void setSuburbError(String suburbError) {
        this.suburbError = suburbError;
    }

    public String getCityError() {
        return cityError;
    }

    public void setCityError(String cityError) {
        this.cityError = cityError;
    }

    public String getPostcodeError() {
        return postcodeError;
    }

    public void setPostcodeError(String postcodeError) {
        this.postcodeError = postcodeError;
    }

    public String getCountryError() {
        return countryError;
    }

    public void setCountryError(String countryError) {
        this.countryError = countryError;
    }

    public String getFieldError() {
        return fieldError;
    }

    public void setFieldError(String fieldError) {
        this.fieldError = fieldError;
    }

    public String getRegionError() {
        return regionError;
    }

    public void setRegionError(String regionError) {
        this.regionError = regionError;
    }
}
