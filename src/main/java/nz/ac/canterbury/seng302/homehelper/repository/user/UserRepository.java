package nz.ac.canterbury.seng302.homehelper.repository.user;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Renovation repository accessor using Spring's @link{CrudRepository}
 */
@Repository
public interface UserRepository extends CrudRepository<User, String> {


    /**
     * Finds a user by their unique email
     *
     * @param email email address to find the user by
     * @return a list of users with that name (there should only be one user)
     */
    @Query("SELECT u FROM RENOVATION_USER u WHERE u.email = :email")
    List<User> findByEmail(String email);

    /**
     * Get profile image of the given user
     *
     * @param email User's email
     * @return File path of user's profile image
     */
    @Query
            ("SELECT u.image FROM RENOVATION_USER u WHERE u.email = :email")
    List<String> getUserImage(String email);

    /**
     * Update the file path to the user's profile picture
     *
     * @param image New file path to save
     * @param id    ID to find user by
     */
    @Transactional
    @Modifying
    @Query("update RENOVATION_USER u set u.image = ?1 where u.id = ?2")
    void setUserImage(String image, String id);

    /**
     * Finds a user from a renovation user relationship with the users id
     *
     * @param id the id of the user
     * @return the user
     */
    @Query("SELECT u from RENOVATION_USER  u WHERE u.id = :id")
    List<User> findUserById(long id);

}
