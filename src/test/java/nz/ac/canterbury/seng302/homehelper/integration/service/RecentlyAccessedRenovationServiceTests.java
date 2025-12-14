package nz.ac.canterbury.seng302.homehelper.integration.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RecentlyAccessedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RecentlyAccessedRenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RecentlyAccessedRenovationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@ExtendWith(MockitoExtension.class)
public class RecentlyAccessedRenovationServiceTests {

    @Autowired
    private RecentlyAccessedRenovationService recentlyAccessedRenovationService;

    @Autowired
    private RecentlyAccessedRenovationRepository recentlyAccessedRenovationRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private UserRepository userRepository;

    private Renovation privateRenovation;

    private Renovation publicRenovation;

    private User ownerUser;

    private User memberUser;

    private User nonMemberUser;

    @BeforeEach
    void setUp() {
        recentlyAccessedRenovationRepository.deleteAll();
        renovationRepository.deleteAll();
        userRepository.deleteAll();

        ownerUser = new User("Steve","Minecraft","steve.minecraft@gmail.com", "Abc123!!", "Abc123!!");
        ownerUser.setActivated(true);
        userRepository.save(ownerUser);

        memberUser = new User("Alex", "Minecraft", "alex.minecraft@gmail.com", "Abc123!!", "Abc123!!");
        memberUser.setActivated(true);
        userRepository.save(memberUser);

        nonMemberUser = new User("Villager", "Minecraft", "villager@gmail.com", "Abc123!!", "Abc123!!");
        nonMemberUser.setActivated(true);
        userRepository.save(nonMemberUser);

        privateRenovation = new Renovation("Minecraft Dirt Hut", "3x4x3 dirt shack, first night");
        privateRenovation.setOwner(ownerUser);
        privateRenovation.addMember(memberUser, RenovationMemberRole.MEMBER);
        renovationRepository.save(privateRenovation);

        publicRenovation = new Renovation("Woodland Mansion", "x=3400, y=70, z=-10023");
        publicRenovation.setOwner(ownerUser);
        publicRenovation.setPublic(true);
        publicRenovation.addMember(memberUser, RenovationMemberRole.MEMBER);
        renovationRepository.save(publicRenovation);

    }

    @Test
    void ownerHasNotAccessedPrivateRenovationBefore_ownerAccessesPrivateRenovation_accessEntryLoggedForOwner() {
        assertEquals(Optional.empty(), recentlyAccessedRenovationRepository.findByRenovationAndUser(privateRenovation, ownerUser));
        LocalDateTime beforeTime = LocalDateTime.now();
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(privateRenovation, ownerUser);
        LocalDateTime afterTime = LocalDateTime.now();
        Optional<RecentlyAccessedRenovation> rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(privateRenovation, ownerUser);
        assertTrue(rarOpt.isPresent());
        RecentlyAccessedRenovation rar = rarOpt.get();
        assertFalse(rar.getTimeAccessed().isBefore(beforeTime));
        assertFalse(rar.getTimeAccessed().isAfter(afterTime));
    }

    @Test
    void memberHasNotAccessedPrivateRenovationBefore_memberAccessesPrivateRenovation_accessEntryLoggedForMember() {
        assertEquals(Optional.empty(), recentlyAccessedRenovationRepository.findByRenovationAndUser(privateRenovation, memberUser));
        LocalDateTime beforeTime = LocalDateTime.now();
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(privateRenovation, memberUser);
        LocalDateTime afterTime = LocalDateTime.now();
        Optional<RecentlyAccessedRenovation> rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(privateRenovation, memberUser);
        assertTrue(rarOpt.isPresent());
        RecentlyAccessedRenovation rar = rarOpt.get();
        assertFalse(rar.getTimeAccessed().isBefore(beforeTime));
        assertFalse(rar.getTimeAccessed().isAfter(afterTime));
    }

