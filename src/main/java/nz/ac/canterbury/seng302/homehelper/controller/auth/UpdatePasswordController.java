package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UserUpdatePasswordException;
import nz.ac.canterbury.seng302.homehelper.model.auth.UserPasswordUpdate;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import static nz.ac.canterbury.seng302.homehelper.utility.UserUtil.getUserFromHttpServletRequest;

/**
 * Controller responsible for handling password update requests
 */
@Controller
public class UpdatePasswordController {
    private final Logger logger = LoggerFactory.getLogger(UpdatePasswordController.class);
    private final UserService userService;

    /**
     * Constructs an instance of UpdatePasswordController with the user service.
     *
     * @param userService the service handling user operations.
     */
    @Autowired
    public UpdatePasswordController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles GET requests to display the password update page.
     *
     * @param model   the model object used to pass attributes to the view.
     * @param request the object representing the client request.
     * @return the name of the view template.
     */
    @GetMapping("/update-password")
    public String updatePasswordPage(Model model, HttpServletRequest request) {
        model.addAttribute("passwordUpdate", new UserPasswordUpdate());
        model.addAttribute("activeLink", "updatePassword");
        return "pages/auth/updatePasswordPage";
    }

    /**
     * Handles POST requests to process the password update form submission.
     *
     * @param passwordUpdate the user password update object containing the submitted password data.
     * @param model          the model object used to pass attributes to the view.
     * @param request        the object representing the client request.
     * @return a redirection to the user page if successful, or the password update page with error messages if unsuccessful.
     */
    @PostMapping("/update-password")
    public String updatePasswordSubmit(@ModelAttribute UserPasswordUpdate passwordUpdate, Model model, HttpServletRequest request) {
        User user = getUserFromHttpServletRequest(userService, request);
        try {
            userService.updateUserPassword(user, passwordUpdate);
            return "redirect:/userPage";
        } catch (UserUpdatePasswordException e) {
            model.addAttribute("oldPasswordError", e.getOldPasswordError());
            model.addAttribute("newPasswordError", e.getNewPasswordError());
            model.addAttribute("retypedNewPasswordError", e.getRetypedNewPasswordError());
        }
        logger.info(passwordUpdate.toString());
        model.addAttribute("passwordUpdate", passwordUpdate);
        return "pages/auth/updatePasswordPage";
    }
}
