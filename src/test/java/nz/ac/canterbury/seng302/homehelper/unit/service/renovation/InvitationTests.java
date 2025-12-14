package nz.ac.canterbury.seng302.homehelper.unit.service.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.InvitationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class InvitationTests {
    private @Mock Renovation renovation;
    private @Mock User owner;

    @Test
    void acceptInvitation_WhenInvitationPending_InvitationStateAccepted() throws Exception {
        Invitation invitation = new Invitation(owner, renovation);
        invitation.acceptInvitation();
        assertSame(InvitationStatus.ACCEPTED, invitation.getInvitationStatus());
    }

    @ParameterizedTest
    // Enum source was a discovery thanks to ChatGPT
    @EnumSource(
            value = InvitationStatus.class,
            names = "PENDING", // states to exclude
            mode = EnumSource.Mode.EXCLUDE
    )
    void acceptInvitation_WhenInvitationResolved_ExceptionIsThrown(InvitationStatus invitationStatus) throws Exception {
        Invitation invitation = new Invitation(owner, renovation);
        invitation.setInvitationStatus(invitationStatus);

        assertThrows(Exception.class, invitation::acceptInvitation);
    }

    @Test
    void declineInvitation_WhenInvitationPending_InvitationStateDeclined() throws Exception {
        Invitation invitation = new Invitation(owner, renovation);
        invitation.declineInvitation();
        assertSame(InvitationStatus.DECLINED, invitation.getInvitationStatus());
    }

    @ParameterizedTest
    @EnumSource(
            value = InvitationStatus.class,
            names = "PENDING",
            mode = EnumSource.Mode.EXCLUDE
    )
    void declineInvitation_WhenInvitationResolved_ExceptionIsThrown(InvitationStatus invitationStatus) throws Exception {
        Invitation invitation = new Invitation(owner, renovation);
        invitation.setInvitationStatus(invitationStatus);

        assertThrows(Exception.class, invitation::declineInvitation);
    }
}
