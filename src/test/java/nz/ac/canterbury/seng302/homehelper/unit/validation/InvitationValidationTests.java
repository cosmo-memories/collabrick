package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.validation.renovation.InvitationValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Unit tests for invitation user validation
 */
public class InvitationValidationTests {

    ArrayList<User> users;
    ArrayList<String> userEmails;
    ArrayList<Invitation> invitations;
    User testUserOne, testUserTwo, testUserThree;
    Renovation renovation;

    @BeforeEach
    public void setUp() {
        users = new ArrayList<>();
        userEmails = new ArrayList<>();
        invitations = new ArrayList<>();
        renovation = new Renovation("name", "desc");

        testUserOne = new User("Alice", "test", "test1@test.com", "Password123!", "Password123!");
        testUserOne.setId(1L);
        testUserTwo = new User("Bob", "test", "test2@test.com", "Password123!", "Password123!");
        testUserTwo.setId(2L);
        testUserThree = new User("Eve", "test", "test3@test.com", "Password123!", "Password123!");
        testUserThree.setId(3L);

        userEmails.add(testUserOne.getEmail());
        userEmails.add(testUserTwo.getEmail());
        userEmails.add(testUserThree.getEmail());
        users.add(testUserOne);
        users.add(testUserTwo);
        users.add(testUserThree);
        invitations.add(new Invitation(testUserOne.getEmail(), renovation));
        invitations.add(new Invitation(testUserTwo.getEmail(), renovation));
        invitations.add(new Invitation(testUserThree.getEmail(), renovation));
    }

    @Test
    public void checkDuplicate_userNotInUserList_returnEmptyString() {
        User newUser = new User("New", "User", "new@test.com", "Password123!", "Password123!");
        newUser.setId(4L);
        String result = InvitationValidation.checkDuplicates(userEmails, newUser.getEmail());
        assertEquals("", result);
    }

    @Test
    public void checkDuplicate_userInUserList_returnErrorString() {
        userEmails.add(testUserOne.getEmail());
        String result = InvitationValidation.checkDuplicates(userEmails, testUserOne.getEmail());
        assertEquals(testUserOne.getEmail() + InvitationValidation.DUPLICATE_INVITE_ERROR, result);
    }

    @Test
    public void checkSelf_userIsNotSelf_returnEmptyString() {
        User newUser = new User("New", "User", "new@test.com", "Password123!", "Password123!");
        newUser.setId(4L);
        String result = InvitationValidation.checkSelf(userEmails, newUser);
        assertEquals("", result);
    }

    @Test
    public void checkSelf_userIsSelf_returnErrorString() {
        String result = InvitationValidation.checkSelf(userEmails, testUserOne);
        assertEquals(InvitationValidation.SELF_INVITE_ERROR, result);
    }

    @Test
    public void checkUserList_listNonEmpty_returnEmptyString() {
        String result = InvitationValidation.checkUsers(userEmails);
        assertEquals("", result);
    }

    @Test
    public void checkUserList_listEmpty_returnErrorString() {
        String result = InvitationValidation.checkUsers(new ArrayList<>());
        assertEquals(InvitationValidation.USER_LIST_EMPTY_ERROR, result);
    }

    @Test
    public void checkMembership_userNotMember_returnEmptyString() {
        User newUser = new User("New", "User", "new@test.com", "Password123!", "Password123!");
        newUser.setId(4L);
        String result = InvitationValidation.checkUserMembership(users, newUser);
        assertEquals("", result);
    }

    @Test
    public void checkMembership_userInUserList_returnErrorString() {
        String result = InvitationValidation.checkUserMembership(users, testUserOne);
        assertEquals(testUserOne.getFname() + " " + testUserOne.getLname() + InvitationValidation.ALREADY_MEMBER_ERROR, result);
    }

    @Test
    public void checkEmailInvited_emailNotAMemberOrInvited_returnEmptyString() {
        User newUser = new User("New", "User", "new@test.com", "Password123!", "Password123!");
        newUser.setId(4L);
        String result = InvitationValidation.checkEmail(invitations, newUser.getEmail());
        assertEquals("", result);
    }

    @Test
    public void checkEmailInvited_emailInUserList_returnErrorString() {
        String result = InvitationValidation.checkEmail(invitations, testUserOne.getEmail());
        assertEquals(InvitationValidation.EMAIL_ALREADY_INVITED_FIRST_HALF_ERROR + testUserOne.getEmail()
                + InvitationValidation.EMAIL_ALREADY_INVITED_SECOND_HALF_ERROR, result);
    }

}
