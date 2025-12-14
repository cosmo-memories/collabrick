package nz.ac.canterbury.seng302.homehelper.unit.repository;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.activity.ActivityType;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole;
import nz.ac.canterbury.seng302.homehelper.repository.activity.ActivityRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationMemberRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ActivityRepository SQL query.
 */
@DataJpaTest
@Transactional
class ActivityRepositoryTests {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private RenovationMemberRepository renovationMemberRepository;

    private User owner;
    private User member;
    private User nonMember;
    private Renovation renovation;

    @BeforeEach
    void setUp() {

        owner = createAndSaveUser("John", "Owner", "john@example.com", "Abc123!!");
        member = createAndSaveUser("Jane", "Member", "jane@example.com", "Abc123!!");
        nonMember = createAndSaveUser("Bob", "NonMember", "bob@example.com", "Abc123!!");

        renovation = createAndSaveRenovation("Kitchen Renovation", owner);

        createAndSaveRenovationMember(renovation, owner, RenovationMemberRole.OWNER);
        createAndSaveRenovationMember(renovation, member, RenovationMemberRole.MEMBER);
    }

    @Test
    void testOwnerSeesAllUpdatesIncludingInviteResponses() {
        LiveUpdate taskUpdate = createAndSaveLiveUpdate(
                renovation,
                member,
                ActivityType.TASK_ADDED,
                Instant.now().minus(1, ChronoUnit.HOURS)
        );

        LiveUpdate inviteAccepted = createAndSaveLiveUpdate(
                renovation,
                member,
                ActivityType.INVITE_ACCEPTED,
                Instant.now().minus(2, ChronoUnit.HOURS)
        );

        LiveUpdate inviteDeclined = createAndSaveLiveUpdate(
                renovation,
                member,
                ActivityType.INVITE_DECLINED,
                Instant.now().minus(3, ChronoUnit.HOURS)
        );

        List<LiveUpdate> ownerUpdates = activityRepository.findLast10UpdatesForUser(owner.getId());

        assertThat(ownerUpdates).hasSize(3);
        assertThat(ownerUpdates).contains(taskUpdate, inviteAccepted, inviteDeclined);
    }

    @Test
    void testMemberDoesNotSeeInviteResponses() {
        LiveUpdate taskUpdate = createAndSaveLiveUpdate(
                renovation,
                owner,
                ActivityType.TASK_ADDED,
                Instant.now().minus(1, ChronoUnit.HOURS)
        );

        LiveUpdate expenseUpdate = createAndSaveLiveUpdate(
                renovation,
                owner,
                ActivityType.EXPENSE_ADDED,
                Instant.now().minus(2, ChronoUnit.HOURS)
        );

        LiveUpdate inviteAccepted = createAndSaveLiveUpdate(
                renovation,
                owner,
                ActivityType.INVITE_ACCEPTED,
                Instant.now().minus(3, ChronoUnit.HOURS)
        );

        LiveUpdate inviteDeclined = createAndSaveLiveUpdate(
                renovation,
                owner,
                ActivityType.INVITE_DECLINED,
                Instant.now().minus(4, ChronoUnit.HOURS)
        );

        List<LiveUpdate> memberUpdates = activityRepository.findLast10UpdatesForUser(member.getId());

        assertThat(memberUpdates).hasSize(2);
        assertThat(memberUpdates).contains(taskUpdate, expenseUpdate);
        assertThat(memberUpdates).doesNotContain(inviteAccepted, inviteDeclined);
    }

    @Test
    void testNonMemberSeesNoUpdates() {
        createAndSaveLiveUpdate(
                renovation,
                owner,
                ActivityType.TASK_ADDED,
                Instant.now().minus(1, ChronoUnit.HOURS)
        );

        createAndSaveLiveUpdate(
                renovation,
                member,
                ActivityType.EXPENSE_ADDED,
                Instant.now().minus(2, ChronoUnit.HOURS)
        );

        List<LiveUpdate> nonMemberUpdates = activityRepository.findLast10UpdatesForUser(nonMember.getId());

        assertThat(nonMemberUpdates).isEmpty();
    }

