package nz.ac.canterbury.seng302.homehelper.unit.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.controller.renovation.NewIndividualRenovationController;
import nz.ac.canterbury.seng302.homehelper.controller.renovation.RenovationPublicityController;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Budget;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UnauthenticatedException;
import nz.ac.canterbury.seng302.homehelper.service.*;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BudgetService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RecentlyAccessedRenovationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.TaskService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewIndividualRenovationControllerTests {

    @Mock
    private AppConfig appConfig;

    @Mock
    private PaginationService paginationService;

    @Mock
    private RenovationService renovationService;

    @Mock
    private UserService userService;

    @Mock
    private ChatChannelService chatChannelService;

    @Mock
    private BudgetService budgetService;

    @Mock
    private TaskService taskService;

    @Mock
    private RecentlyAccessedRenovationService recentlyAccessedRenovationService;

    private NewIndividualRenovationController controller;

    private RenovationPublicityController publicityController;

    @BeforeEach
    void setUp() {
        controller = new NewIndividualRenovationController(
                appConfig, paginationService, renovationService, userService, budgetService, taskService, chatChannelService
        );
        publicityController = new RenovationPublicityController(renovationService, userService, recentlyAccessedRenovationService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void getRenovationOverview_AddsOverviewContent_ReturnsLayout() throws Exception {
        Model model = new ConcurrentModel();
        User u = mock(User.class);

        Renovation reno = mock(Renovation.class);
        Long renoId = 55L;
        when(reno.getId()).thenReturn(renoId);

        Budget budget = mock(Budget.class);
        when(budgetService.findByRenovationId(55L)).thenReturn(Optional.ofNullable(budget));

        String view = controller.getRenovationOverview(model, u, reno);
        assertThat(view).isEqualTo("renovation/layout");
        assertThat(model.getAttribute("contentType")).isEqualTo("overview");
    }

    @Test
    void getChat_ValidAccess_AddsAttributesAndReturnsChat() throws Exception {
        Model model = new ConcurrentModel();

        User user = mock(User.class);
        Renovation reno = mock(Renovation.class);
        Long renoId = 55L;
        when(reno.getId()).thenReturn(renoId);

        Long userId = 7L;
        when(user.getId()).thenReturn(userId);

        ChatChannel channel = mock(ChatChannel.class);
        when(channel.getRenovation()).thenReturn(reno);
        when(channel.getMembers()).thenReturn(List.of(user));

        when(chatChannelService.findById(99L)).thenReturn(Optional.of(channel));
        String view = controller.getChat(99L, user, reno, model, null);

        assertThat(view).isEqualTo("renovation/layout");
        assertThat(model.getAttribute("contentType")).isEqualTo("chat");
        assertThat(model.getAttribute("channel")).isSameAs(channel);
        assertThat(model.getAttribute("userId")).isEqualTo(7L);
        assertThat(model.getAttribute("renovationId")).isEqualTo(55L);
    }

    @Test
    void getChat_ChannelNotFound_Throws404() {
        when(chatChannelService.findById(1L)).thenReturn(Optional.empty());
        User user = mock(User.class);
        assertThatThrownBy(() ->
                controller.getChat(1L, user, mock(Renovation.class), new ConcurrentModel(), null)
        ).isInstanceOf(NoResourceFoundException.class)
                .hasMessageContaining("Channel not found");
    }

    @Test
    void getChat_NotMemberOrWrongReno_Throws404() {
        User user = mock(User.class);
        Renovation reno = mock(Renovation.class);

        Renovation other = mock(Renovation.class);
        ChatChannel channel = mock(ChatChannel.class);
        when(channel.getRenovation()).thenReturn(other);

        when(chatChannelService.findById(99L)).thenReturn(Optional.of(channel));

        assertThatThrownBy(() ->
                controller.getChat(99L, user, reno, new ConcurrentModel(), null)
        ).isInstanceOf(NoResourceFoundException.class)
                .hasMessageContaining("Channel not found");
    }

    @Test
    void getRenovationCalendar_GetsDateFromReferer_ReturnsCalendarContent() {
        User user = mock(User.class);
        Model model = new ConcurrentModel();
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("referer"))
                .thenReturn("http://host/renovation/55/calendar?month=08&year=2025");

        String view = controller.getRenovationCalendar(
                model, user, mock(Renovation.class),
                null, null, "2025-08-16", List.of(), req
        );
        assertThat(view).isEqualTo("renovation/layout");
        assertThat(model.getAttribute("contentType")).isEqualTo("calendar");
        assertThat(model.getAttribute("month")).isEqualTo("08");
        assertThat(model.getAttribute("year")).isEqualTo("2025");
        assertThat(model.getAttribute("dateStr")).isEqualTo("2025-08-16");
        assertThat(model.getAttribute("states")).isEqualTo(List.of());
    }

    @Test
    void getRenovationBudget_NotMember_RedirectsToOverview() throws NoResourceFoundException {
        Model model = new ConcurrentModel();
        Renovation reno = mock(Renovation.class);
        when(reno.getId()).thenReturn(77L);
        User user = mock(User.class);

        String view = controller.getRenovationBudget(model, user, reno, false);
        assertThat(view).isEqualTo("redirect:/renovation/77");
    }

    @Test
    void getRenovationBudget_IsMember_ReturnsLayoutWithBudgetInfo() throws NoResourceFoundException {
        Model model = new ConcurrentModel();
        Renovation reno = mock(Renovation.class);
        when(reno.getId()).thenReturn(77L);
        User user = mock(User.class);
        Budget budget = mock(Budget.class);
        when(budgetService.findByRenovationId(77L)).thenReturn(Optional.ofNullable(budget));

        String view = controller.getRenovationBudget(model, user, reno, true);
        assertThat(view).isEqualTo("renovation/layout");
        assertThat(model.getAttribute("contentType")).isEqualTo("budget");
    }

    @Test
    void getRenovationExpenses_NotMember_RedirectsToOverview() {
        Model model = new ConcurrentModel();
        Renovation reno = mock(Renovation.class);
        when(reno.getId()).thenReturn(88L);
        User user = mock(User.class);

        String view = controller.getRenovationExpenses(model, user, reno, false);
        assertThat(view).isEqualTo("redirect:/renovation/88");
    }

    @Test
    void getRenovationExpenses_GoesToPageWithExpensesContent_ReturnsLayout() {
        Model model = new ConcurrentModel();
        String view = controller.getRenovationExpenses(model, mock(User.class), mock(Renovation.class), true);
        assertThat(view).isEqualTo("renovation/layout");
        assertThat(model.getAttribute("contentType")).isEqualTo("expenses");
    }

    @Test
    void getRenovationMembers_GoesToPageWithMembersContent_ReturnsLayout() {
        Model model = new ConcurrentModel();
        String view = controller.getRenovationMembers(model, mock(User.class), mock(Renovation.class));
        assertThat(view).isEqualTo("renovation/layout");
        assertThat(model.getAttribute("contentType")).isEqualTo("members");
    }

    // ---------- setRenovationVisibility ----------
    @Test
    void setRenovationVisibility_OwnerProvided_SavesAndRedirects() throws Exception {
        long renoId = 123L;
        HttpServletRequest req = mock(HttpServletRequest.class);

        User owner = mock(User.class);
        Renovation reno = mock(Renovation.class);

        when(renovationService.getRenovation(renoId)).thenReturn(Optional.of(reno));
        when(reno.getOwner()).thenReturn(owner);

        // mock the static UserUtil call used by the controller
        try (MockedStatic<UserUtil> mocked = mockStatic(UserUtil.class)) {
            mocked.when(() -> UserUtil.getUserFromHttpServletRequest(userService, req))
                    .thenReturn(owner);

            String view = publicityController.setRenovationVisibility(renoId, true, req);

            verify(reno).setPublic(true);
            verify(renovationService).saveRenovation(reno, owner);
            assertThat(view).isEqualTo("redirect:/renovation/" + renoId);
        }
    }

    @Test
    void setRenovationVisibility_NotOwner_UnAuthException() {
        long renoId = 321L;
        HttpServletRequest req = mock(HttpServletRequest.class);

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(2L, null)
        );

        assertThatThrownBy(() ->
                publicityController.setRenovationVisibility(renoId, false, req)
        ).isInstanceOf(UnauthenticatedException.class);

        verify(renovationService, never()).saveRenovation(any(), any());
    }
}
