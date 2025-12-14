package nz.ac.canterbury.seng302.homehelper.exceptions.user;

public class UserNotActivatedException extends RuntimeException {

    public UserNotActivatedException(String message) {
        super(message);
    }
}
