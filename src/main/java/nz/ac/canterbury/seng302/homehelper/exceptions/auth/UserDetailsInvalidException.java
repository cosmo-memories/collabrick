package nz.ac.canterbury.seng302.homehelper.exceptions.auth;

import nz.ac.canterbury.seng302.homehelper.exceptions.LocationException;

/**
 * Exception to be thrown when user details are invalid on registering, signing in or updating
 * user details. Extends LocationException.
 */
public class UserDetailsInvalidException extends LocationException {

    private String firstNameError;
    private String lastNameError;
    private String emailError;
    private String passwordError;
    private String credentialsError;

    public String getFirstNameError() {
        return firstNameError;
    }

    public void setFirstNameError(String firstNameError) {
        this.firstNameError = firstNameError;
    }

    public String getLastNameError() {
        return lastNameError;
    }

    public void setLastNameError(String lastNameError) {
        this.lastNameError = lastNameError;
    }

    public String getEmailError() {
        return emailError;
    }

    public void setEmailError(String emailError) {
        this.emailError = emailError;
    }

    public String getPasswordError() {
        return passwordError;
    }

    public void setPasswordError(String passwordError) {
        this.passwordError = passwordError;
    }

    public String getCredentialsError() {
        return credentialsError;
    }

    public void setCredentialsError(String credentialsError) {
        this.credentialsError = credentialsError;
    }
}
