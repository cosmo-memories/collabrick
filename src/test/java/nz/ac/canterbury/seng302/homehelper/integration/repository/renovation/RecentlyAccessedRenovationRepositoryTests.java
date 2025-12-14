package nz.ac.canterbury.seng302.homehelper.integration.repository.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.RecentlyAccessedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RecentlyAccessedRenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class RecentlyAccessedRenovationRepositoryTests {

    @Autowired
    private RecentlyAccessedRenovationRepository recentlyAccessedRepo;

    @Autowired
    private RenovationRepository renovationRepo;

    @Autowired
    private UserRepository userRepo;

    private User user;

    @BeforeEach
    void setup() {
        user = new User("Jane", "Doe", "jane@gmail.com", "password", "password");
        user = userRepo.save(user);
    }

    @Test
    void testFindTop3ByUserOrderByTimeAccessedDesc_GivenNoRenovations_ReturnsEmptyList() {
        List<RecentlyAccessedRenovation> results =
                recentlyAccessedRepo.findTop3ByUserOrderByTimeAccessedDesc(user);

        assertTrue(results.isEmpty());
    }

    @Test
    void testFindTop3ByUserOrderByTimeAccessedDesc_GivenLessThan3_ReturnsAll() {
        Renovation reno1 = new Renovation("Kitchen", "Desc1");
        reno1.setOwner(user);
        reno1 = renovationRepo.save(reno1);

        Renovation reno2 = new Renovation("Bathroom", "Desc2");
        reno2.setOwner(user);
        reno2 = renovationRepo.save(reno2);


        RecentlyAccessedRenovation r1 = new RecentlyAccessedRenovation(user, reno1);
        r1.setTimeAccessed(LocalDateTime.now().minusMinutes(10));
        RecentlyAccessedRenovation r2 = new RecentlyAccessedRenovation(user, reno2);
        r2.setTimeAccessed(LocalDateTime.now());

        recentlyAccessedRepo.save(r1);
        recentlyAccessedRepo.save(r2);

        List<RecentlyAccessedRenovation> results =
                recentlyAccessedRepo.findTop3ByUserOrderByTimeAccessedDesc(user);

        assertEquals(2, results.size());
        assertEquals("Bathroom", results.get(0).getRenovation().getName()); // most recent first
    }

    @Test
    void testFindTop3ByUserOrderByTimeAccessedDesc_GivenMoreThan3_ReturnsLatest3() {
        Renovation reno1 = new Renovation("R1", "Desc1");
        reno1.setOwner(user);
        Renovation reno2 = new Renovation("R2", "Desc2");
        reno2.setOwner(user);
        Renovation reno3 = new Renovation("R3", "Desc3");
        reno3.setOwner(user);
        Renovation reno4 = new Renovation("R4", "Desc4");
        reno4.setOwner(user);

        reno1 = renovationRepo.save(reno1);
        reno2 = renovationRepo.save(reno2);
        reno3 = renovationRepo.save(reno3);
        reno4 = renovationRepo.save(reno4);




        RecentlyAccessedRenovation r1 = new RecentlyAccessedRenovation(user, reno1);
        r1.setTimeAccessed(LocalDateTime.now().minusHours(4));
        RecentlyAccessedRenovation r2 = new RecentlyAccessedRenovation(user, reno2);
        r2.setTimeAccessed(LocalDateTime.now().minusHours(3));
        RecentlyAccessedRenovation r3 = new RecentlyAccessedRenovation(user, reno3);
        r3.setTimeAccessed(LocalDateTime.now().minusHours(2));
        RecentlyAccessedRenovation r4 = new RecentlyAccessedRenovation(user, reno4);
        r4.setTimeAccessed(LocalDateTime.now().minusHours(1));

        recentlyAccessedRepo.saveAll(List.of(r1, r2, r3, r4));

        List<RecentlyAccessedRenovation> results =
                recentlyAccessedRepo.findTop3ByUserOrderByTimeAccessedDesc(user);

        assertEquals(3, results.size());
        assertEquals("R4", results.get(0).getRenovation().getName());
        assertEquals("R3", results.get(1).getRenovation().getName());
        assertEquals("R2", results.get(2).getRenovation().getName());
    }
}
