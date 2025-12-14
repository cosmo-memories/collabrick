package nz.ac.canterbury.seng302.homehelper.model.renovation;

/**
 * Renovation data transfer object for sending renovation details to the client
 *
 * @param id renovation id
 * @param name renovation name
 */
public record RenovationDto(Long id, String name) {

}
