package nz.ac.canterbury.seng302.homehelper.controller.auth;

import nz.ac.canterbury.seng302.homehelper.controller.renovation.RenovationController;
import nz.ac.canterbury.seng302.homehelper.entity.user.ForgottenPasswordToken;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UserUpdatePasswordException;
import nz.ac.canterbury.seng302.homehelper.exceptions.token.TokenExpiredException;
import nz.ac.canterbury.seng302.homehelper.exceptions.token.TokenInvalidException;
import nz.ac.canterbury.seng302.homehelper.service.auth.ForgottenPasswordService;
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
 * Controller responsible for handling password reset requests.
 */
@Controller
public class ResetPasswordController {

    private final ForgottenPasswordService forgottenPasswordService;
    Logger logger = LoggerFactory.getLogger(RenovationController.class);

    @Autowired
    public ResetPasswordController(ForgottenPasswordService forgottenPasswordService) {
        this.forgottenPasswordService = forgottenPasswordService;
    }

    /**
     * Handles GET requests for the forgotten password page.
     *
     * @param model the model to populate attributes for the view.
     * @return the name of the forgotten password page template.
     */
    @GetMapping("forgotten-password")
    public String getForgottenPassword(Model model) {
        logger.info("/GET /forgotten-password");
        model.addAttribute("email", "");
        return "pages/auth/forgottenPasswordPage";
    }

    /**
     * Handles GET requests for the reset password page.
     *
     * @param token the password reset token, if provided.
     * @param model the model to populate attributes for the view.
     * @return the name of the reset password page template.
     */
    @GetMapping("reset-password")
    public String getResetPassword(@RequestParam(required = false) String token, Model model, RedirectAttributes redirectAttributes) {
        logger.info("/GET /reset-password - token: {}", token);

        try {
            ForgottenPasswordToken forgottenPasswordToken = forgottenPasswordService.findForgottenPasswordToken(token);
            model.addAttribute("token", forgottenPasswordToken.getId().toString());
            return "pages/auth/resetPasswordPage";
        } catch (TokenInvalidException | TokenExpiredException e) {
            redirectAttributes.addFlashAttribute("tokenErrorMessage", e.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * Handles POST requests for submitting a forgotten password request.
     *
     * @param email the email address of the user requesting a password reset.
     * @param model the model to populate attributes for the view.
     * @return the name of the forgotten password page template.
     */
    @PostMapping("forgotten-password")
    public String postForgottenPassword(@RequestParam String email, Model model) {
        logger.info("/POST /forgotten-password");
        try {
            forgottenPasswordService.sendResetPasswordEmail(email);
            model.addAttribute("emailSentMessage", "An email was sent to the address if it was recognised");
        } catch (IllegalArgumentException e) {
            model.addAttribute("emailErrorMessage", e.getMessage());
        }
        return "pages/auth/forgottenPasswordPage";
    }

    /**
     * Handles POST requests for submitting a password reset.
     *
     * @param newPassword          the new password chosen by the user.
     * @param confirmedNewPassword the confirmed password chosen by the user.
     * @param model                the model to populate attributes for the view.
     * @return the name of the reset password page template.
     */
    @PostMapping("reset-password")
    public String postResetPassword(@RequestParam String token,
                                    @RequestParam String newPassword,
                                    @RequestParam String confirmedNewPassword,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        logger.info("/POST /reset-password");

        try {
            forgottenPasswordService.resetPassword(token, newPassword, confirmedNewPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Your password has been reset.");
            return "redirect:/login";
        } catch (TokenInvalidException | TokenExpiredException e) {
            redirectAttributes.addFlashAttribute("tokenErrorMessage", e.getMessage());
            return "redirect:/login";
        } catch (UserUpdatePasswordException e) {
            model.addAttribute("token", token);
            model.addAttribute("newPasswordError", e.getNewPasswordError());
            model.addAttribute("confirmedNewPasswordError", e.getRetypedNewPasswordError());
            return "pages/auth/resetPasswordPage";
        }
    }
}
