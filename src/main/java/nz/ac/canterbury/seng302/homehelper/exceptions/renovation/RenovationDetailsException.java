package nz.ac.canterbury.seng302.homehelper.exceptions.renovation;

import nz.ac.canterbury.seng302.homehelper.exceptions.LocationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception for errors in Renovation details. Extends LocationException.
 */
public class RenovationDetailsException extends LocationException {

    private final String nameErrorMessage;
    private final String descriptionErrorMessage;
    private final ArrayList<String> roomErrorMessages;

    public RenovationDetailsException(String nameErrorMessage, String descriptionErrorMessage, ArrayList<String> roomErrorMessages) {
        super();
        this.nameErrorMessage = nameErrorMessage;
        this.descriptionErrorMessage = descriptionErrorMessage;
        this.roomErrorMessages = roomErrorMessages;
    }

    /**
     * Gets the name of the error message
     *
     * @return the name of the error message
     */
    public String getNameErrorMessage() {
        return nameErrorMessage;
    }

    /**
     * Gets the description of the error message
     *
     * @return the description of the error message
     */
    public String getDescriptionErrorMessage() {
        return descriptionErrorMessage;
    }

    /**
     * Get room error messages.
     * @return      Room error messages.
     */
    public ArrayList<String> getRoomErrorMessages() { return roomErrorMessages; }

}
