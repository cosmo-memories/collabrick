package nz.ac.canterbury.seng302.homehelper.integration.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.InvitationException;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.InvitationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.InvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class InvitationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    private Invitation invitation1;
    private Invitation invitation2;
    private User inviter;
    private User invitee1;
    private User invitee2;
    private Renovation renovation1;
    private Renovation renovation2;

    @BeforeEach
    public void setup() {
        // Cleanup
        invitationRepository.deleteAll();
        userRepository.deleteAll();
        renovationRepository.deleteAll();

        // Setup Users
        inviter = new User("test", "inviter", "inviter@test.com", "Password123!", "Password123!");
        invitee1 = new User("test", "invitee", "user1@test.com", "Password123!", "Password123!");
        invitee2 = new User("test", "uninvolved", "user2@test.com", "Password123!", "Password123!");
        userRepository.save(inviter);
        userRepository.save(invitee1);
        userRepository.save(invitee2);

        // Setup Renovations
        renovation1 = new Renovation("Kitchen Remodel", "Remodel the kitchen");
        renovation1.setOwner(inviter);
        renovationRepository.save(renovation1);
        renovation2 = new Renovation("Bathroom Sink", "Get a new bathroom sink");
        renovation2.setOwner(inviter);
        renovationRepository.save(renovation2);
    }

    @Test
    public void searchForInvitation_correctUserInput_invitationIsFound() throws Exception {
        invitation1 = invitationService.createInvite(invitee1.getEmail(), renovation1);

        Optional<Invitation> result = invitationService.findByRenovationAndUser(invitee1, renovation1);
        assertEquals(Optional.of(invitation1), result);
    }

    @Test
    public void searchForInvitation_unrelatedUserInput_invitationIsNotFound() throws Exception {
        invitation1 = invitationService.createInvite(invitee1.getEmail(), renovation1);

        Optional<Invitation> result = invitationService.findByRenovationAndUser(invitee2, renovation1);
        assertEquals(Optional.empty(), result);
    }

    @Test
    public void searchForInvitation_unrelatedRenovationInput_invitationIsNotFound() throws Exception {
        invitation1 = invitationService.createInvite(invitee1.getEmail(), renovation1);

        Optional<Invitation> result = invitationService.findByRenovationAndUser(invitee1, renovation2);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void searchForInvitation_userHasTwoInvitations_correctInvitationIsFound() throws Exception {
        invitation1 = invitationService.createInvite(invitee1.getEmail(), renovation1);
        invitation2 = invitationService.createInvite(invitee1.getEmail(), renovation2);

        Optional<Invitation> result = invitationService.findByRenovationAndUser(invitee1, renovation2);
        assertEquals(Optional.of(invitation2), result);
    }

    @Test
    void searchForInvitation_renovationHasTwoInvitedUsers_correctInvitationIsFound() throws Exception {
        invitation1 = invitationService.createInvite(invitee1.getEmail(), renovation1);
        Invitation invitation3 = invitationService.createInvite(invitee2.getEmail(), renovation1);

        Optional<Invitation> result = invitationService.findByRenovationAndUser(invitee2, renovation1);
        assertEquals(Optional.of(invitation3), result);
    }

    @Test
    void searchInvitesByReno_NoResults() {
        assertTrue(invitationService.findInvitesByRenovation(renovation1).isEmpty());
    }

    @Test
    void searchInvitesByReno_OneRegisteredResult() {
        invitation1 = invitationService.createInvite(invitee1.getEmail(), renovation1);

        assertEquals(invitee1, invitationService.findInvitesByRenovation(renovation1).getFirst().getUser());
    }

    @Test
    void searchInvitesByReno_TwoRegisteredResults() {
        invitation1 = invitationService.createInvite(invitee1.getEmail(), renovation1);
        invitation2 = invitationService.createInvite(invitee2.getEmail(), renovation1);

        List<Invitation> result = invitationService.findInvitesByRenovation(renovation1);
        assertTrue(result.contains(invitation1) && result.contains(invitation2) && result.size() == 2);
    }

    @Test
    void searchInvitesByReno_OneUnregisteredResult() {
        Invitation invite = invitationService.createInvite("abc@abc.abc", renovation1);

        List<Invitation> result = invitationService.findInvitesByRenovation(renovation1);
        assertTrue(result.contains(invite) && result.size() == 1);
    }

    @Test
    void searchInvitesByReno_TwoUnregisteredResults() {
        Invitation invite1 = invitationService.createInvite("abc@abc.abc", renovation1);
        Invitation invite2 = invitationService.createInvite("def@def.def", renovation1);

        List<Invitation> result = invitationService.findInvitesByRenovation(renovation1);
        assertTrue(result.contains(invite1) && result.contains(invite2) && result.size() == 2);
    }

    @Test
    void searchInvitesByReno_RegisteredAndUnregisteredResults() {
        invitation1 = new Invitation(invitee1, renovation1);
        invitationRepository.save(invitation1);
        Invitation newInvite = invitationService.createInvite("abc@abc.abc", renovation1);

        List<Invitation> result = invitationService.findInvitesByRenovation(renovation1);
        assertTrue(result.contains(invitation1) && result.contains(newInvite) && result.size() == 2);
    }

    @Test
    void searchInvitesByUser_NoResults() {
        List<Invitation> result = invitationService.findInvitesByUser(invitee1);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchInvitesByUser_OneResult() {
        invitation1 = invitationService.createInvite(invitee1.getEmail(), renovation1);

        List<Invitation> result = invitationService.findInvitesByUser(invitee1);
        assertTrue(result.size() == 1 && result.contains(invitation1));
    }

    @Test
    void searchInvitesByUser_SeveralResults() {
        invitation1 = invitationService.createInvite(invitee1.getEmail(), renovation1);
        invitation2 = invitationService.createInvite(invitee1.getEmail(), renovation2);

        List<Invitation> result = invitationService.findInvitesByUser(invitee1);
        assertTrue(result.size() == 2 && result.contains(invitation1) && result.contains(invitation2));
    }

    @Test
    void searchInvitesByEmail_UnregisteredEmail_NoResults() {
        List<Invitation> result = invitationService.findInvitesByEmail("unregistered@email.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void searchInvitesByEmail_UnregisteredEmail_OneResult() {
        invitation1 = invitationService.createInvite("unregistered@email.com", renovation1);

        List<Invitation> result = invitationService.findInvitesByEmail("unregistered@email.com");
        assertTrue(result.size() == 1 && result.contains(invitation1));
    }

    @Test
    void searchInvitesByEmail_UnregisteredEmail_SeveralResults() {
        invitation1 = invitationService.createInvite("unregistered@email.com", renovation1);
        invitation2 = invitationService.createInvite("unregistered@email.com", renovation2);

        List<Invitation> result = invitationService.findInvitesByEmail("unregistered@email.com");
        assertTrue(result.size() == 2 && result.contains(invitation1) && result.contains(invitation2));
    }

    @Test
    void searchInvitesByEmail_RegisteredEmail_NoResults() {
        List<Invitation> result = invitationService.findInvitesByEmail(invitee1.getEmail());
        assertTrue(result.isEmpty());
    }

    @Test
    void searchInvitesByEmail_RegisteredEmail_OneResult() {
        invitation1 = invitationService.createInvite(invitee1.getEmail(), renovation1);

        List<Invitation> result = invitationService.findInvitesByEmail(invitee1.getEmail());
        assertTrue(result.size() == 1 && result.contains(invitation1));
    }

    @Test
    void searchInvitesByEmail_RegisteredEmail_SeveralResults() {
        invitation1 = invitationService.createInvite(invitee1.getEmail(), renovation1);
        invitation2 = invitationService.createInvite(invitee1.getEmail(), renovation2);

        List<Invitation> result = invitationService.findInvitesByEmail(invitee1.getEmail());
        assertTrue(result.size() == 2 && result.contains(invitation1) && result.contains(invitation2));
    }

    @Test
    void createInvite_UnregisteredEmail_Passes() {
        invitation1 = invitationService.createInvite("unregistered@email.com", renovation1);
        assertEquals("unregistered@email.com", invitation1.getEmail());
        assertNull(invitation1.getUser());
        assertEquals(renovation1, invitation1.getRenovation());
    }

    @Test
    void createInvite_RegisteredEmail_Passes() {
        invitation1 = invitationService.createInvite(invitee1.getEmail(), renovation1);
        assertEquals(invitee1, invitation1.getUser());
        assertEquals(renovation1, invitation1.getRenovation());
    }

    @Test
    void inviteValidation_RegisteredEmails_Passes() {
        ArrayList<String> invitees = new ArrayList<>();
        invitees.add(invitee1.getEmail());
        invitees.add(invitee2.getEmail());
        assertDoesNotThrow(() -> invitationService.validateInvitationData(invitees, renovation1));
    }

    @Test
    void inviteValidation_UnregisteredEmails_Passes() {
        ArrayList<String> invitees = new ArrayList<>();
        invitees.add("blah@email.com");
        invitees.add("hah@email.com");
        assertDoesNotThrow(() -> invitationService.validateInvitationData(invitees, renovation1));
    }

    @Test
    void inviteValidation_MixedEmails_Passes() {
        ArrayList<String> invitees = new ArrayList<>();
        invitees.add(invitee1.getEmail());
        invitees.add("blah@email.com");
        assertDoesNotThrow(() -> invitationService.validateInvitationData(invitees, renovation1));
    }

    @Test
    void inviteValidation_DuplicateUsers_Fails() {
        ArrayList<String> invitees = new ArrayList<>();
        invitees.add(invitee1.getEmail());
        invitees.add(invitee1.getEmail());
        assertThrows(InvitationException.class, () -> invitationService.validateInvitationData(invitees, renovation1));
    }

    @Test
    void inviteValidation_DuplicateEmail_Fails() {
        ArrayList<String> invitees = new ArrayList<>();
        invitees.add("blah@email.com");
        invitees.add("blah@email.com");
        assertThrows(InvitationException.class, () -> invitationService.validateInvitationData(invitees, renovation1));
    }

    @Test
    void inviteValidation_EmptyList_Fails() {
        ArrayList<String> invitees = new ArrayList<>();
        assertThrows(InvitationException.class, () -> invitationService.validateInvitationData(invitees, renovation1));
    }

    @Test
    void inviteValidation_InvitingSelf_Fails() {
        ArrayList<String> invitees = new ArrayList<>();
        invitees.add(renovation1.getOwner().getEmail());
        assertThrows(InvitationException.class, () -> invitationService.validateInvitationData(invitees, renovation1));
    }

    @Test
    void inviteValidation_InvitingSelfAndOthers_Fails() {
        ArrayList<String> invitees = new ArrayList<>();
        invitees.add(invitee1.getEmail());
        invitees.add(renovation1.getOwner().getEmail());
        assertThrows(InvitationException.class, () -> invitationService.validateInvitationData(invitees, renovation1));
    }

    @Test
    void inviteValidation_AlreadyInvitedUser_Fails() {
        ArrayList<String> invitees = new ArrayList<>();
        invitees.add(invitee1.getEmail());
        invitees.add(invitee2.getEmail());
        invitationService.createInvite(invitee1.getEmail(), renovation1);
        assertThrows(InvitationException.class, () -> invitationService.validateInvitationData(invitees, renovation1));
    }

    @Test
    void inviteValidation_AlreadyInvitedEmail_Fails() {
        ArrayList<String> invitees = new ArrayList<>();
        invitees.add("blah@email.com");
        invitees.add(invitee2.getEmail());
        invitationService.createInvite("blah@email.com", renovation1);
        assertThrows(InvitationException.class, () -> invitationService.validateInvitationData(invitees, renovation1));
    }
}
