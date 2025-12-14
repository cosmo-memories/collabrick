package nz.ac.canterbury.seng302.homehelper.integration.repository;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationMemberRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Transactional
public class RenovationMemberRepositoryTests {

    @Autowired
    private RenovationMemberRepository renovationMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    private Renovation renovationA, renovationB, renovationC;

    private User userA, userB, userC, userD;

    @BeforeEach
    void setup() {
        userA = createUser("a@gmail.com", "Jane", "Smith");
        userB = createUser("b@gmail.com", "Bob", "Doe");
        userC = createUser("c@gmail.com", "Jerry", "James");
        userD = createUser("d@gmail.com", "David", "Williamson");

        renovationA = createRenovation("RenovationA", userA);
        renovationA.addMember(userB, RenovationMemberRole.MEMBER);

        renovationB = createRenovation("RenovationB", userB);
        renovationB.addMember(userA, RenovationMemberRole.MEMBER);
        renovationB.addMember(userD, RenovationMemberRole.MEMBER);

        renovationC = createRenovation("RenovationC", userC);
        renovationC.addMember(userB, RenovationMemberRole.MEMBER);
        renovationC.addMember(userD, RenovationMemberRole.MEMBER);

    }

    @Test
    void testFindMatchingKnownUsers_withEmptyQuery_returnsAllOtherMembers() {
        // userA has a reno that contains userB
        // userA is in a renovation with userA
        List<User> users = renovationMemberRepository.findCollaboratorsInRenovations(userA, "");
        assertEquals(2, users.size());
        assertTrue(users.contains(userB));
        assertTrue(users.contains(userD));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "b",
            "B@gmail",
            "b@gmail.com",
            "B@GMAIL.COM"
    })
    void testFindMatchingKnownUsers_withEmailMatch_returnsAllOtherMembers(String query) {
        // userA has a reno that contains userB
        // userA is in a renovation with userA
        List<User> users = renovationMemberRepository.findCollaboratorsInRenovations(userA, query);
        assertEquals(1, users.size());
        assertTrue(users.contains(userB));
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "vaD",
            "hamburger",
            "frog",
            "Fred Davidson",
            "William Davidson"
    })
    void testFindMatchingKnownUsers_withNoMatch_returnsAllOtherMembers(String query) {
        // userA has a reno that contains userB
        // userA is in a renovation with userA
        List<User> users = renovationMemberRepository.findCollaboratorsInRenovations(userA, query);
        assertEquals(0, users.size());
    }

    @Test
    void testFindMatchingKnownUsers_withNoRenovations_returnsEmptyList() {
        User user = createUser("e@gmail.com", "Emma", "Stone");
        List<User> users = renovationMemberRepository.findCollaboratorsInRenovations(user, "");
        assertEquals(0, users.size());
    }

    @Test
    void testFindMatchingKnownUsers_withNoCollaboratorsInRenovation_returnsEmptyList() {
        User user = createUser("e@gmail.com", "Emma", "Stone");
        createRenovation("Emma's Renovation", user);

        List<User> users = renovationMemberRepository.findCollaboratorsInRenovations(user, "");
        assertEquals(0, users.size());
    }

    private Renovation createRenovation(String name, User owner) {
        Renovation renovation = new Renovation(name, "");
        renovation.setOwner(owner);
        return renovationRepository.save(renovation);
    }

    private User createUser(String email, String firstName, String lastName) {
        User user = new User(firstName, lastName, email, "password", "password");
        return userRepository.save(user);
    }
}
