package nz.ac.canterbury.seng302.homehelper.unit.service.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationMemberRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.InvitationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationMemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RenovationMemberServiceTests {

    @Mock
    RenovationMemberRepository renovationMemberRepository;

    @InjectMocks
    RenovationMemberService renovationMemberService;

    @Mock
    InvitationService invitationService;

    @Mock
    Renovation renovation;

    private User userWithId(long id) {
        User u = mock(User.class);
        u.setId(id);
        return u;
    }

    @Test
    void checkMembership_userIsMember_returnsTrue() throws NoResourceFoundException {
        User user = userWithId(1L);

        when(renovationMemberRepository.checkMembership(user, renovation))
                .thenReturn(List.of(user));

        boolean result = renovationMemberService.checkMembership(user, renovation);

        assertTrue(result);
        verify(renovationMemberRepository).checkMembership(user, renovation);
    }


    @Test
    void checkMembership_userNotMember_throwsNoResourceFoundExc() throws NoResourceFoundException {
        User user = userWithId(1L);

        when(renovationMemberRepository.checkMembership(user, renovation))
                .thenReturn(List.of());


        assertThrows(NoResourceFoundException.class,
                () -> renovationMemberService.checkMembership(user, renovation));

        verify(renovationMemberRepository).checkMembership(user, renovation);
    }

    @Test
    void deleteRenovationMember_roleIsOwner_throwsException() {
        RenovationMember owner = mock(RenovationMember.class);
        when(owner.getRole()).thenReturn(RenovationMemberRole.OWNER);

        assertThrows(Exception.class,
                () -> renovationMemberService.deleteRenovationMember(owner));
        verify(renovationMemberRepository, never()).delete(any());
    }
}
