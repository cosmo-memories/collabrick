package nz.ac.canterbury.seng302.homehelper.model.auth;

/**
 * Represents a user's password reset request containing the new password and the retyped new password. This class is
 * used as a model attribute in Thymeleaf templates to bind to the form.
 */

public class UserPasswordReset {
    private String newPassword;
    private String retypedNewPassword;

    /**
     * Gets the new password that the user wants to set.
     *
     * @return the new password
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * Sets the new password that the user wants to set.
     *
     * @param newPassword the new password
     */

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    /**
     * Gets the retyped new password to confirm the user's input.
     *
     * @return the retyped new password
     */

    public String getRetypedNewPassword() {
        return retypedNewPassword;
    }

    /**
     * Sets the retyped new password to confirm the user's input.
     *
     * @param retypedNewPassword the retyped new password
     */
    public void setRetypedNewPassword(String retypedNewPassword) {
        this.retypedNewPassword = retypedNewPassword;
    }
}
