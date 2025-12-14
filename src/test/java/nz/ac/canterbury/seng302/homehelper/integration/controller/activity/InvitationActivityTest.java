package nz.ac.canterbury.seng302.homehelper.integration.controller.activity;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.activity.ActivityType;
import nz.ac.canterbury.seng302.homehelper.model.renovation.InvitationStatus;
import nz.ac.canterbury.seng302.homehelper.repository.activity.ActivityRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.InvitationRepository;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InvitationActivityTest {

    @MockBean
    private ActivityService activityService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private MockMvc mockMvc;

    private User owner;
    private User invitee;
    private Renovation renovation;

    @BeforeEach
    void setup() {
        activityRepository.deleteAll();
        userRepository.deleteAll();
        renovationRepository.deleteAll();

        owner = new User("John", "Smith", "john.smith@gmail.com", "Abc123!!", "Abc123!!");
        owner.setActivated(true);
        userRepository.save(owner);

        invitee = new User("Sam", "Smith", "sam.smith@gmail.com", "Abc123!!", "Abc123!!");
        invitee.setActivated(true);
        userRepository.save(invitee);

        renovation = new Renovation("John's Renovation", "Doing some stuff");
        renovation.setOwner(owner);
        renovationRepository.save(renovation);

    }

    @Test
    void invitationUpdate_RegisteredUser_InviteAccepted() throws Exception {
        Invitation invite = new Invitation(invitee, renovation);
        invite.setInvitationStatus(InvitationStatus.PENDING);
        invitationRepository.save(invite);

        mockMvc.perform(MockMvcRequestBuilders.get("/invitation")
                        .queryParam("token", invite.getId().toString())
                        .with(user(String.valueOf(invitee.getId())).password(invitee.getPassword()).roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(activityService, times(1)). saveLiveUpdate(any());
        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(1)).saveLiveUpdate(captor.capture());

        LiveUpdate liveUpdate = captor.getValue();
        assertEquals(renovation, liveUpdate.getRenovation());
        assertEquals(invitee, liveUpdate.getUser());
        assertEquals(invite, liveUpdate.getInvitation());
        assertEquals(ActivityType.INVITE_ACCEPTED, liveUpdate.getActivityType());
    }


    @Test
    void invitationUpdate_RegisteredUser_InviteDeclined() throws Exception {
        Invitation invite = new Invitation(invitee, renovation);
        invite.setInvitationStatus(InvitationStatus.PENDING);
        invitationRepository.save(invite);

        mockMvc.perform(MockMvcRequestBuilders.get("/decline-invitation")
                        .queryParam("token", invite.getId().toString())
                        .with(user(String.valueOf(invitee.getId())).password(invitee.getPassword()).roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(activityService, times(1)). saveLiveUpdate(any());
        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(1)).saveLiveUpdate(captor.capture());

        LiveUpdate liveUpdate = captor.getValue();
        assertEquals(renovation, liveUpdate.getRenovation());
        assertEquals(invitee, liveUpdate.getUser());
        assertEquals(invite, liveUpdate.getInvitation());
        assertEquals(ActivityType.INVITE_DECLINED, liveUpdate.getActivityType());
    }

    @Test
    void invitationUpdate_UnregisteredUser_InviteAccepted() throws Exception {
        Invitation invite = new Invitation("unregistered@user.com", renovation);
        invite.setInvitationStatus(InvitationStatus.PENDING);
        invitationRepository.save(invite);

        mockMvc.perform(MockMvcRequestBuilders.get("/invitation")
                        .queryParam("token", invite.getId().toString()))
                .andExpect(status().is3xxRedirection());

        verify(activityService, times(0)).saveLiveUpdate(any());

        Invitation inviteCheck = invitationRepository.findById(invite.getId())
                .orElseThrow();
        assertEquals(InvitationStatus.PENDING, inviteCheck.getInvitationStatus());
        assertTrue(inviteCheck.getAcceptedPendingRegistration());
    }

    @Test
    void invitationUpdate_UnregisteredUser_InviteDeclined() throws Exception {
        Invitation invite = new Invitation("unregistered@user.com", renovation);
        invite.setInvitationStatus(InvitationStatus.PENDING);
        invitationRepository.save(invite);

        mockMvc.perform(MockMvcRequestBuilders.get("/decline-invitation")
                        .queryParam("token", invite.getId().toString()))
                .andExpect(status().isOk());

        verify(activityService, times(1)). saveLiveUpdate(any());
        ArgumentCaptor<LiveUpdate> captor = ArgumentCaptor.forClass(LiveUpdate.class);
        verify(activityService, times(1)).saveLiveUpdate(captor.capture());

        LiveUpdate liveUpdate = captor.getValue();
        assertEquals(renovation, liveUpdate.getRenovation());
        assertNull(liveUpdate.getUser());
        assertEquals(invite, liveUpdate.getInvitation());
        assertEquals(ActivityType.INVITE_DECLINED, liveUpdate.getActivityType());
    }

}

