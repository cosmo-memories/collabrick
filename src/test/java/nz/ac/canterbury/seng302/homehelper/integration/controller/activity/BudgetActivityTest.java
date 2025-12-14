package nz.ac.canterbury.seng302.homehelper.integration.controller.activity;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.activity.ActivityType;
import nz.ac.canterbury.seng302.homehelper.repository.activity.ActivityRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.activity.ActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BudgetActivityTest {

    @MockBean
    private ActivityService activityService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private MockMvc mockMvc;

    private User owner;
    private Renovation renovation;

    @BeforeEach
    void setup() {
        activityRepository.deleteAll();
        userRepository.deleteAll();
        renovationRepository.deleteAll();

        owner = new User("John", "Smith", "john.smith@gmail.com", "Abc123!!", "Abc123!!");
        owner.setActivated(true);
        userRepository.save(owner);

        renovation = new Renovation("John's Renovation", "Doing some stuff");
        renovation.setOwner(owner);
        renovationRepository.save(renovation);
    }

    @Test
    void budgetUpdate_OneValueUpdated_NotificationSent() throws Exception {
        mockMvc.perform(post("/renovation/{id}/editBudget", renovation.getId())
                        .with(csrf())
                        .with(user(String.valueOf(owner.getId())).password(owner.getPassword()).roles("USER"))
                        .param("miscellaneousBudget", "1"))
                .andExpect(status().is3xxRedirection());

        verify(activityService, times(1)). saveLiveUpdate(any());
        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(1)).saveLiveUpdate(captor.capture());

        LiveUpdate liveUpdate = captor.getValue();
        assertEquals(renovation, liveUpdate.getRenovation());
        assertEquals(owner, liveUpdate.getUser());
        assertEquals(ActivityType.BUDGET_EDITED, liveUpdate.getActivityType());
    }

    @Test
    void budgetUpdate_MultipleValuesUpdated_NotificationSent() throws Exception {
        mockMvc.perform(post("/renovation/{id}/editBudget", renovation.getId())
                        .with(csrf())
                        .with(user(String.valueOf(owner.getId())).password(owner.getPassword()).roles("USER"))
                        .param("miscellaneousBudget", "1")
                        .param("materialBudget", "2")
                        .param("labourBudget", "3")
                        .param("equipmentBudget", "4")
                        .param("professionalServiceBudget", "5")
                        .param("permitBudget", "6")
                        .param("cleanupBudget", "7")
                        .param("deliveryBudget", "8"))
                .andExpect(status().is3xxRedirection());

        verify(activityService, times(1)). saveLiveUpdate(any());
        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(1)).saveLiveUpdate(captor.capture());

        LiveUpdate liveUpdate = captor.getValue();
        assertEquals(renovation, liveUpdate.getRenovation());
        assertEquals(owner, liveUpdate.getUser());
        assertEquals(ActivityType.BUDGET_EDITED, liveUpdate.getActivityType());
    }

}
