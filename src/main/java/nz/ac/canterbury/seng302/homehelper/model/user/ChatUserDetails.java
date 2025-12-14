package nz.ac.canterbury.seng302.homehelper.model.user;


import nz.ac.canterbury.seng302.homehelper.entity.user.User;

/**
 * Represents the only details needed for a chat message to prevent revealing sensitive information
 */
public record ChatUserDetails(String firstName, String lastName, String image, Long id) {
    /**
     * Constructs a new ChatUserDetails from a User entity.
     *
     * @param user the user entity to extract the chat user details from
     */
    public ChatUserDetails(User user) {
        this(user.getFname(), user.getLname(), user.getImage(), user.getId());
    }

    /**
     * Constructs a new ChatUserDetails with the provided user information
     *
     * @param firstName the users first name
     * @param lastName  the users last name
     * @param image     the user profile image
     */
    public ChatUserDetails {
    }
}
