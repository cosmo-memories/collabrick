package nz.ac.canterbury.seng302.homehelper.unit.service.renovation;

import io.cucumber.core.gherkin.Argument;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RecentlyAccessedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RecentlyAccessedRenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RecentlyAccessedRenovationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

public class RecentlyAccessedRenovationServiceTests {

    @Mock
    private RenovationRepository renovationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecentlyAccessedRenovationRepository recentlyAccessedRenovationRepository;

    @InjectMocks
    private RecentlyAccessedRenovationService recentlyAccessedRenovationService;

    private Renovation privateRenovation;

    private Renovation publicRenovation;

    private User ownerUser;

    private User memberUser;

    private User nonMemberUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ownerUser = new User("Steve","Minecraft","steve.minecraft@gmail.com", "Abc123!!", "Abc123!!");
        ownerUser.setId(1L);
        ownerUser.setActivated(true);
        userRepository.save(ownerUser);

        memberUser = new User("Alex", "Minecraft", "alex.minecraft@gmail.com", "Abc123!!", "Abc123!!");
        memberUser.setId(2L);
        memberUser.setActivated(true);

        nonMemberUser = new User("Villager", "Minecraft", "villager@gmail.com", "Abc123!!", "Abc123!!");
        nonMemberUser.setId(3L);
        nonMemberUser.setActivated(true);
        userRepository.save(nonMemberUser);

        privateRenovation = new Renovation("Minecraft Dirt Hut", "3x4x3 dirt shack, first night");
        privateRenovation.setId(1L);
        privateRenovation.setOwner(ownerUser);
        renovationRepository.save(privateRenovation);

        publicRenovation = new Renovation("Woodland Mansion", "x=3400, y=70, z=-10023");
        publicRenovation.setId(2L);
        publicRenovation.setOwner(ownerUser);
        publicRenovation.setPublic(true);
        renovationRepository.save(publicRenovation);

        memberUser.addRenovation(privateRenovation);
        memberUser.addRenovation(publicRenovation);
        userRepository.save(memberUser);

        when(renovationRepository.findById(1L)).thenReturn(java.util.Optional.of(privateRenovation));
        when(renovationRepository.findById(2L)).thenReturn(java.util.Optional.of(publicRenovation));

