package nz.ac.canterbury.seng302.homehelper.integration.controller.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole;
import nz.ac.canterbury.seng302.homehelper.model.user.PublicUserDetailsRenovation;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class InvitationAutoCompleteIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RenovationRepository renovationRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Corey", "Hines", "corey@gmail.com", "password", "password");
        userRepo.save(user);
    }

    @Test
    void matchingKnownUsers_emptyQueryUserApartOfRenovation_returnsUserWithIsMemberTrue() throws Exception {
        User userA = makeUser("Brendan", "Jerry", "brendanj@gmail.com");
        Renovation renoA = makeRenovation("Office", user);
        renoA.addMember(userA, RenovationMemberRole.MEMBER);

        MvcResult result = mockMvc.perform(get("/invitation/user-matching")
                        .param("renovationId", String.valueOf(renoA.getId()))
                        .with(user(String.valueOf(user.getId()))
                                .password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
        List<PublicUserDetailsRenovation> members = extractMembersFromResult(result);

        PublicUserDetailsRenovation expectedMember = new PublicUserDetailsRenovation(userA, true, false, renoA.getId());
        assertEquals(1, members.size());
        assertTrue(members.contains(expectedMember));
    }

    @Test
    void matchingKnownUsers_emptyQueryUserNotApartOfRenovation_returnsUserWithIsMemberFalse() throws Exception {
        User userA = makeUser("Brendan", "Jerry", "brendanj@gmail.com");
        Renovation renoA = makeRenovation("Office", user);
        Renovation renoB = makeRenovation("Home", user);
        renoB.addMember(userA, RenovationMemberRole.MEMBER);

        MvcResult result = mockMvc.perform(get("/invitation/user-matching")
                        .param("renovationId", String.valueOf(renoA.getId()))
                        .with(user(String.valueOf(user.getId()))
                                .password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
        List<PublicUserDetailsRenovation> members = extractMembersFromResult(result);

        PublicUserDetailsRenovation expectedMember = new PublicUserDetailsRenovation(userA, false, false, renoA.getId());
        assertEquals(1, members.size());
        assertTrue(members.contains(expectedMember));
    }


    @Test
    void matchingKnownUsers_emptyQuery_returnsAllCollaboratedUsers() throws Exception {
        User userA = makeUser("Brendan", "Jerry", "brendanj@gmail.com");
        User userB = makeUser("Angel", "Cheeto", "angelcheeto@gmail.com");
        User userC = makeUser("Bailey", "Bales", "bailey@gmail.com");
        User userD = makeUser("Greg", "Smith", "smithy@gmail.com");
        Renovation renoA = makeRenovation("Office", user);
        Renovation renoB = makeRenovation("House", user);
        renoA.addMember(userA, RenovationMemberRole.MEMBER);
        renoA.addMember(userB, RenovationMemberRole.MEMBER);
        renoB.addMember(userC, RenovationMemberRole.MEMBER);
        renoB.addMember(userA, RenovationMemberRole.MEMBER);

        MvcResult result = mockMvc.perform(get("/invitation/user-matching")
                        .param("renovationId", String.valueOf(renoA.getId()))
                        .with(user(String.valueOf(user.getId()))
                                .password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
        List<PublicUserDetailsRenovation> members = extractMembersFromResult(result);

        // userA, userB are members of the users office renovation
        // userC is a member of the users house renovation
        // userD is not apart of the users renovations
        List<PublicUserDetailsRenovation> expectedUsers = List.of(
                new PublicUserDetailsRenovation(userA, true, false, renoA.getId()),
                new PublicUserDetailsRenovation(userB, true, false, renoA.getId()),
                new PublicUserDetailsRenovation(userC, false, false, renoA.getId())
        );
        assertEquals(3, members.size());
        assertTrue(members.containsAll(expectedUsers));
    }

    @Test
    void matchingKnownUsers_queryByEmail_returnsMatchingUsers() throws Exception {
        User userA = makeUser("Brendan", "Jerry", "brendanj@gmail.com");
        User userB = makeUser("Angel", "Cheeto", "angelcheeto@gmail.com");
        Renovation renovation = makeRenovation("Office", user);
        renovation.addMember(userA, RenovationMemberRole.MEMBER);
        renovation.addMember(userB, RenovationMemberRole.MEMBER);

        MvcResult result = mockMvc.perform(get("/invitation/user-matching")
                        .param("renovationId", String.valueOf(renovation.getId()))
                        .param("search", "brendanJ@GMAIL")
                        .with(user(String.valueOf(user.getId()))
                                .password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
        List<PublicUserDetailsRenovation> members = extractMembersFromResult(result);

        assertEquals(1, members.size());
        assertEquals(new PublicUserDetailsRenovation(userA, true, false, renovation.getId()), members.getFirst());
    }

    @Test
    void matchingKnownUsers_queryNoMatch_returnsEmptyList() throws Exception {
        User userA = makeUser("Angel", "Cheeto", "angel@gmail.com");
        Renovation renovation = makeRenovation("Kitchen", user);
        renovation.addMember(userA, RenovationMemberRole.MEMBER);

        MvcResult result = mockMvc.perform(get("/invitation/user-matching")
                        .param("renovationId", String.valueOf(renovation.getId()))
                        .param("search", "unknown-user")
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();

        List<PublicUserDetailsRenovation> members = extractMembersFromResult(result);
        assertEquals(0, members.size());
    }

    @Test
    void matchingKnownUsers_renovationWithNoOtherMembers_returnsEmptyList() throws Exception {
        Renovation renovation = makeRenovation("SoloProject", user);

        MvcResult result = mockMvc.perform(get("/invitation/user-matching")
                        .param("renovationId", String.valueOf(renovation.getId()))
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();

        List<PublicUserDetailsRenovation> members = extractMembersFromResult(result);
        assertEquals(0, members.size());
    }

    @Test
    void matchingKnownUsers_userDoesntOwnRenovation_returnsMembersOnCommonRenovations() throws Exception {
        User userA = makeUser("Brendan", "Jerry", "brendanj@gmail.com");
        User userB = makeUser("Angel", "Cheeto", "angelcheeto@gmail.com");
        User userC = makeUser("Bailey", "Bales", "bailey@gmail.com");
        User userD = makeUser("Greg", "Smith", "smithy@gmail.com");
        Renovation renoA = makeRenovation("University Upgrade", user);
        Renovation renoB = makeRenovation("Man Cave", user);
        renoA.addMember(userA, RenovationMemberRole.MEMBER);
        renoA.addMember(userB, RenovationMemberRole.MEMBER);
        renoB.addMember(userC, RenovationMemberRole.MEMBER);
        renoB.addMember(userA, RenovationMemberRole.MEMBER);

        MvcResult result = mockMvc.perform(get("/invitation/user-matching")
                        .param("renovationId", String.valueOf(renoA.getId()))
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();

        List<PublicUserDetailsRenovation> expectedUsers = List.of(
                new PublicUserDetailsRenovation(userA, true, false, renoA.getId()),
                new PublicUserDetailsRenovation(userB, true, false, renoA.getId()),
                new PublicUserDetailsRenovation(userC, false, false, renoA.getId())
        );

        List<PublicUserDetailsRenovation> members = extractMembersFromResult(result);
        assertEquals(3, members.size());
        assertTrue(members.containsAll(expectedUsers));
    }

    @Test
    void matchingKnownUsers_userDoesntOwnRenovationAndHasEmailSearch_returnsMembersOnCommonRenovations() throws Exception {
        User userA = makeUser("Brendan", "Jerry", "brendanj@gmail.com");
        User userB = makeUser("Angel", "Cheeto", "angelcheeto@gmail.com");
        User userC = makeUser("Bailey", "Bales", "bailey@gmail.com");
        User userD = makeUser("Greg", "Smith", "smithy@gmail.com");
        Renovation renoA = makeRenovation("Golf course", user);
        Renovation renoB = makeRenovation("Suspicious Warehouse", user);
        renoA.addMember(userA, RenovationMemberRole.MEMBER);
        renoA.addMember(userB, RenovationMemberRole.MEMBER);
        renoB.addMember(userC, RenovationMemberRole.MEMBER);
        renoB.addMember(userA, RenovationMemberRole.MEMBER);

        MvcResult result = mockMvc.perform(get("/invitation/user-matching")
                        .param("renovationId", String.valueOf(renoA.getId()))
                        .with(user(String.valueOf(userA.getId())).password(userA.getPassword()).roles("USER"))
                        .param("search", "corey@gmail.com"))
                .andExpect(status().isOk())
                .andReturn();

        List<PublicUserDetailsRenovation> members = extractMembersFromResult(result);
        assertEquals(1, members.size());
        assertTrue(members.contains(new PublicUserDetailsRenovation(user, true, false, renoA.getId())));
    }

    Renovation makeRenovation(String name, User user) {
        Renovation reno = new Renovation(name, "");
        reno.setOwner(user);
        renovationRepo.save(reno);
        return reno;
    }

    User makeUser(String fName, String lName, String email) {
        User user = new User(fName, lName, email, "password", "password");
        userRepo.save(user);
        return user;
    }

    List<PublicUserDetailsRenovation> extractMembersFromResult(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        JsonNode arrayNode = objectMapper.readTree(content);
        List<PublicUserDetailsRenovation> list = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            long id = node.get("id").asLong();
            String firstName = node.get("firstName").asText();
            String lastName = node.get("lastName").asText();
            String email = node.get("email").asText();
            String image = node.get("image").asText();
            boolean isMember = node.get("member").asBoolean();     // or "isMember" depending on JSON
            boolean isInvited = node.get("invited").asBoolean();   // or "isInvited"
            long renovationId = node.get("renovationId").asLong();
            list.add(new PublicUserDetailsRenovation(id, firstName, lastName, email, image, isMember, isInvited, renovationId));
        }
        return list;
    }
}