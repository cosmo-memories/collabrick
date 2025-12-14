package nz.ac.canterbury.seng302.homehelper.integration.controller.renovation;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.InvitationStatus;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole;
import nz.ac.canterbury.seng302.homehelper.service.renovation.InvitationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class InvitationIntegrationTests {

    private @Autowired MockMvc mockMvc;
    private @Autowired UserService userService;
    private @Autowired RenovationService renovationService;
    private @Autowired InvitationService invitationService;

    private final String newUserEmail = "jane@gmail.com";
    private Renovation renovation;
    private User renovationOwner;
    private User invitee;


    @BeforeEach
    public void setup() {
        renovationOwner = new User("Jerry", "Joe", "joe@gmail.com", "Abc123!!", "Abc123!!");
        renovationOwner = userService.addUser(renovationOwner);

        invitee = new User("Bob", "Jones", "bob@gmail.com", "Abc123!!", "Abc123!!");
        invitee = userService.addUser(invitee);

        renovation = new Renovation("Kitchen Remodel", "Remodel the kitchen");
        renovation = renovationService.saveRenovation(renovation, renovationOwner);
    }

    // accept - blue sky

    @Test
    void openInvitation_GivenValidTokenAndExistingUserAndEmailMatchesInvitation_ThenAcceptsInvitationAndRedirectsToMyRenovations() throws Exception {
        Invitation invitation = createInvitation(invitee.getEmail());
        UUID token = invitation.getId();

        mockMvc.perform(get("/invitation")
                        .queryParam("token", token.toString())
                        .with(user(String.valueOf(invitee.getId()))))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/renovation/" + renovation.getId()));

        Optional<Invitation> optionalInvitation = invitationService.getInvitation(token.toString());
        assertTrue(optionalInvitation.isPresent());

        Invitation invitationResult = optionalInvitation.get();
        assertEquals(InvitationStatus.ACCEPTED, invitationResult.getInvitationStatus());

        Renovation renovationResult = renovationService.findRenovation(renovation.getId()).orElseThrow();
        assertEquals(2, renovationResult.getMembers().size());
        assertEquals(RenovationMemberRole.OWNER, getMemberByUser(renovationResult, renovationOwner).getRole());
        assertEquals(RenovationMemberRole.MEMBER, getMemberByUser(renovationResult, invitee).getRole());
    }

    // decline - blue sky
    @Test
    void declineInvitation_GivenValidToken_ThenDeclinesInvitationAndShowsDeclinedPage() throws Exception {
        Invitation invitation = createInvitation(invitee.getEmail());
        UUID token = invitation.getId();

        mockMvc.perform(get("/decline-invitation")
                        .queryParam("token", token.toString())
                        .with(user(String.valueOf(invitee.getId()))))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/declinedInvitationPage"));

        Optional<Invitation> optionalInvitation = invitationService.getInvitation(token.toString());
        assertTrue(optionalInvitation.isPresent());

        Invitation invitationResult = optionalInvitation.get();
        assertEquals(InvitationStatus.DECLINED, invitationResult.getInvitationStatus());

        Renovation renovationResult = renovationService.findRenovation(renovation.getId()).orElseThrow();
        assertEquals(1, renovationResult.getMembers().size());
        assertEquals(RenovationMemberRole.OWNER, getMemberByUser(renovationResult, renovationOwner).getRole());

    }

    @Test
    void openInvitation_GivenValidTokenAndExistingUserAndEmailDoesNotMatchInvitation_ThenAcceptsInvitationAndRedirectsToMyRenovations() throws Exception {
        Invitation invitation = createInvitation(invitee.getEmail());
        UUID token = invitation.getId();

        mockMvc.perform(get("/invitation")
                        .queryParam("token", token.toString())
                        .with(user(String.valueOf(invitee.getId()))))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/renovation/" + renovation.getId()));
    }

    @Test
    public void testInvitationAccepting_WhenRecipientLogsInNormally_InvitationIsNotResolvedAndRecipientUserIsNotMember() throws Exception {
        Invitation invitation = createInvitation(invitee.getEmail());

        mockMvc.perform(post("/do_login")
                .param("email", invitee.getEmail())
                .param("password", "Abc123!!")
                .with(csrf()));
        assertSame(InvitationStatus.PENDING, invitation.getInvitationStatus());
        assertFalse(renovation.isMember(invitee));
    }

    // valid token, user unauthenticated

    @Test
    void openInvitation_GivenValidTokenAndExistingUserNotLoggedIn_ThenRedirectsToLogin() throws Exception {
        Invitation invitation = createInvitation(invitee.getEmail());
        UUID token = invitation.getId();
        String expectedRedirect = UriUtils.encode("/invitation?token=" + token, StandardCharsets.UTF_8);

        mockMvc.perform(get("/invitation")
                        .queryParam("token", token.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?redirect=" + expectedRedirect));

    }

    @Test
    void openInvitation_GivenValidTokenAndNonExistingUserNotLoggedIn_ThenRedirectsToRegisterAndInvitationIsMarkedAsAcceptedPendingRegistration() throws Exception {
        Invitation invitation = createInvitation(newUserEmail);
        UUID token = invitation.getId();

        mockMvc.perform(get("/invitation")
                        .queryParam("token", token.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"));

        Optional<Invitation> optionalInvitation = invitationService.getInvitation(token.toString());
        assertTrue(optionalInvitation.isPresent());

        Invitation invitationResult = optionalInvitation.get();
        assertTrue(invitationResult.getAcceptedPendingRegistration());
        assertEquals(InvitationStatus.PENDING, invitationResult.getInvitationStatus());
    }

    // accepting - invalid token or accepting an already accepted/declined/expired invitation

    @Test
    void openInvitation_GivenInvalidToken_ThenShowsNotFoundPage() throws Exception {
        mockMvc.perform(get("/invitation")
                        .queryParam("token", "invalid"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/notFoundPage"));
    }

    @Test
    void openInvitation_GivenUserAttemptsToAcceptAlreadyAcceptedInvitation_ThenShowsExpiredPage() throws Exception {
        Invitation invitation = createAcceptedInvitation(invitee.getEmail());
        UUID token = invitation.getId();

        mockMvc.perform(get("/invitation")
                        .param("token", token.toString())
                        .with(user(String.valueOf(invitee.getId()))))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/expiredInvalidInvitationPage"))
                .andExpect(model().attribute("isInviteExpired", false));
    }

    @Test
    void openInvitation_GivenUserAttemptsToAcceptAlreadyDeclinedInvitation_ThenShowsExpiredPage() throws Exception {
        Invitation invitation = createDeclinedInvitation(invitee.getEmail());
        UUID token = invitation.getId();

        mockMvc.perform(get("/invitation")
                        .param("token", token.toString())
                        .with(user(String.valueOf(invitee.getId()))))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/expiredInvalidInvitationPage"))
                .andExpect(model().attribute("isInviteExpired", false));
    }

    @Test
    void openInvitation_GivenUserAttemptsToAcceptExpiredInvitation_ThenShowsExpiredPage() throws Exception {
        Invitation invitation = createExpiredInvitation(invitee.getEmail());
        UUID token = invitation.getId();

        mockMvc.perform(get("/invitation")
                        .queryParam("token", token.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/expiredInvalidInvitationPage"))
                .andExpect(model().attribute("isInviteExpired", true));
    }

    // declining - invalid token or accepting an already accepted/declined/expired invitation

    @Test
    void declineInvitation_GivenInvalidToken_ThenShowsNotFoundPage() throws Exception {
        mockMvc.perform(get("/decline-invitation")
                        .queryParam("token", "invalid"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/notFoundPage"));
    }

    @Test
    void declineInvitation_GivenUserAttemptsToAcceptAlreadyAcceptedInvitation_ThenShowsExpiredPage() throws Exception {
        Invitation invitation = createAcceptedInvitation(invitee.getEmail());
        UUID token = invitation.getId();

        mockMvc.perform(get("/decline-invitation")
                        .param("token", token.toString())
                        .with(user(String.valueOf(invitee.getId()))))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/expiredInvalidInvitationPage"))
                .andExpect(model().attribute("isInviteExpired", false));
    }

    @Test
    void declineInvitation_GivenUserAttemptsToAcceptAlreadyDeclinedInvitation_ThenShowsExpiredPage() throws Exception {
        Invitation invitation = createDeclinedInvitation(invitee.getEmail());
        UUID token = invitation.getId();

        mockMvc.perform(get("/decline-invitation")
                        .param("token", token.toString())
                        .with(user(String.valueOf(invitee.getId()))))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/expiredInvalidInvitationPage"))
                .andExpect(model().attribute("isInviteExpired", false));
    }

    @Test
    void declineInvitation_GivenUserAttemptsToAcceptExpiredInvitation_ThenShowsExpiredPage() throws Exception {
        Invitation invitation = createExpiredInvitation(invitee.getEmail());
        UUID token = invitation.getId();

        mockMvc.perform(get("/decline-invitation")
                        .queryParam("token", token.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/expiredInvalidInvitationPage"))
                .andExpect(model().attribute("isInviteExpired", true));
    }

    private Invitation createInvitation(String email) {
        return invitationService.createInvite(email, renovation);
    }

    private Invitation createAcceptedInvitation(String email) {
        Invitation invitation = invitationService.createInvite(email, renovation);
        invitationService.acceptInvitation(invitation);
        return invitationService.getInvitation(invitation.getId().toString()).orElseThrow();
    }

    private Invitation createDeclinedInvitation(String email) {
        Invitation invitation = invitationService.createInvite(email, renovation);
        invitationService.declineInvitation(invitation);
        return invitationService.getInvitation(invitation.getId().toString()).orElseThrow();
    }

    private Invitation createExpiredInvitation(String email) {
        Invitation invitation = invitationService.createInvite(email, renovation);
        invitationService.expireInvitation(invitation);
        return invitationService.getInvitation(invitation.getId().toString()).orElseThrow();
    }

    private RenovationMember getMemberByUser(Renovation renovation, User user) {
        return renovation.getMembers()
                .stream()
                .filter(member -> member.getUser() == user)
                .findFirst()
                .orElseThrow();
    }
}