    @Test
    void testUpdatesOrderedByTimestampDescending() {
        LiveUpdate oldUpdate = createAndSaveLiveUpdate(
                renovation,
                owner,
                ActivityType.TASK_ADDED,
                Instant.now().minus(5, ChronoUnit.DAYS)
        );

        LiveUpdate recentUpdate = createAndSaveLiveUpdate(
                renovation,
                owner,
                ActivityType.TASK_CHANGED_FROM_COMPLETED,
                Instant.now().minus(1, ChronoUnit.HOURS)
        );

        LiveUpdate middleUpdate = createAndSaveLiveUpdate(
                renovation,
                owner,
                ActivityType.EXPENSE_ADDED,
                Instant.now().minus(2, ChronoUnit.DAYS)
        );

        List<LiveUpdate> updates = activityRepository.findLast10UpdatesForUser(owner.getId());

        assertThat(updates).hasSize(3);
        assertThat(updates.get(0)).isEqualTo(recentUpdate);
        assertThat(updates.get(1)).isEqualTo(middleUpdate);
        assertThat(updates.get(2)).isEqualTo(oldUpdate);
    }

    @Test
    void testLimitOf10Updates() {
        for (int i = 0; i < 15; i++) {
            createAndSaveLiveUpdate(
                    renovation,
                    owner,
                    ActivityType.TASK_ADDED,
                    Instant.now().minus(i, ChronoUnit.HOURS)
            );
        }

        List<LiveUpdate> updates = activityRepository.findLast10UpdatesForUser(owner.getId());

        assertThat(updates).hasSize(10);
    }

    @Test
    void testMultipleRenovationsOnlyReturnsRelevant() {
        // Create second renovation with different members
        User otherOwner = createAndSaveUser("Alice", "Other", "alice@example.com", "Abc123!!");
        Renovation otherRenovation = createAndSaveRenovation("Bathroom Renovation", otherOwner);
        createAndSaveRenovationMember(otherRenovation, otherOwner, RenovationMemberRole.OWNER);

        LiveUpdate relevantUpdate = createAndSaveLiveUpdate(
                renovation,
                owner,
                ActivityType.TASK_ADDED,
                Instant.now().minus(1, ChronoUnit.HOURS)
        );

        LiveUpdate irrelevantUpdate = createAndSaveLiveUpdate(
                otherRenovation,
                otherOwner,
                ActivityType.EXPENSE_ADDED,
                Instant.now().minus(2, ChronoUnit.HOURS)
        );

        List<LiveUpdate> memberUpdates = activityRepository.findLast10UpdatesForUser(member.getId());

        assertThat(memberUpdates).hasSize(1);
        assertThat(memberUpdates).contains(relevantUpdate);
        assertThat(memberUpdates).doesNotContain(irrelevantUpdate);
    }

    @Test
    void testAllTaskStateChangeActivities() {
        ActivityType[] taskStateChanges = {
                ActivityType.TASK_CHANGED_FROM_NOT_STARTED,
                ActivityType.TASK_CHANGED_FROM_IN_PROGRESS,
                ActivityType.TASK_CHANGED_FROM_COMPLETED,
                ActivityType.TASK_CHANGED_FROM_CANCELLED,
                ActivityType.TASK_CHANGED_FROM_BLOCKED
        };

        for (ActivityType activityType : taskStateChanges) {
            createAndSaveLiveUpdate(
                    renovation,
                    owner,
                    activityType,
                    Instant.now().minus(1, ChronoUnit.HOURS)
            );
        }

        List<LiveUpdate> memberUpdates = activityRepository.findLast10UpdatesForUser(member.getId());

        assertThat(memberUpdates).hasSize(taskStateChanges.length);
        for (LiveUpdate update : memberUpdates) {
            assertThat(taskStateChanges).contains(update.getActivityType());
        }
    }


    private User createAndSaveUser(String firstName, String lastName, String email, String password) {
        User user = new User();
        user.setFname(firstName);
        user.setLname(lastName);
        user.setEmail(email);
        user.setPassword(password);
        return userRepository.save(user);
    }

    private Renovation createAndSaveRenovation(String name, User owner) {
        Renovation renovation = new Renovation(name, "test description");
        renovation.setOwner(owner);
        return renovationRepository.save(renovation);
    }

    private RenovationMember createAndSaveRenovationMember(Renovation renovation, User user, RenovationMemberRole role) {
        RenovationMember member = new RenovationMember(renovation, user, role);
        return renovationMemberRepository.save(member);
    }

    private LiveUpdate createAndSaveLiveUpdate(Renovation renovation, User user,
                                               ActivityType activityType, Instant timestamp) {
        LiveUpdate liveUpdate = new LiveUpdate(user, renovation, activityType);
        liveUpdate.setTimestamp(timestamp);
        return activityRepository.save(liveUpdate);
    }
}