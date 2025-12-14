package nz.ac.canterbury.seng302.homehelper.controller.chat;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatMention;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UnauthenticatedException;
import nz.ac.canterbury.seng302.homehelper.model.chat.OutgoingMention;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatMentionService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * REST controller for handling operations related to chat mentions.
 *
 *
 * @see ChatMentionService
 * @see UserService
 */
@RestController
public class MentionController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ChatMentionService chatMentionService;
    private final UserService userService;

    @Autowired
    public MentionController(ChatMentionService chatMentionService,  UserService userService) {
        this.chatMentionService = chatMentionService;
        this.userService = userService;
    }

    /**
     * Marks all mentions in a given chat channel as "seen" for the currently authenticated user.
     *
     * @param channelId the ID of the chat channel in which mentions should be marked as seen
     * @param request   the HTTP servlet request, used to determine the currently authenticated user
     * @return a {@link ResponseEntity} containing:
     *         <ul>
     *             <li>{@link HttpStatus#OK} if the mentions were successfully marked as seen</li>
     *             <li>{@link HttpStatus#INTERNAL_SERVER_ERROR} if an unexpected error occurs</li>
     *         </ul>
     * @throws UnauthenticatedException if no authenticated user can be resolved from the request
     */
    @PostMapping("/mark-seen/{channelId}")
    public ResponseEntity<HttpStatus> markSeen(@PathVariable Long channelId,
                                               HttpServletRequest request) {
        Optional<User> optionalUser = UserUtil.getOptionalUserFromHttpServletRequest(userService, request);
        if (optionalUser.isEmpty()) {
            throw new UnauthenticatedException();
        }
        User user = optionalUser.get();
        logger.info("Marking seen mentions for {}, user {}", channelId, user.getId());

        try {
            chatMentionService.markMentionsAsSeen(user.getId(), channelId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while marking seen mentions for {}", channelId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