    @Test
    void ownerHasNotAccessedPublicRenovationBefore_ownerAccessesPublicRenovation_accessEntryLoggedForOwner() {
        assertEquals(Optional.empty(), recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, ownerUser));
        LocalDateTime beforeTime = LocalDateTime.now();
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(publicRenovation, ownerUser);
        LocalDateTime afterTime = LocalDateTime.now();
        Optional<RecentlyAccessedRenovation> rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, ownerUser);
        assertTrue(rarOpt.isPresent());
        RecentlyAccessedRenovation rar = rarOpt.get();
        assertFalse(rar.getTimeAccessed().isBefore(beforeTime));
        assertFalse(rar.getTimeAccessed().isAfter(afterTime));
    }

    @Test
    void memberHasNotAccessedPublicRenovationBefore_memberAccessesPublicRenovation_accessEntryLoggedForMember() {
        assertEquals(Optional.empty(), recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, memberUser));
        LocalDateTime beforeTime = LocalDateTime.now();
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(publicRenovation, memberUser);
        LocalDateTime afterTime = LocalDateTime.now();
        Optional<RecentlyAccessedRenovation> rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, memberUser);
        assertTrue(rarOpt.isPresent());
        RecentlyAccessedRenovation rar = rarOpt.get();
        assertFalse(rar.getTimeAccessed().isBefore(beforeTime));
        assertFalse(rar.getTimeAccessed().isAfter(afterTime));
    }

    @Test
    void nonMemberHasNotAccessedPublicRenovationBefore_nonMemberAccessesPublicRenovation_accessEntryLoggedForNonMember() {
        assertEquals(Optional.empty(), recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, nonMemberUser));
        LocalDateTime beforeTime = LocalDateTime.now();
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(publicRenovation, nonMemberUser);
        LocalDateTime afterTime = LocalDateTime.now();
        Optional<RecentlyAccessedRenovation> rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, nonMemberUser);
        assertTrue(rarOpt.isPresent());
        RecentlyAccessedRenovation rar = rarOpt.get();
        assertFalse(rar.getTimeAccessed().isBefore(beforeTime));
        assertFalse(rar.getTimeAccessed().isAfter(afterTime));
    }

    @Test
    void ownerHasAccessedPrivateRenovationBefore_ownerAccessesPrivateRenovation_accessEntryUpdatedForOwner() {
        RecentlyAccessedRenovation recentlyAccessedRenovation = new RecentlyAccessedRenovation(ownerUser, privateRenovation);
        recentlyAccessedRenovation.setTimeAccessed(LocalDateTime.of(2025, 9, 17, 15, 30, 0));
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovation);
        Optional<RecentlyAccessedRenovation> rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(privateRenovation, ownerUser);
        assertTrue(rarOpt.isPresent());
        RecentlyAccessedRenovation rar = rarOpt.get();
        assertTrue(rar.getTimeAccessed().isBefore(LocalDateTime.now()));
        LocalDateTime beforeTime = LocalDateTime.now();
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(privateRenovation, ownerUser);
        LocalDateTime afterTime = LocalDateTime.now();
        rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(privateRenovation, ownerUser);
        assertTrue(rarOpt.isPresent());
        rar = rarOpt.get();
        assertFalse(rar.getTimeAccessed().isBefore(beforeTime));
        assertFalse(rar.getTimeAccessed().isAfter(afterTime));
    }

    @Test
    void memberHasAccessedPrivateRenovationBefore_memberAccessesPrivateRenovation_accessEntryUpdatedForMember() {
        RecentlyAccessedRenovation recentlyAccessedRenovation = new RecentlyAccessedRenovation(memberUser, privateRenovation);
        recentlyAccessedRenovation.setTimeAccessed(LocalDateTime.of(2025, 9, 17, 15, 30, 0));
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovation);
        Optional<RecentlyAccessedRenovation> rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(privateRenovation, memberUser);
        assertTrue(rarOpt.isPresent());
        RecentlyAccessedRenovation rar = rarOpt.get();
        assertTrue(rar.getTimeAccessed().isBefore(LocalDateTime.now()));
        LocalDateTime beforeTime = LocalDateTime.now();
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(privateRenovation, memberUser);
        LocalDateTime afterTime = LocalDateTime.now();
        rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(privateRenovation, memberUser);
        assertTrue(rarOpt.isPresent());
        rar = rarOpt.get();
        assertFalse(rar.getTimeAccessed().isBefore(beforeTime));
        assertFalse(rar.getTimeAccessed().isAfter(afterTime));
    }

    @Test
    void ownerHasAccessedPublicRenovationBefore_ownerAccessesPublicRenovation_accessEntryUpdatedForOwner() {
        RecentlyAccessedRenovation recentlyAccessedRenovation = new RecentlyAccessedRenovation(ownerUser, publicRenovation);
        recentlyAccessedRenovation.setTimeAccessed(LocalDateTime.of(2025, 9, 17, 15, 30, 0));
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovation);
        Optional<RecentlyAccessedRenovation> rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, ownerUser);
        assertTrue(rarOpt.isPresent());
        RecentlyAccessedRenovation rar = rarOpt.get();
        assertTrue(rar.getTimeAccessed().isBefore(LocalDateTime.now()));
        LocalDateTime beforeTime = LocalDateTime.now();
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(publicRenovation, ownerUser);
        LocalDateTime afterTime = LocalDateTime.now();
        rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, ownerUser);
        assertTrue(rarOpt.isPresent());
        rar = rarOpt.get();
        assertFalse(rar.getTimeAccessed().isBefore(beforeTime));
        assertFalse(rar.getTimeAccessed().isAfter(afterTime));
    }

    @Test
    void memberHasAccessedPublicRenovationBefore_memberAccessesPublicRenovation_accessEntryUpdatedForMember() {
        RecentlyAccessedRenovation recentlyAccessedRenovation = new RecentlyAccessedRenovation(memberUser, publicRenovation);
        recentlyAccessedRenovation.setTimeAccessed(LocalDateTime.of(2025, 9, 17, 15, 30, 0));
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovation);
        Optional<RecentlyAccessedRenovation> rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, memberUser);
        assertTrue(rarOpt.isPresent());
        RecentlyAccessedRenovation rar = rarOpt.get();
        assertTrue(rar.getTimeAccessed().isBefore(LocalDateTime.now()));
        LocalDateTime beforeTime = LocalDateTime.now();
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(publicRenovation, memberUser);
        LocalDateTime afterTime = LocalDateTime.now();
        rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, memberUser);
        assertTrue(rarOpt.isPresent());
        rar = rarOpt.get();
        assertFalse(rar.getTimeAccessed().isBefore(beforeTime));
        assertFalse(rar.getTimeAccessed().isAfter(afterTime));
    }

    @Test
    void nonMemberHasAccessedPublicRenovationBefore_nonMemberAccessesPublicRenovation_accessEntryUpdatedForNonMember() {
        RecentlyAccessedRenovation recentlyAccessedRenovation = new RecentlyAccessedRenovation(nonMemberUser, publicRenovation);
        recentlyAccessedRenovation.setTimeAccessed(LocalDateTime.of(2025, 9, 17, 15, 30, 0));
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovation);
        Optional<RecentlyAccessedRenovation> rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, nonMemberUser);
        assertTrue(rarOpt.isPresent());
        RecentlyAccessedRenovation rar = rarOpt.get();
        assertTrue(rar.getTimeAccessed().isBefore(LocalDateTime.now()));
        LocalDateTime beforeTime = LocalDateTime.now();
        recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(publicRenovation, nonMemberUser);
        LocalDateTime afterTime = LocalDateTime.now();
        rarOpt = recentlyAccessedRenovationRepository.findByRenovationAndUser(publicRenovation, nonMemberUser);
        assertTrue(rarOpt.isPresent());
        rar = rarOpt.get();
        assertFalse(rar.getTimeAccessed().isBefore(beforeTime));
        assertFalse(rar.getTimeAccessed().isAfter(afterTime));
    }

    @Test
    void deleteAllRecentlyAccessedRenovationsEntries_forAllDifferentUserTypes_forPrivateRenovation() {
        RecentlyAccessedRenovation recentlyAccessedRenovationOwner = new RecentlyAccessedRenovation(ownerUser, privateRenovation);
        RecentlyAccessedRenovation recentlyAccessedRenovationMember = new RecentlyAccessedRenovation(memberUser, privateRenovation);
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovationOwner);
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovationMember);
        assertEquals(2, recentlyAccessedRenovationRepository.findAllRecentlyAccessedRenovationsByRenovationId(privateRenovation.getId()).size());
        recentlyAccessedRenovationService.deleteAllRenovationAccessEntriesForRenovation(privateRenovation.getId());
        assertEquals(0, recentlyAccessedRenovationRepository.findAllRecentlyAccessedRenovationsByRenovationId(privateRenovation.getId()).size());
    }

    @Test
    void deleteAllRecentlyAccessedRenovationsEntries_forAllDifferentUserTypes_forPublicRenovation() {
        RecentlyAccessedRenovation recentlyAccessedRenovationOwner = new RecentlyAccessedRenovation(ownerUser, publicRenovation);
        RecentlyAccessedRenovation recentlyAccessedRenovationMember = new RecentlyAccessedRenovation(memberUser, publicRenovation);
        RecentlyAccessedRenovation recentlyAccessedRenovationNonMember = new RecentlyAccessedRenovation(nonMemberUser, publicRenovation);
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovationOwner);
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovationMember);
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovationNonMember);
        assertEquals(3, recentlyAccessedRenovationRepository.findAllRecentlyAccessedRenovationsByRenovationId(publicRenovation.getId()).size());
        recentlyAccessedRenovationService.deleteAllRenovationAccessEntriesForRenovation(publicRenovation.getId());
        assertEquals(0, recentlyAccessedRenovationRepository.findAllRecentlyAccessedRenovationsByRenovationId(publicRenovation.getId()).size());
    }

    @Test
    void allUsersAccessedPublicRenovation_publicRenovationGetsSetToPrivate_allNonMemberAccessesGetDeleted() {
        RecentlyAccessedRenovation recentlyAccessedRenovationOwner = new RecentlyAccessedRenovation(ownerUser, publicRenovation);
        RecentlyAccessedRenovation recentlyAccessedRenovationMember = new RecentlyAccessedRenovation(memberUser, publicRenovation);
        RecentlyAccessedRenovation recentlyAccessedRenovationNonMember = new RecentlyAccessedRenovation(nonMemberUser, publicRenovation);
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovationOwner);
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovationMember);
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovationNonMember);
        assertEquals(3, recentlyAccessedRenovationRepository.findAllRecentlyAccessedRenovationsByRenovationId(publicRenovation.getId()).size());
        publicRenovation.setPublic(false);
        recentlyAccessedRenovationService.deleteNonMemberAccessesFromPrivateRenovation(publicRenovation.getId(), publicRenovation.getIsPublic());
        assertEquals(2, recentlyAccessedRenovationRepository.findAllRecentlyAccessedRenovationsByRenovationId(publicRenovation.getId()).size());
    }

    @Test
    void memberHasAccessedPrivateRenovation_memberIsRemovedFromPrivateRenovation_memberAccessesGetsDeleted() {
        RecentlyAccessedRenovation recentlyAccessedRenovationMember = new RecentlyAccessedRenovation(memberUser, privateRenovation);
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovationMember);
        assertEquals(1, recentlyAccessedRenovationRepository.findAllRecentlyAccessedRenovationsByRenovationId(privateRenovation.getId()).size());
        recentlyAccessedRenovationService.deleteUserRenovationAccessEntryForPrivateRenovation(privateRenovation.getId(), memberUser.getId());
        assertEquals(0, recentlyAccessedRenovationRepository.findAllRecentlyAccessedRenovationsByRenovationId(privateRenovation.getId()).size());
    }

    @Test
    void memberHasAccessedPublicRenovation_memberIsRemovedFromPublicRenovation_memberAccessesDontGetDeleted() {
        RecentlyAccessedRenovation recentlyAccessedRenovationMember = new RecentlyAccessedRenovation(memberUser, publicRenovation);
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovationMember);
        assertEquals(1, recentlyAccessedRenovationRepository.findAllRecentlyAccessedRenovationsByRenovationId(publicRenovation.getId()).size());
        recentlyAccessedRenovationService.deleteUserRenovationAccessEntryForPrivateRenovation(publicRenovation.getId(), memberUser.getId());
        assertEquals(1, recentlyAccessedRenovationRepository.findAllRecentlyAccessedRenovationsByRenovationId(publicRenovation.getId()).size());
    }
}
