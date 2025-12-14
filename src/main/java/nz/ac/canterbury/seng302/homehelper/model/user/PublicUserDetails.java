package nz.ac.canterbury.seng302.homehelper.model.user;

import nz.ac.canterbury.seng302.homehelper.entity.user.User;

import java.util.Objects;

/**
 * Represents the public details of a user that can be shared or displayed without revealing
 * sensitive information.
 */
public class PublicUserDetails {
    private final long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String image;

    /**
     * Constructs a new PublicUserDetails from a User entity.
     *
     * @param user the user entity to extract public details from.
     */
    public PublicUserDetails(User user) {
        this(user.getId(), user.getFname(), user.getLname(), user.getEmail(), user.getImage());
    }

    /**
     * Constructs a new PublicUserDetails with the provided user information.
     *
     * @param id        the user's unique ID
     * @param firstName the user's first name
     * @param lastName  the user's last name
     * @param email     the user's email address
     * @param image     the user's image
     */
    public PublicUserDetails(long id, String firstName, String lastName, String email, String image) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.image = image;
    }

    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getImage() {
        return image;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PublicUserDetails that)) return false;
        return id == that.id && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName);
    }

    @Override
    public String toString() {
        return "PublicUserDetails{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
