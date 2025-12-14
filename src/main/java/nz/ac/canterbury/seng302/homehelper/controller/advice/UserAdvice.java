package nz.ac.canterbury.seng302.homehelper.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatMentionService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class UserAdvice {
    private final UserService userService;
    private final ChatMentionService chatMentionService;
    Logger logger = LoggerFactory.getLogger(UserAdvice.class);
    private final AppConfig appConfig;

    @Autowired
    public UserAdvice(UserService userService, AppConfig appConfig, ChatMentionService chatMentionService) {
        this.userService = userService;
        this.appConfig = appConfig;
        this.chatMentionService = chatMentionService;
    }

    /**
     * Add the user's information to the model so their details can be retrieved easily
     *
     * @param model   Model
     * @param request HTTP request
     */
    @ModelAttribute()
    public void addUserToModel(Model model, HttpServletRequest request) {
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null) {
            long id = Long.parseLong(userPrincipal.getName());
            User user = userService.findUserById(id);
            // If user is logged in
            if (user != null) {
                // Add user to model
                model.addAttribute("chatMentions", chatMentionService.getOutgoingMentions(user.getId()));
                model.addAttribute("user", user);
                model.addAttribute("userId", id);
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("webSocketUrl", appConfig.getWebSocketUrl());
                model.addAttribute("baseUrl", appConfig.getFullBaseUrl());
                model.addAttribute("fullBaseUrl", appConfig.getFullBaseUrl());
            }
        } else {
            // User is not logged in
            model.addAttribute("isAuthenticated", false);
        }
    }
}
