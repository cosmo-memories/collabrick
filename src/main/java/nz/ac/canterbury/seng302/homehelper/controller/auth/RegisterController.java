package nz.ac.canterbury.seng302.homehelper.controller.auth;

import jakarta.mail.MessagingException;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UserDetailsInvalidException;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.service.auth.VerificationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

/**
 * Spring Boot controller class; defines endpoints as functions with HTTP mappings
 */
@Controller
public class RegisterController {
    private final UserService userService;
    private final LocationService locationService;
    private final VerificationTokenService verificationTokenService;
    Logger logger = LoggerFactory.getLogger(RegisterController.class);

    /**
     * Constructs an {@code LoginController} with the required service
     *
     * @param userService              the user service
     * @param verificationTokenService the verification token service
     */
    @Autowired
    public RegisterController(UserService userService, LocationService locationService, VerificationTokenService verificationTokenService) {
        this.userService = userService;
        this.locationService = locationService;
        this.verificationTokenService = verificationTokenService;
    }

    /**
     * GETs initial register page
     *
     * @param model representation of the user and the activeLink in thymeleaf
     * @return register page
     */
    @GetMapping("/register")
    public String registerPage(@RequestParam(value = "from", required = false) String from,
                               @ModelAttribute("user") User user,
                               @ModelAttribute("location") Location location,
                               Model model) {
        logger.info("GET /register");
        // redirect the user to home if they are already logged in
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/";
        }

        // user will be provided when redirecting to the register page from accepting an invitation
        model.addAttribute("user", Objects.requireNonNullElseGet(user, User::new));
        model.addAttribute("location", new Location());
        model.addAttribute("activeLink", "register");
        return "pages/auth/registerPage";
    }

    /**
     * Handles the submission of the user registration form.
     *
     * @param user               the user data to add to database
     * @param model              the model thymeleaf model containing error message attributes
     * @param redirectAttributes the redirect attributes for setting flash attributes during redirection.
     * @return "/" home page if success, otherwise return register
     */
    @PostMapping("/register")
    public String submitRegisterForm(@ModelAttribute User user,
                                     @ModelAttribute("location") Location location,
                                     Model model, RedirectAttributes redirectAttributes) {
        logger.info("POST /register");
        try {
            user.setActivated(false);
            user.setLocation(location);
            userService.addUser(user);
            verificationTokenService.sendVerificationEmail(user);
            redirectAttributes.addFlashAttribute("email", user.getEmail());
            return "redirect:/verification";
        } catch (UserDetailsInvalidException e) {
            locationService.populateLocationErrors(location, model, e);
            model.addAttribute("errors", e);
            return "pages/auth/registerPage";
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
