package nz.ac.canterbury.seng302.homehelper.validation.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.InvitationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class InvitationValidation {

    public static final String DUPLICATE_INVITE_ERROR = " has already been selected";
    public static final String SELF_INVITE_ERROR = "You cannot invite yourself";
    public static final String ALREADY_MEMBER_ERROR = " is already a member";
    public static final String USER_LIST_EMPTY_ERROR = "You must add at least one member before sending invitations";
    public static final String EMAIL_ALREADY_INVITED_FIRST_HALF_ERROR = "User with email address ";
    public static final String EMAIL_ALREADY_INVITED_SECOND_HALF_ERROR = " has already been invited";

    /**
     * Check the current list of users to invite and make sure the given user is not already there.
     *
     * @param currentUsers Current list of users to invite.
     * @param newUser      User to check.
     * @return Empty string if user is not already in list, otherwise error message.
     */
    public static String checkDuplicates(ArrayList<String> currentUsers, String newUser) {
        int counter = 0;
        for (String user : currentUsers) {
            if (Objects.equals(user, newUser)) {
                counter++;
                if (counter > 1) {
                    // User shouldn't be in list twice
                    return user + DUPLICATE_INVITE_ERROR;
                }
            }
        }
        return "";
    }

    /**
     * Check to ensure the current (logged in) user is not in the invite list.
     *
     * @param inviteUsers List of users to be invited.
     * @param currentUser Current (logged in) user.
     * @return Empty string if user is not in the list, otherwise error message.
     */
    public static String checkSelf(List<String> inviteUsers, User currentUser) {
        for (String user : inviteUsers) {
            if (Objects.equals(user, currentUser.getEmail())) {
                return SELF_INVITE_ERROR;
            }
        }
        return "";
    }

    /**
     * Check that the list of users to invite is not empty.
     *
     * @param inviteUsers List of users to be invited.
     * @return Empty string if list is nonempty, otherwise error message.
     */
    public static String checkUsers(ArrayList<String> inviteUsers) {
        if (inviteUsers == null || inviteUsers.isEmpty()) {
            return USER_LIST_EMPTY_ERROR;
        }
        return "";
    }

    /**
     * Check that given user is not already a member of the renovation.
     *
     * @param currentMembers List of renovation's current members.
     * @param newUser        New user to be added.
     * @return Empty string if user is not a member, otherwise error message.
     */
    public static String checkUserMembership(ArrayList<User> currentMembers, User newUser) {
        for (User member : currentMembers) {
            if (Objects.equals(member.getId(), newUser.getId())) {
                return member.getFname() + " " + member.getLname() + ALREADY_MEMBER_ERROR;
            }
        }
        return "";
    }

    /**
     * Check that given email is not already a member of the renovation.
     *
     * @param currentMembers List of renovation's current members.
     * @param email          New email to be added.
     * @return Empty string if email is not a member, otherwise error message.
     */
    public static String checkEmailMembership(Set<RenovationMember> currentMembers, String email) {
        for (RenovationMember member : currentMembers) {
            if (Objects.equals(member.getUser().getEmail(), email)) {
                return email + ALREADY_MEMBER_ERROR;
            }
        }
        return "";
    }

    /**
     * Check that a given user's email is not already invited to a renovation
     *
     * @param invitedUsers List of renovation's current members + users that have been invited
     * @param newUser      New user to be added
     * @return Empty string if user is not invited (member or pending), otherwise error message
     */
    public static String checkEmail(List<Invitation> invitedUsers, String newUser) {
        for (Invitation invite : invitedUsers) {
            InvitationStatus status = invite.getInvitationStatus();
            if (status == InvitationStatus.EXPIRED || status == InvitationStatus.DECLINED)
                continue;

            // ChatGPT fix for validation failing:
            // If invite has associated User
            if (invite.getUser() != null && Objects.equals(invite.getUser().getEmail(), newUser)) {
                return EMAIL_ALREADY_INVITED_FIRST_HALF_ERROR + newUser + EMAIL_ALREADY_INVITED_SECOND_HALF_ERROR;
            }
            // If invite has associated email address
            if (Objects.equals(invite.getEmail(), newUser)) {
                return EMAIL_ALREADY_INVITED_FIRST_HALF_ERROR + invite.getEmail() + EMAIL_ALREADY_INVITED_SECOND_HALF_ERROR;
            }
        }
        return "";
    }

}
