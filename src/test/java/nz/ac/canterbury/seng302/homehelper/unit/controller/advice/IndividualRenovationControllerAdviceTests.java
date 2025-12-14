package nz.ac.canterbury.seng302.homehelper.unit.controller.advice;

import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.controller.advice.IndividualRenovationControllerAdvice;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.key.RecentlyAccessedRenovationKey;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RecentlyAccessedRenovationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IndividualRenovationControllerAdviceTests {

    private @Mock AppConfig appConfig;
    private @Mock UserService userService;
    private @Mock RenovationService renovationService;
    private @Mock ChatChannelService chatChannelService;
    private @Mock RecentlyAccessedRenovationService recentlyAccessedRenovationService;

    private @InjectMocks IndividualRenovationControllerAdvice controllerAdvice;

    @Test
    void getRenovationAndUser_PublicReno_AddsModelAndChannels() throws Exception {
        Model model = new ConcurrentModel();

        User user = mock(User.class);
        User user2 = mock(User.class);
        when(userService.findUserById(77L)).thenReturn(user);

        Renovation reno = mock(Renovation.class);
        when(reno.getIsPublic()).thenReturn(true);
        when(reno.isMember(any())).thenReturn(false);
        when(reno.getOwner()).thenReturn(user2);
        when(reno.getName()).thenReturn("Kitchen Reno");

        when(renovationService.findRenovation(55L)).thenReturn(Optional.of(reno));

        when(chatChannelService.getChannelByRenovationAndUser(user, reno))
                .thenReturn(List.of(mock(ChatChannel.class)));

        when(appConfig.getBaseUrl()).thenReturn("http://base/");
        when(appConfig.getFullBaseUrl()).thenReturn("http://full/");

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(77L, null)
        );

        controllerAdvice.getRenovationAndUser(55L, model);

        assertThat(model.getAttribute("renovation")).isSameAs(reno);
        assertThat(model.getAttribute("user")).isSameAs(user);
        assertThat(model.getAttribute("isPublic")).isEqualTo(true);
        assertThat(model.getAttribute("isMember")).isEqualTo(false);
        assertThat(model.getAttribute("isOwner")).isEqualTo(false);
        assertThat(model.getAttribute("channels")).asList().hasSize(1);
        assertThat(model.getAttribute("baseUrl")).isEqualTo("http://base/");
        assertThat(model.getAttribute("fullBaseUrl")).isEqualTo("http://full/");
    }

    @Test
    void getRenovationAndUser_PrivateRenoNotMember_Throws404() {
        Model model = new ConcurrentModel();

        UserDetails ud = mock(UserDetails.class);
        when(ud.getUsername()).thenReturn("123");
        User user = mock(User.class);
        when(userService.findUserById(123L)).thenReturn(user);

        Renovation reno = mock(Renovation.class);

        when(reno.getIsPublic()).thenReturn(false);
        when(reno.isMember(any())).thenReturn(false);

        when(renovationService.findRenovation(5L)).thenReturn(Optional.of(reno));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(ud, null)
        );

        assertThatThrownBy(() -> controllerAdvice.getRenovationAndUser(5L, model))
                .isInstanceOf(NoResourceFoundException.class)
                .hasMessageContaining("Renovation not found");
    }

    @Test
    void getRenovationAndUser_PrivateRenovation_AllowsAccessAndAttributesGood() throws Exception {
        Model model = new ConcurrentModel();

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("mem@x");
        when(userService.findUserById(200L)).thenReturn(user);

        Renovation reno = mock(Renovation.class);
        when(reno.getIsPublic()).thenReturn(false);
        when(reno.isMember(any())).thenReturn(true);
        when(reno.getOwner()).thenReturn(user);
        when(reno.getName()).thenReturn("Kitchen Reno");

        when(renovationService.findRenovation(77L)).thenReturn(Optional.of(reno));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(200L, null)
        );

        controllerAdvice.getRenovationAndUser(77L, model);

        assertThat(model.getAttribute("isPublic")).isEqualTo(false);
        assertThat(model.getAttribute("isMember")).isEqualTo(true);
        assertThat(model.getAttribute("isOwner")).isEqualTo(true);
    }
}
