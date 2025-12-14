package nz.ac.canterbury.seng302.homehelper.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UnauthenticatedException;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.security.Principal;

/**
 * This controller handles exceptions that may happen when running the site
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private final UserService userService;

    @Autowired
    public GlobalExceptionHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles bad request max upload exceptions
     *
     * @param ex      the max upload exception
     * @param model   model to add to the return
     * @param request the request that was sent by the site
     * @return the edit user page
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // You can use other status codes as needed
    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, Model model, HttpServletRequest request) {
        // You can add attributes to the model to display error messages on the frontend
        Principal userPrincipal = request.getUserPrincipal();
        long id = Long.parseLong(userPrincipal.getName());
        User user = userService.findUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("uploadError", "Image must be less than 10MB.");
        model.addAttribute("isAuthenticated", true);
        return "pages/user/editUserPage";
    }

    /**
     * Handles any instance of a 404 error
     *
     * @param ex    the 404 exception
     * @param model the model to add to the return with the exception message
     *              returns the not found thymeleaf template path
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoResourceFoundException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "pages/notFoundPage";
    }

    /**
     * Handles when an unauthenticated exception is thrown. This will redirect the user to the login page.
     *
     * @return a redirection link to the login page.
     */
    @ExceptionHandler(UnauthenticatedException.class)
    public String handleUnauthenticatedException() {
        return "redirect:/login";
    }
}
