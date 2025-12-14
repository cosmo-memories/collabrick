package nz.ac.canterbury.seng302.homehelper.unit.service.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.user.PublicUserDetailsRenovation;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.InvitationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationMemberRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.InvitationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BrickAiService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvitationServiceTests {

    private @Mock InvitationRepository invitationRepository;
    private @Mock EmailService emailService;
    private @Mock UserService userService;
    private @Mock RenovationMemberRepository renovationMemberRepository;
    private @Mock RenovationService renovationService;
    private @Mock ChatChannelService chatChannelService;
    private @Mock BrickAiService brickAiService;
    private @InjectMocks InvitationService invitationService;

    private @Mock Renovation renovation;
    private @Mock User owner;

    @BeforeEach
    void setup() {

    }

    @Test
    void findUserAutoCompletionMatches_GivenMatchingMemberUser_ThenReturnMemberDetails() {
        String search = "";
        User susan = mock(User.class);
        RenovationMember susanMember = mock(RenovationMember.class);
        when(susan.getId()).thenReturn(10L);
        when(susanMember.getUser()).thenReturn(susan);
        when(renovation.getMembers()).thenReturn(Set.of(susanMember));

        when(renovationMemberRepository.findCollaboratorsInRenovations(owner, search)).thenReturn(List.of(susan));
        when(invitationRepository.findByRenovationOwner(owner, search)).thenReturn(List.of());

        List<PublicUserDetailsRenovation> expectedDetails = List.of(
                new PublicUserDetailsRenovation(susan, true, false, renovation.getId())
        );
        List<PublicUserDetailsRenovation> details = invitationService.findUserAutoCompletionMatches(owner, renovation, search);
        assertEquals(1, details.size());
        assertTrue(details.containsAll(expectedDetails));
    }

    @Test
    void findUserAutoCompletionMatches_GivenMatchingInvitedUser_ThenReturnsInvitationDetails() {
        String search = "";
        User susan = mock(User.class);
        Invitation susanInvitation = mock(Invitation.class);
        when(susan.getId()).thenReturn(10L);
        when(susan.getFname()).thenReturn("Susan");
        when(susan.getLname()).thenReturn("Susanson");
        when(susanInvitation.getRenovation()).thenReturn(renovation);
        when(susanInvitation.getUser()).thenReturn(susan);
        when(renovation.getMembers()).thenReturn(Set.of());

        when(renovationMemberRepository.findCollaboratorsInRenovations(owner, search)).thenReturn(List.of());
        when(invitationRepository.findByRenovationOwner(owner, search)).thenReturn(List.of(susanInvitation));

        List<PublicUserDetailsRenovation> expectedDetails = List.of(
                new PublicUserDetailsRenovation(susan, false, true, renovation.getId())
        );
        List<PublicUserDetailsRenovation> details = invitationService.findUserAutoCompletionMatches(owner, renovation, search);
        assertEquals(1, details.size());
        assertTrue(details.containsAll(expectedDetails));
    }

    @Test
    void findUserAutoCompletionMatches_GivenMatchingForThisRenovationAndOtherRenovation_ThenThisRenovationInvitationDetailsIsReturned() {
        String search = "";
        User susan = mock(User.class);
        RenovationMember susanMember = mock(RenovationMember.class);
        Invitation susanInvitation = mock(Invitation.class);
        Renovation otherRenovation = mock(Renovation.class);
        when(susan.getId()).thenReturn(10L);
        when(susan.getFname()).thenReturn("Susan");
        when(susan.getLname()).thenReturn("Susanson");
        when(susanMember.getUser()).thenReturn(susan);
        when(susanInvitation.getRenovation()).thenReturn(otherRenovation);
        when(susanInvitation.getUser()).thenReturn(susan);
        when(renovation.getMembers()).thenReturn(Set.of(susanMember));
        when(otherRenovation.getId()).thenReturn(20L);

        when(renovationMemberRepository.findCollaboratorsInRenovations(owner, search)).thenReturn(List.of(susan));
        when(invitationRepository.findByRenovationOwner(owner, search)).thenReturn(List.of(susanInvitation));

        List<PublicUserDetailsRenovation> expectedDetails = List.of(
                new PublicUserDetailsRenovation(susan, true, false, renovation.getId())
        );
        List<PublicUserDetailsRenovation> details = invitationService.findUserAutoCompletionMatches(owner, renovation, search);
        assertEquals(1, details.size());
        assertTrue(details.containsAll(expectedDetails));
    }

    @Test
    void acceptInvitationsPendingRegistration_GivenPendingRegistrations_ThenMarksThemAsAccepted() throws Exception {
        String email = "test@test.com";
        Invitation invitation = mock(Invitation.class);
        Renovation renovation = mock(Renovation.class);
        User user = mock(User.class);
        List<Invitation> invitations = List.of(invitation);

        when(invitationRepository.findByEmailAndAcceptedPendingRegistrationIsTrue(email))
                .thenReturn(invitations);
        when(invitation.getRenovation()).thenReturn(renovation);
        when(renovation.getOwner()).thenReturn(user);

        invitationService.acceptInvitationsPendingRegistration(email);

        verify(invitation).acceptInvitation();
        verify(invitationRepository).save(invitation);
    }

    @Test
    void setInvitationsPendingRegistrationToFalse_GivenPendingRegistrations_ThenMarksThemAsNotPendingAccepted() {
        String email = "test@test.com";
        Invitation invitation = mock(Invitation.class);
        List<Invitation> invitations = List.of(invitation);
        when(invitationRepository.findByEmailAndAcceptedPendingRegistrationIsTrue(email))
                .thenReturn(invitations);

        invitationService.setInvitationsPendingRegistrationToFalse(email);

        verify(invitation).setAcceptedPendingRegistration(false);
        verify(invitationRepository).saveAll(invitations);
    }
}
