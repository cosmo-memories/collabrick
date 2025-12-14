package nz.ac.canterbury.seng302.homehelper.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UserDetailsInvalidException;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

/**
 * Spring Boot controller class; defines endpoints as functions with HTTP mappings
 */
@Controller
public class UserPageController {
    private final UserService userService;
    private final LocationService locationService;
    Logger logger = LoggerFactory.getLogger(UserPageController.class);

    @Autowired
    public UserPageController(UserService userService, LocationService locationService) {
        this.userService = userService;
        this.locationService = locationService;
    }

    /**
     * GET user profile page
     *
     * @return the user page
     */
    @GetMapping("/userPage")
    public String userPage(Model model) {
        logger.info("GET /userPage");
        return "pages/user/userPage";
    }

    /**
     * The get for the edit user page
     *
     * @param model unused model
     * @return the edit user page
     */
    @GetMapping("/userPage/edit")
    public String editUserPage(Model model, HttpServletRequest request) {
        logger.info("GET /userPage/edit");
        Principal userPrincipal = request.getUserPrincipal();
        long id = Long.parseLong(userPrincipal.getName());
        User authUser = userService.findUserById(id);
        model.addAttribute("editedUser", authUser);
        model.addAttribute("location", authUser.getLocation());
        return "pages/user/editUserPage";
    }

    /**
     * The post mapping for editing a user
     *
     * @param user    the user data
     * @param model   a model of the user to return
     * @param request the request made to the server
     * @return if editing is successful redirect to the user page, if unsuccessful the edit user page with errors preventing submission shown.
     */
    @PostMapping("/userPage")
    public String submitEditUserForm(@ModelAttribute("editedUser") User user,
                                     @ModelAttribute("location") Location location,
                                     Model model, HttpServletRequest request) {
        Principal userPrincipal = request.getUserPrincipal();
        //If userPage has been accessed then there must be an authenticated user present hence no null check
        long id = Long.parseLong(userPrincipal.getName());
        User authUser = userService.findUserById(id);
        String email = user.getEmail();
        String fname = user.getFname();
        String lname = user.getLname();
        authUser.setLocation(location);
        try {
            userService.updateUser(authUser, fname, lname, email);
        } catch (UserDetailsInvalidException e) {
            model.addAttribute("errors", e);
            locationService.populateLocationErrors(location, model, e);
            return "pages/user/editUserPage";
        }
        return "redirect:/userPage";
    }
}
