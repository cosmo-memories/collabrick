package nz.ac.canterbury.seng302.homehelper.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.controller.advice.UserAdvice;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Controller
public class BrickAIPermissionsRestController {

    private final RenovationService renovationService;
    private final UserService userService;
    Logger logger = LoggerFactory.getLogger(BrickAIPermissionsRestController.class);


    /**
     * REST controller for managing BrickAI access permissions on renovations.
     *
     * Provides an endpoint to toggle whether BrickAI is allowed to read a specific
     * renovation's details. The current user is resolved from the {@link HttpServletRequest}
     * and must be the owner of the target renovation.
     */
    @Autowired
    public BrickAIPermissionsRestController(RenovationService renovationService, UserService userService) {
        this.renovationService = renovationService;
        this.userService = userService;
    }

    /**
     * Toggles the {@code allowBrickAI} flag for the given renovation.
     *
     *
     * @param renovationId the ID of the renovation to update
     * @param request      the incoming HTTP request, used to resolve the caller
     * @return {@link ResponseEntity} with status {@code 200} on success or {@code 500} otherwise
     * @throws NoResourceFoundException if the renovation does not exist or the user is not the owner
     */
    @PostMapping("/renovation/{renovationId}/allowBrickAI")
    public String setBrickAIPermissions(@PathVariable(name = "renovationId") long renovationId,
                                                      HttpServletRequest request) throws NoResourceFoundException {
        try {
            User user = UserUtil.getUserFromHttpServletRequest(userService, request);
            Renovation renovation = renovationService.getRenovation(renovationId)
                    .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Renovation not found"));

            if (!renovation.getOwner().equals(user)) {
                throw new Exception("Only the owner of this renovation can change the AI permissions");
            }

            renovationService.toggleAllowBrickAI(renovation);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "redirect:/renovation/" + renovationId;

    }

    /**
     * Handles POST requests to toggle the BrickAI access permissions for the currently
     * authenticated user.
     *
     * @param request the {@link HttpServletRequest} containing the authenticated user's session
     * @return a {@link ResponseEntity} with status 200 if successful, or 500 if an error occurs
     * @throws NoResourceFoundException if the user cannot be found from the request
     */
    @PostMapping("/userPage/allowBrickAI")
    public ResponseEntity<Void> setUserBrickAIPermissions(HttpServletRequest request) throws NoResourceFoundException {
        try {
            User user = UserUtil.getUserFromHttpServletRequest(userService, request);
            userService.toggleBrickAIAccess(user);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
