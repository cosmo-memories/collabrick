package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.Embeddable;

/**
 * Location class to be stored and embedded in other classes.
 * Values are initialized as empty strings for thymeleaf injection.
 */
@Embeddable
public class Location {
    private String streetAddress = "";
    private String country = "";
    private String postcode = "";
    private String city = "";
    private String suburb = "";
    private String region = "";

    public Location() {
    }

    public Location(String streetAddress, String country, String postcode, String city, String suburb, String region) {
        this.streetAddress = streetAddress;
        this.country = country;
        this.postcode = postcode;
        this.city = city;
        this.suburb = suburb;
        this.region = region;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
