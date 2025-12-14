package nz.ac.canterbury.seng302.homehelper.integration.controller.renovation;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.InvitationStatus;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.InvitationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class InvitationControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    private Renovation renovation;

    private User renovationOwner;


    @BeforeEach
    public void setup() {
        renovationRepository.deleteAll();
        userRepository.deleteAll();
        invitationRepository.deleteAll();
        renovationOwner = new User("Renovation owner", "", "user@test.com", "Password123!", "Password123!");
        userRepository.save(renovationOwner);
        renovation = new Renovation("Luxury outdoor kitchen", "New deck and furniture along with an outdoor pizza oven");
        renovation.setOwner(renovationOwner);
        renovationRepository.save(renovation);

    }

    @Test
    public void declineRenovation_ReceiverLoggedIn_InvitationSuccessfullyDeclined() throws Exception {
        User receiver = new User("Receiver", "receiver", "receiver@test.com", "Password123!", "Password123!");
        userRepository.save(receiver);
        Invitation invitation = new Invitation(receiver, renovation);
        invitationRepository.save(invitation);

        mockMvc.perform(get("/decline-invitation")
                        .queryParam("token", invitation.getId().toString())
                        .with(user(String.valueOf(receiver.getId())).password(receiver.getPassword()).roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/declinedInvitationPage"));

        assertSame(InvitationStatus.DECLINED, invitationRepository.findById(invitation.getId()).get().getInvitationStatus());
    }

    @Test
    public void declineRenovation_ReceiverNotLoggedIn_InvitationSuccessfullyDeclined() throws Exception {
        User receiver = new User("Receiver", "receiver", "receiver@test.com", "Password123!", "Password123!");
        userRepository.save(receiver);
        Invitation invitation = new Invitation(receiver, renovation);
        invitationRepository.save(invitation);

        mockMvc.perform(get("/decline-invitation")
                        .queryParam("token", invitation.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/declinedInvitationPage"));

        assertSame(InvitationStatus.DECLINED, invitationRepository.findById(invitation.getId()).get().getInvitationStatus());
    }

    @Test
    public void declineRenovation_LoggedInAsAnotherUser_InvitationSuccessfullyDeclined() throws Exception {
        User receiver = new User("Receiver", "receiver", "receiver@test.com", "Password123!", "Password123!");
        userRepository.save(receiver);

        Invitation invitation = new Invitation(receiver, renovation);
        invitationRepository.save(invitation);

        User otherUser = new User("Other", "other", "other@test.com", "Password123!", "Password123!");
        userRepository.save(otherUser);

        mockMvc.perform(get("/decline-invitation")
                        .queryParam("token", invitation.getId().toString())
                        .with(user(String.valueOf(otherUser.getId())).password(otherUser.getPassword()).roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertSame(InvitationStatus.DECLINED, invitationRepository.findById(invitation.getId()).get().getInvitationStatus());
    }

    @Test
    public void declineInvitation_ReceiverNotKnownByTheSystem_InvitationSuccessfullyDeclined() throws Exception {
        String receiver = "receiver@test.com";
        Invitation invitation = new Invitation(receiver, renovation);
        invitationRepository.save(invitation);

        mockMvc.perform(get("/decline-invitation")
                        .queryParam("token", invitation.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/declinedInvitationPage"));

        assertSame(InvitationStatus.DECLINED, invitationRepository.findById(invitation.getId()).get().getInvitationStatus());
    }


    /*
    Test cases identified and not yet implemented :
    - invitation already resolved (i.e. already accepted, already declined, expired)
     */

    @Test
    void validPendingToken_acceptInvitationWithAnonymousUser_redirectsToLogin() throws Exception {
        Invitation pendingInvitation = new Invitation("invitee@test.com", renovation);
        pendingInvitation.setInvitationStatus(InvitationStatus.PENDING);
        invitationRepository.save(pendingInvitation);

        mockMvc.perform(get("/invitation")
                        .queryParam("token", pendingInvitation.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"));
    }

    @Test
    void expiredToken_acceptInvitation_rendersExpiredPageWithModel() throws Exception {
        Invitation expiredInvitation = new Invitation("invitee@test.com", renovation);
        expiredInvitation.setInvitationStatus(InvitationStatus.EXPIRED);
        invitationRepository.save(expiredInvitation);

        mockMvc.perform(get("/invitation")
                        .queryParam("token", expiredInvitation.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/expiredInvalidInvitationPage"))
                .andExpect(model().attribute("isInviteExpired", true))
                .andExpect(model().attribute("invitee", expiredInvitation.getEmail()))
                .andExpect(model().attribute("renovationName", renovation.getName()))
                .andExpect(model().attributeExists("renovationOwner"));
    }

    @Test
    void acceptedToken_acceptInvitation_rendersInvalidPageWithModel() throws Exception {
        Invitation acceptedInvitation = new Invitation("invitee@test.com", renovation);
        acceptedInvitation.setInvitationStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(acceptedInvitation);

        mockMvc.perform(get("/invitation")
                        .queryParam("token", acceptedInvitation.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/expiredInvalidInvitationPage"))
                .andExpect(model().attribute("isInviteExpired", false))
                .andExpect(model().attribute("invitee", acceptedInvitation.getEmail()))
                .andExpect(model().attribute("renovationName", renovation.getName()))
                .andExpect(model().attributeExists("renovationOwner"));
    }

    @Test
    void declinedToken_acceptInvitation_rendersInvalidPageWithModel() throws Exception {
        Invitation declinedInvitation = new Invitation("invitee@test.com", renovation);
        declinedInvitation.setInvitationStatus(InvitationStatus.DECLINED);
        invitationRepository.save(declinedInvitation);

        mockMvc.perform(get("/invitation")
                        .queryParam("token", declinedInvitation.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/expiredInvalidInvitationPage"))
                .andExpect(model().attribute("isInviteExpired", false))
                .andExpect(model().attribute("invitee", declinedInvitation.getEmail()))
                .andExpect(model().attribute("renovationName", renovation.getName()))
                .andExpect(model().attributeExists("renovationOwner"));
    }

    @Test
    void acceptInvitation_tokenNotFound_rendersNotFoundPage() throws Exception {
        String nonexistentToken = "999999";
        mockMvc.perform(get("/invitation")
                        .queryParam("token", nonexistentToken)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/notFoundPage"));
    }

    @Test
    void expiredToken_declineInvitation_rendersExpiredPageWithModel() throws Exception {
        Invitation expiredInvitation = new Invitation("invitee@test.com", renovation);
        expiredInvitation.setExpiryDate(LocalDateTime.now().minusDays(1));
        expiredInvitation.setInvitationStatus(InvitationStatus.EXPIRED);
        invitationRepository.save(expiredInvitation);

        mockMvc.perform(get("/decline-invitation")
                        .queryParam("token", expiredInvitation.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/expiredInvalidInvitationPage"))
                .andExpect(model().attribute("isInviteExpired", true))
                .andExpect(model().attribute("invitee", expiredInvitation.getEmail()))
                .andExpect(model().attribute("renovationName", renovation.getName()))
                .andExpect(model().attributeExists("renovationOwner"));
    }

}