        when(userRepository.findUserById(1L)).thenReturn(java.util.List.of(ownerUser));
        when(userRepository.findUserById(2L)).thenReturn(java.util.List.of(memberUser));
        when(userRepository.findUserById(3L)).thenReturn(java.util.List.of(nonMemberUser));

    }

    @Test
    void ownerHasNotAccessedPrivateRenovationBefore_ownerAccessesPrivateRenovation_accessEntryLoggedForOwner() {
        when(recentlyAccessedRenovationRepository.findByRenovationAndUser(privateRenovation, ownerUser)).thenReturn(Optional.empty());
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(privateRenovation, ownerUser);
        ArgumentCaptor<RecentlyAccessedRenovation> captor = ArgumentCaptor.forClass(RecentlyAccessedRenovation.class);
        verify(recentlyAccessedRenovationRepository, times(1)).save(captor.capture());
    }

    @Test
    void ownerHasAccessedPrivateRenovationBefore_ownerAccessesPrivateRenovation_accessEntryLoggedForOwner() {
        RecentlyAccessedRenovation recentlyAccessedRenovation = new RecentlyAccessedRenovation(ownerUser, privateRenovation);
        when(recentlyAccessedRenovationRepository.findByRenovationAndUser(privateRenovation, ownerUser)).thenReturn(Optional.of(recentlyAccessedRenovation));
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(privateRenovation, ownerUser);
        verify(recentlyAccessedRenovationRepository, times(1)).save(recentlyAccessedRenovation);
    }

    @Test
    void memberHasNotAccessedPrivateRenovationBefore_memberAccessesPrivateRenovation_accessEntryLoggedForMember() {
        when(recentlyAccessedRenovationRepository.findByRenovationAndUser(privateRenovation, memberUser)).thenReturn(Optional.empty());
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(privateRenovation, memberUser);
        ArgumentCaptor<RecentlyAccessedRenovation> captor = ArgumentCaptor.forClass(RecentlyAccessedRenovation.class);
        verify(recentlyAccessedRenovationRepository, times(1)).save(captor.capture());
    }

    @Test
    void memberHasAccessedPrivateRenovationBefore_memberAccessesPrivateRenovation_accessEntryLoggedForMember() {
        RecentlyAccessedRenovation recentlyAccessedRenovation = new RecentlyAccessedRenovation(memberUser, privateRenovation);
        when(recentlyAccessedRenovationRepository.findByRenovationAndUser(privateRenovation, memberUser)).thenReturn(Optional.of(recentlyAccessedRenovation));
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(privateRenovation, memberUser);
        verify(recentlyAccessedRenovationRepository, times(1)).save(recentlyAccessedRenovation);
    }

    @Test
    void ownerHasNotAccessedPublicRenovationBefore_ownerAccessesPublicRenovation_accessEntryLoggedForOwner() {
        when(recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, ownerUser)).thenReturn(Optional.empty());
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(publicRenovation, ownerUser);
        ArgumentCaptor<RecentlyAccessedRenovation> captor = ArgumentCaptor.forClass(RecentlyAccessedRenovation.class);
        verify(recentlyAccessedRenovationRepository, times(1)).save(captor.capture());
    }

    @Test
    void ownerHasAccessedPublicRenovationBefore_ownerAccessesPublicRenovation_accessEntryLoggedForOwner() {
        RecentlyAccessedRenovation recentlyAccessedRenovation = new RecentlyAccessedRenovation(ownerUser, publicRenovation);
        when(recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, ownerUser)).thenReturn(Optional.of(recentlyAccessedRenovation));
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(publicRenovation, ownerUser);
        verify(recentlyAccessedRenovationRepository, times(1)).save(recentlyAccessedRenovation);
    }

    @Test
    void memberHasNotAccessedPublicRenovationBefore_memberAccessesPublicRenovation_accessEntryLoggedForMember() {
        when(recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, memberUser)).thenReturn(Optional.empty());
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(publicRenovation, memberUser);
        ArgumentCaptor<RecentlyAccessedRenovation> captor = ArgumentCaptor.forClass(RecentlyAccessedRenovation.class);
        verify(recentlyAccessedRenovationRepository, times(1)).save(captor.capture());
    }

    @Test
    void memberHasAccessedPublicRenovationBefore_memberAccessesPublicRenovation_accessEntryLoggedForMember() {
        RecentlyAccessedRenovation recentlyAccessedRenovation = new RecentlyAccessedRenovation(memberUser, publicRenovation);
        when(recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, memberUser)).thenReturn(Optional.of(recentlyAccessedRenovation));
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(publicRenovation, memberUser);
        verify(recentlyAccessedRenovationRepository, times(1)).save(recentlyAccessedRenovation);
    }

    @Test
    void nonMemberHasNotAccessedPublicRenovationBefore_nonMemberAccessesPublicRenovation_accessEntryLoggedForNonMember() {
        when(recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, nonMemberUser)).thenReturn(Optional.empty());
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(publicRenovation, nonMemberUser);
        ArgumentCaptor<RecentlyAccessedRenovation> captor = ArgumentCaptor.forClass(RecentlyAccessedRenovation.class);
        verify(recentlyAccessedRenovationRepository, times(1)).save(captor.capture());
    }

    @Test
    void nonMemberHasAccessedPublicRenovationBefore_nonMemberAccessesPublicRenovation_accessEntryLoggedForNonMember() {
        RecentlyAccessedRenovation recentlyAccessedRenovation = new RecentlyAccessedRenovation(nonMemberUser, publicRenovation);
        when(recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, nonMemberUser)).thenReturn(Optional.of(recentlyAccessedRenovation));
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(publicRenovation, nonMemberUser);
        verify(recentlyAccessedRenovationRepository, times(1)).save(recentlyAccessedRenovation);
    }

    @Test
    void deleteAllRenovationAccessEntriesForRenovation_callsRepositoryOnce() {
        recentlyAccessedRenovationService.deleteAllRenovationAccessEntriesForRenovation(publicRenovation.getId());
        verify(recentlyAccessedRenovationRepository, times(1)).deleteAllRenovationAccessEntriesForRenovation(publicRenovation.getId());
        verifyNoMoreInteractions(recentlyAccessedRenovationRepository);
    }

    @Test
    void deleteNonMemberAccessesFromPrivateRenovation_callsRepositoryOnce() {
        recentlyAccessedRenovationService.deleteNonMemberAccessesFromPrivateRenovation(privateRenovation.getId(), privateRenovation.getIsPublic());
        verify(recentlyAccessedRenovationRepository, times(1)).deleteNonMembersFromPrivateRenovation(privateRenovation.getId());
        verifyNoMoreInteractions(recentlyAccessedRenovationRepository);
    }
}
