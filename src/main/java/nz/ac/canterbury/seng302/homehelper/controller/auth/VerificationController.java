package nz.ac.canterbury.seng302.homehelper.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.service.auth.VerificationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for verification page
 */
@Controller
public class VerificationController {

    private final VerificationTokenService verificationTokenService;
    Logger logger = LoggerFactory.getLogger(VerificationController.class);

    @Autowired
    public VerificationController(VerificationTokenService verificationTokenService) {
        this.verificationTokenService = verificationTokenService;
    }

    /**
     * GETs verification code input page
     *
     * @return verification page
     */
    @GetMapping("/verification")
    public String verificationPage() {
        logger.info("GET /verification");
        return "pages/auth/verificationPage";
    }

    /**
     * Submits the verification code to be checked and activates the user if successful
     *
     * @param verificationCode   the verification code inputted by the user
     * @param model              map-like representation of user email and error message
     * @param redirectAttributes sends a success message to the sign-in page
     * @param request            the request which we get the session from to carry over the user email from register page
     * @return sign-in page if successful, otherwise stay on this page
     */
    @PostMapping("/verification")
    public String submitVerification(@RequestParam("verificationCode") String verificationCode, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        logger.info("POST /verification");
        if (verificationTokenService.verifyUserByToken(verificationCode)) {
            redirectAttributes.addFlashAttribute("successMessage", "Your account has been activated, please log in");
            return "redirect:/login";
        }

        model.addAttribute("verificationError", "Signup code invalid");
        return "pages/auth/verificationPage";
    }
}
