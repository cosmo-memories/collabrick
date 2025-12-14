package nz.ac.canterbury.seng302.homehelper.exceptions.auth;

public class UnauthenticatedException extends RuntimeException {
    public UnauthenticatedException() {
        super("Unauthenticated");
    }
}
