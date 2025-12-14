package nz.ac.canterbury.seng302.homehelper.exceptions.auth;

/**
 * Exception thrown when there are errors during a user's password update. This exception provides specific error
 * messages for the old password, new password, and new retyped password.
 */
public class UserUpdatePasswordException extends RuntimeException {
    private final String oldPasswordError;
    private final String newPasswordError;
    private final String retypedNewPasswordError;

    /**
     * Constructs a new UserUpdatePasswordException with the specified error messages
     * for the old password, new password, and retyped new password.
     *
     * @param oldPasswordError        The error message related to the old password.
     * @param newPasswordError        The error message related to the new password.
     * @param retypedNewPasswordError The error message related to the retyped new password.
     */
    public UserUpdatePasswordException(String oldPasswordError, String newPasswordError, String retypedNewPasswordError) {
        this.oldPasswordError = oldPasswordError;
        this.newPasswordError = newPasswordError;
        this.retypedNewPasswordError = retypedNewPasswordError;
    }

    /**
     * Creates a UserUpdatePasswordException instance for an old password error.
     *
     * @param oldPasswordError The error message for the old password.
     * @return A new UserUpdatePasswordException instance with the old password error.
     */
    public static UserUpdatePasswordException createOldPasswordError(String oldPasswordError) {
        return new UserUpdatePasswordException(oldPasswordError, null, null);
    }

    /**
     * Creates a UserUpdatePasswordException instance for a new password error.
     *
     * @param newPasswordError The error message for the new password.
     * @return A new UserUpdatePasswordException instance with the new password error.
     */
    public static UserUpdatePasswordException createNewPasswordError(String newPasswordError) {
        return new UserUpdatePasswordException(null, newPasswordError, null);
    }

    /**
     * Creates a UserUpdatePasswordException instance for a retyped new password error.
     *
     * @param retypedNewPasswordError The error message for the retyped new password.
     * @return A new UserUpdatePasswordException instance with the retyped new password error.
     */
    public static UserUpdatePasswordException createRetypedNewPasswordError(String retypedNewPasswordError) {
        return new UserUpdatePasswordException(null, null, retypedNewPasswordError);
    }

    /**
     * Creates a UserUpdatePasswordException instance for a new password and a retyped new password
     *
     * @param newPasswordError        The error message for the new password.
     * @param retypedNewPasswordError The error message for the retyped new password.
     * @return A new UserUpdatePasswordException instance with the new password error and the retyped new password error.
     */
    public static UserUpdatePasswordException createNewPasswordAndRetypedPasswordError(String newPasswordError, String retypedNewPasswordError) {
        return new UserUpdatePasswordException(null, newPasswordError, retypedNewPasswordError);
    }

    /**
     * Returns the error message for the old password.
     *
     * @return The error message related to the old password.
     */
    public String getOldPasswordError() {
        return oldPasswordError;
    }

    /**
     * Returns the error message for the new password.
     *
     * @return The error message related to the new password.
     */
    public String getNewPasswordError() {
        return newPasswordError;
    }

    /**
     * Returns the error message for the retyped new password.
     *
     * @return The error message related to the retyped new password.
     */
    public String getRetypedNewPasswordError() {
        return retypedNewPasswordError;
    }
}
