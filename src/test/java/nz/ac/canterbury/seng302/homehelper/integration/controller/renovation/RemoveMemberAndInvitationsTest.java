package nz.ac.canterbury.seng302.homehelper.integration.controller.renovation;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.InvitationStatus;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole;
import nz.ac.canterbury.seng302.homehelper.service.renovation.InvitationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationMemberService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RemoveMemberAndInvitationsTest {
    private @Autowired MockMvc mockMvc;
    private @Autowired UserService userService;
    private @Autowired RenovationService renovationService;
    private @Autowired InvitationService invitationService;
    private @Autowired RenovationMemberService renovationMemberService;


    private User renovationOwner;
    private User member;
    private Renovation renovation;

    @BeforeEach
    public void setup() {
        renovationOwner = new User("Jerry", "Joe", "joe@gmail.com", "Abc123!!", "Abc123!!");
        renovationOwner = userService.addUser(renovationOwner);

        member = new User("Bob", "Jones", "bob@gmail.com", "Abc123!!", "Abc123!!");
        member = userService.addUser(member);

        renovation = new Renovation("Kitchen Remodel", "Remodel the kitchen");
        renovation = renovationService.saveRenovation(renovation, renovationOwner);
    }

    @Test
    public void addMember_removeMember_memberIsRemoved() throws Exception {
        renovation.addMember(member, RenovationMemberRole.MEMBER);
        renovationService.saveRenovation(renovation, renovationOwner);
        RenovationMember renovationMember = renovationMemberService.getRenovationMember(member,renovation);

        mockMvc.perform(post("/renovation/" + renovation.getId() + "/removeMember")
                .param("renovationUser", String.valueOf(renovationMember.getUser().getId()))
                        .with(csrf())
                        .with(user(String.valueOf(renovationOwner.getId())).password(renovationOwner.getPassword()).roles("USER")))
                .andExpect(status().is3xxRedirection());

        assertFalse(renovation.isMember(member));
    }
    @Test
    public void isNotOwner_removeMember_memberIsNotRemoved() throws Exception {
        renovation.addMember(member, RenovationMemberRole.MEMBER);
        renovationService.saveRenovation(renovation, renovationOwner);
        RenovationMember renovationMember = renovationMemberService.getRenovationMember(member,renovation);

        mockMvc.perform(post("/renovation/" + renovation.getId() + "/removeMember")
                        .param("renovationUser", String.valueOf(renovationMember.getUser().getId()))
                        .with(csrf())
                        .with(user(String.valueOf(member.getId())).password(member.getPassword()).roles("USER")))
                .andExpect(status().is4xxClientError());

        assertTrue(renovation.isMember(member));
    }
    @Test
    public void isOwner_removeOwner_OwnerIsNotRemoved() throws Exception {
        renovation.addMember(member, RenovationMemberRole.MEMBER);
        renovationService.saveRenovation(renovation, renovationOwner);

        mockMvc.perform(post("/renovation/" + renovation.getId() + "/removeMember")
                        .param("renovationUser", String.valueOf(renovationOwner.getId()))
                        .with(csrf())
                        .with(user(String.valueOf(renovationOwner.getId())).password(renovationOwner.getPassword()).roles("USER")))
                .andExpect(status().is4xxClientError());

        assertTrue(renovation.isMember(renovationOwner));
    }
    @Test
    public void isNotLoggedIn_removeMember_MemberIsNotRemoved() throws Exception {
        renovation.addMember(member, RenovationMemberRole.MEMBER);
        renovationService.saveRenovation(renovation, renovationOwner);
        RenovationMember renovationMember = renovationMemberService.getRenovationMember(member,renovation);

        mockMvc.perform(post("/renovation/" + renovation.getId() + "/removeMember")
                        .param("renovationUser", String.valueOf(renovationMember.getId()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertTrue(renovation.isMember(renovationOwner));
    }

    @Test
    public void isOwner_removeNonMember_errorThrown() throws Exception {
        mockMvc.perform(post("/renovation/" + renovation.getId() + "/removeMember")
                        .param("renovationUser", String.valueOf(member.getId()))
                        .with(csrf())
                        .with(user(String.valueOf(renovationOwner.getId())).password(renovationOwner.getPassword()).roles("USER")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void sentInvite_removeInvite_inviteIsSetToExpired() throws Exception {
        Invitation invitation = invitationService.createInvite(member.getEmail(),renovation);

        mockMvc.perform(post("/renovation/" + renovation.getId() + "/removeInvitation")
                        .param("invitation", String.valueOf(invitation.getId()))
                        .with(csrf())
                        .with(user(String.valueOf(renovationOwner.getId())).password(renovationOwner.getPassword()).roles("USER")))
                .andExpect(status().is3xxRedirection());

        assertSame(invitation.getInvitationStatus(), InvitationStatus.EXPIRED);

    }

    @Test
    public void acceptedInvite_removedInvite_inviteStatusIsNotChanged() throws Exception {
        Invitation invitation = invitationService.createInvite(member.getEmail(),renovation);
        invitationService.acceptInvitation(invitation);

        mockMvc.perform(post("/renovation/" + renovation.getId() + "/removeInvitation")
                        .param("invitation", String.valueOf(invitation.getId()))
                        .with(csrf())
                        .with(user(String.valueOf(renovationOwner.getId())).password(renovationOwner.getPassword()).roles("USER")))
                .andExpect(status().is3xxRedirection());

        assertSame(invitation.getInvitationStatus(), InvitationStatus.ACCEPTED);
    }

    @Test
    public void sentInvite_memberRemovesInvite_inviteStatusIsNotChanged() throws Exception {
        Invitation invitation = invitationService.createInvite(member.getEmail(),renovation);

        mockMvc.perform(post("/renovation/" + renovation.getId() + "/removeInvitation")
                        .param("invitation", String.valueOf(invitation.getId()))
                        .with(csrf())
                        .with(user(String.valueOf(member.getId())).password(member.getPassword()).roles("USER")))
                .andExpect(status().is4xxClientError());

        assertSame(invitation.getInvitationStatus(), InvitationStatus.PENDING);
    }
}
