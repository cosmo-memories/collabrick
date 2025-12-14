package nz.ac.canterbury.seng302.homehelper.controller.advice;

import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UnauthenticatedException;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RecentlyAccessedRenovationRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RecentlyAccessedRenovationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@ControllerAdvice
public class IndividualRenovationControllerAdvice {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AppConfig appConfig;
    private final UserService userService;
    private final RenovationService renovationService;
    private final ChatChannelService chatChannelService;
    private final RecentlyAccessedRenovationService recentlyAccessedRenovationService;

    @Autowired
    public IndividualRenovationControllerAdvice(AppConfig appConfig, UserService userService, RenovationService renovationService, ChatChannelService chatChannelService, RecentlyAccessedRenovationService recentlyAccessedRenovationService) {
        this.appConfig = appConfig;
        this.userService = userService;
        this.renovationService = renovationService;
        this.chatChannelService = chatChannelService;
        this.recentlyAccessedRenovationService = recentlyAccessedRenovationService;
    }

    /**
     * Loads the renovation and authenticated user into the model for all handler methods in this controller. Throws
     * a 404 error if the renovation does not exist or the user does not have access.
     *
     * @param renovationId the ID of the renovation.
     * @param model        The model object.
     */
    @ModelAttribute
    public void getRenovationAndUser(
            @PathVariable(name = "renovationId", required = false) Long renovationId,
            Model model
    ) throws NoResourceFoundException {
        if (renovationId == null)
            return;

        Renovation renovation = null;
        User user = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Long userId) {
            logger.debug("Found user principal as a long - {}", userId);
            user = userService.findUserById(userId);
        } else if (principal instanceof UserDetails userDetails) {
            String userId = userDetails.getUsername();
            logger.debug("Found user principal as UserDetails - {}", userId);
            user = userService.findUserById(Long.parseLong(userId));
        }

        if (user == null) {
            logger.debug("User not authenticated or is invalid");
        } else {
            logger.debug("User found: {}", user.getEmail());
        }

        logger.debug("Fetching renovation with ID: {}", renovationId);
        try {
            renovation = renovationService.findRenovation(renovationId).orElse(null);
            if (renovation == null) {
                logger.debug("Renovation not found for ID: {}", renovationId);
            } else if (renovation.getIsPublic() || (user != null && renovation.isMember(user))) {
                boolean isPublic = renovation.getIsPublic();
                boolean isMember = user != null && renovation.isMember(user);
                boolean isOwner = renovation.getOwner().equals(user);
                model.addAttribute("isPublic", renovation.getIsPublic());
                model.addAttribute("isMember", user != null && renovation.isMember(user));
                model.addAttribute("isOwner", renovation.getOwner().equals(user));
                logger.debug("Renovation found and accessible: {} - isPublic={}, isMember={}, isOwner={}",
                        renovation.getName(), isPublic, isMember, isOwner);
            } else if (user == null & !renovation.getIsPublic()) {
                logger.debug("Renovation is private and user is not logged in");
                throw new UnauthenticatedException();
            } else {
                logger.debug("Renovation is private and user is not a member");
                throw new NoResourceFoundException(HttpMethod.GET, "Renovation not found");
            }
        } catch (NumberFormatException e) {
            logger.debug("Invalid renovation ID format: {}", renovationId);
        }

        if (renovation == null) {
            throw new NoResourceFoundException(HttpMethod.GET, "Renovation not found");
        }
        if (user != null) {
            recentlyAccessedRenovationService.createOrUpdateRecentlyAccessedRenovation(renovation, user);
        }
        List<ChatChannel> channels = chatChannelService.getChannelByRenovationAndUser(user, renovation);
        model.addAttribute("channels", channels);
        model.addAttribute("channelNames", channels.stream().map(ChatChannel::getName).toList());
        model.addAttribute("renovation", renovation);
        model.addAttribute("user", user);
        model.addAttribute("baseUrl", appConfig.getBaseUrl());
        model.addAttribute("fullBaseUrl", appConfig.getFullBaseUrl());
    }
}
