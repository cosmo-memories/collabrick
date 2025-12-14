package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.service.activity.ActivityService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RecentlyAccessedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RecentlyAccessedRenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.List;
import java.util.Optional;

/**
 * This is a basic spring boot controller, note the @link{Controller} annotation which defines this.
 * This controller defines endpoints as functions with specific HTTP mappings
 */

@Controller
public class HomeController {

    private final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private final UserService userService;
    private final TaskService taskService;
    private final RenovationService renovationService;
    private final RecentlyAccessedRenovationService recentlyAccessedRenovationService;
    private final ActivityService activityService;

    @Autowired
    public HomeController(UserService userService, TaskService taskService, RenovationService renovationService, ActivityService activityService, RecentlyAccessedRenovationService recentlyAccessedRenovationService) {
        this.userService = userService;
        this.taskService = taskService;
        this.renovationService = renovationService;
        this.recentlyAccessedRenovationService = recentlyAccessedRenovationService;
        this.activityService = activityService;
    }

    /**
     * Redirects GET default url '/' to '/home'
     *
     * @return redirect to /home
     */
    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        logger.info("GET /");
        model.addAttribute("activeLink", "Home");

        Optional<User> authenticatedUser = UserUtil.getOptionalUserFromHttpServletRequest(userService, request);
        if (authenticatedUser.isPresent()) {
            User user = authenticatedUser.get();
            model.addAttribute("activity", activityService.getUserUpdates(user.getId()));
            model.addAttribute("tasks", taskService.findUpcomingTasks(user.getId()));
            model.addAttribute("renoSum", renovationService.sumRenovationsForUser(user));
            model.addAttribute("completeTasks", taskService.sumCompletedTasksByUser(user));

            List<RecentlyAccessedRenovation> recentlyAccessedRenovations = recentlyAccessedRenovationService.getRecentlyAccessedRenovationsForUser(user);
            model.addAttribute("recentlyAccessedRenovations", recentlyAccessedRenovations);
            return "pages/dashboard";
        }
        return "pages/homePage";
    }
}
