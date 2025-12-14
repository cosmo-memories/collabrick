package nz.ac.canterbury.seng302.homehelper.model.auth;

/**
 * Represents a user's password update request containing the old password, new password, and the retyped
 * new password. This class is used as a model attribute in Thymeleaf templates to bind to the form.
 */
public class UserPasswordUpdate {
    private String oldPassword;
    private String newPassword;
    private String retypedNewPassword;

    /**
     * Default constructor for UserPasswordUpdate.
     * Initializes an empty password update request.
     */
    public UserPasswordUpdate() {
    }

    /**
     * Constructs a new UserPasswordUpdate object with the given old password, new password,
     * and retyped new password.
     *
     * @param oldPassword        The current password of the user.
     * @param newPassword        The new password the user wants to set.
     * @param retypedNewPassword The retyped new password for confirmation.
     */
    public UserPasswordUpdate(String oldPassword, String newPassword, String retypedNewPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.retypedNewPassword = retypedNewPassword;
    }

    /**
     * Gets the current password of the user.
     *
     * @return the old password.
     */
    public String getOldPassword() {
        return oldPassword;
    }

    /**
     * Sets the current password of the user.
     *
     * @param oldPassword the old password.
     */
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    /**
     * Gets the new password that the user wants to set.
     *
     * @return the new password.
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * Sets the new password that the user wants to set.
     *
     * @param newPassword the new password.
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    /**
     * Gets the retyped new password to confirm the user's input.
     *
     * @return the retyped new password.
     */
    public String getRetypedNewPassword() {
        return retypedNewPassword;
    }

    /**
     * Sets the retyped new password to confirm the user's input.
     *
     * @param retypedNewPassword the retyped new password.
     */
    public void setRetypedNewPassword(String retypedNewPassword) {
        this.retypedNewPassword = retypedNewPassword;
    }

    @Override
    public String toString() {
        return "UserPasswordUpdate{" +
                "oldPassword='" + oldPassword + '\'' +
                ", newPassword='" + newPassword + '\'' +
                ", retypedNewPassword='" + retypedNewPassword + '\'' +
                '}';
    }
}
