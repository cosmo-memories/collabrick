package nz.ac.canterbury.seng302.homehelper.service.user;

import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UserDetailsInvalidException;
import nz.ac.canterbury.seng302.homehelper.exceptions.user.UserNotActivatedException;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UserUpdatePasswordException;
import nz.ac.canterbury.seng302.homehelper.model.auth.UserPasswordUpdate;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.validation.user.UserValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static nz.ac.canterbury.seng302.homehelper.exceptions.auth.UserUpdatePasswordException.*;

/**
 * Service class for User, defined by the @link{Service} annotation.
 * This class links automatically with @link{UserRepository}, see @link{Autowired}
 * constructor below
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final LocationService locationService;
    Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Constructs a {@code UserService} with the user repository
     *
     * @param userRepository  the repository for handling users
     * @param passwordEncoder the password encoder for encoding passwords
     * @param emailService    the email service for handling sending emails
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, LocationService locationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.locationService = locationService;
    }

    /**
     * Adds a user to the database if the user details are valid and throws an exception if they're not
     *
     * @param user to be added to the database
     * @return the user that is added to the database
     * @throws UserDetailsInvalidException thrown if user details are incorrect
     */
    public User addUser(User user) throws UserDetailsInvalidException {
        String fNameCheck = UserValidation.validateName(user.getFname(), "First");
        UserDetailsInvalidException exception = new UserDetailsInvalidException();
        boolean valid = true;
        if (!fNameCheck.isEmpty()) {
            exception.setFirstNameError(fNameCheck);
            valid = false;
        }

        String lNameCheck = UserValidation.validateName(user.getLname(), "Last");
        if (!lNameCheck.isEmpty()) {
            exception.setLastNameError(lNameCheck);
            valid = false;
        }

        String emailFormatCheck = UserValidation.validateEmailFormat(user.getEmail());
        String emailExistenceCheck = UserValidation.checkRegisterEmail(userRepository, user.getEmail());
        if (!emailFormatCheck.isEmpty()) {
            exception.setEmailError(emailFormatCheck);
            valid = false;
        } else if (!emailExistenceCheck.isEmpty()) {
            exception.setEmailError(emailExistenceCheck);
            valid = false;
        }

        String passwordStrengthCheck = UserValidation.validatePasswordStrength(user.getPassword());
        String passwordMatchCheck = UserValidation.validatePasswordMatch(user.getPassword(), user.getRetypePassword());
        if (!passwordStrengthCheck.isEmpty()) {
            exception.setPasswordError(passwordStrengthCheck);
            valid = false;
        } else if (!passwordMatchCheck.isEmpty()) {
            exception.setPasswordError(passwordMatchCheck);
            valid = false;
        }

        try {
            locationService.validateUserLocation(user.getLocation(), exception.getFirstNameError(),
                    exception.getLastNameError(), exception.getEmailError(), exception.getPasswordError());
        } catch (UserDetailsInvalidException e) {
            exception = e;
            valid = false;
        }

        if (!valid) {
            throw exception;
        }

        String password = user.getPassword();
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        user.setRetypePassword(hashedPassword);
        user.grantAuthority("ROLE_USER");
        user.setCreatedTimestamp(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Validates that the user is able to be logged in
     *
     * @param user to log in
     * @throws IllegalArgumentException  thrown if email is unknown, in the wrong format or password is incorrect
     * @throws UserNotActivatedException thrown if the user cannot be activated
     */
    public void validateUserForSignIn(User user) throws UserDetailsInvalidException, UserNotActivatedException {
        String emailFormatCheck = UserValidation.validateEmailFormat(user.getEmail());
        if (!emailFormatCheck.isEmpty()) {
            UserDetailsInvalidException exception = new UserDetailsInvalidException();
            exception.setEmailError(emailFormatCheck);
            throw exception;
        }
    }

    /**
     * Updates user information including first name, last name and email. Throws illegal argument exception if updates are invalid
     *
     * @param user             user to update
     * @param updatedFirstName the updated first name
     * @param updatedLastName  the updated last name
     * @param updatedEmail     the updated email address
     * @throws IllegalArgumentException thrown if any of the inputs are invalid
     */
    public void updateUser(User user, String updatedFirstName, String updatedLastName, String updatedEmail) throws IllegalArgumentException {
        String fNameCheck = UserValidation.validateName(updatedFirstName, "First");
        UserDetailsInvalidException exception = new UserDetailsInvalidException();
        boolean valid = true;
        if (!fNameCheck.isEmpty()) {
            exception.setFirstNameError(fNameCheck);
            valid = false;
        }

        String lNameCheck = UserValidation.validateName(updatedLastName, "Last");
        if (!lNameCheck.isEmpty()) {
            exception.setLastNameError(lNameCheck);
            valid = false;
        }
        String emailFormatCheck = UserValidation.validateEmailFormat(updatedEmail);
        String emailExistenceCheck = UserValidation.checkRegisterEmail(userRepository, updatedEmail);
        if (!emailFormatCheck.isEmpty()) {
            exception.setEmailError(emailFormatCheck);
            valid = false;
        } else if (!emailExistenceCheck.isEmpty() && !Objects.equals(user.getEmail(), updatedEmail)) {
            exception.setEmailError(emailExistenceCheck);
            valid = false;
        }

        try {
            locationService.validateUserLocation(user.getLocation(), exception.getFirstNameError(),
                    exception.getLastNameError(), exception.getEmailError(), exception.getPasswordError());
        } catch (UserDetailsInvalidException e) {
            exception = e;
            valid = false;
        }

        if (!valid) {
            throw exception;
        }
        // only apply changes once all validation has passed, else the object provided by user advice will be modified
        // until reloaded when urls change
        user.setFname(updatedFirstName);
        user.setLname(updatedLastName);
        user.setEmail(updatedEmail);
        userRepository.save(user);
    }


    /**
     * Finds the user by email
     *
     * @param email the email of the user
     * @return the user that has been found in the database
     */
    public User findUserByEmail(String email) {
        List<User> users = userRepository.findByEmail(email);
        return users.isEmpty() ? null : users.get(0);
    }

    public void toggleBrickAIAccess(User user) {
        user.setAllowBrickAIChatAccess(!user.isAllowBrickAIChatAccess());
        userRepository.save(user);
    }

    /**
     * Finds the user by id
     *
     * @param id the id of the user
     * @return the user that has been found in the database
     */
    public User findUserById(long id) {
        List<User> users = userRepository.findUserById(id);
        return users.isEmpty() ? null : users.get(0);
    }

    /**
     * Saves changes made to a user
     *
     * @param user the user which is being saved
     */
    public void saveUser(User user) {
        userRepository.save(user);
    }

    /**
     * Deletes a user from the repository
     *
     * @param user the user being deleted from the repository
     */
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    /**
     * Updates the password for the specified user after validating the old password, the strength of the new password,
     * and checking that the new password matches the retyped new password.
     *
     * @param user           The user whose password is to be updated.
     * @param passwordUpdate An object containing the old password, new password, and retyped new password.
     * @throws UserUpdatePasswordException if the old password is incorrect, the new password does not meet strength
     *                                     requirements or the new passwords do not match.
     */
    public void updateUserPassword(User user, UserPasswordUpdate passwordUpdate) throws UserUpdatePasswordException {
        // check if the old password matches their current password
        if (!verifyPassword(passwordUpdate.getOldPassword(), user.getPassword())) {
            throw createOldPasswordError(UserValidation.OLD_PASSWORD_INCORRECT);
        }

        updateUserPassword(user, passwordUpdate.getNewPassword(), passwordUpdate.getRetypedNewPassword());
    }

    /**
     * Updates the user's password after validating its strength and ensuring it matches the confirmation.
     *
     * @param user                 the user whose password is being reset
     * @param newPassword          the new password to be set
     * @param confirmedNewPassword the retyped new password for confirmation
     * @throws UserUpdatePasswordException if the password does not meet strength requirements or does not match
     */
    public void updateUserPassword(User user, String newPassword, String confirmedNewPassword) {
        // check if the new password is of valid strength
        String passwordStrengthCheck = UserValidation.validatePasswordStrength(newPassword);

        // check if the new password and the retyped new password matches
        String passwordMatchCheck = UserValidation.validatePasswordMatch(newPassword, confirmedNewPassword);

        //throw correct errors
        if (!passwordStrengthCheck.isEmpty() && !passwordMatchCheck.isEmpty()) {
            throw createNewPasswordAndRetypedPasswordError(passwordStrengthCheck, passwordMatchCheck);
        }

        if (!passwordStrengthCheck.isEmpty()) {
            throw createNewPasswordError(passwordStrengthCheck);
        }

        if (!passwordMatchCheck.isEmpty()) {
            throw createRetypedNewPasswordError(passwordMatchCheck);
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
        emailService.sendPasswordUpdatedMail(user);
    }

    /**
     * Verifies if a provided password matches the stored hashed password.
     *
     * @param password       the plain-text password to verify
     * @param hashedPassword the stored hashed password
     * @return true if the password matches the hashed password, false otherwise
     */
    public boolean verifyPassword(String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }


    /**
     * Save the user's file to machine (automatically overwrites old file if it exists) and upload file path in database
     *
     * @param file            File to save
     * @param fileNameAndPath File path to save to
     * @param id              User's ID number
     * @return Boolean success or failure
     * @throws IOException Something went wrong
     */
    public boolean saveFile(MultipartFile file, Path fileNameAndPath, String id) throws IOException {
        try {
            // Save file to directory; automatically overwrites old file if it exists
            Files.write(fileNameAndPath, file.getBytes());

            // Save filepath in DB
            String path = fileNameAndPath.toString().substring(7);
            userRepository.setUserImage(path, id);
        } catch (Exception e) {
            // Something went wrong updating DB
            return false;
        }
        return true;
    }
}
