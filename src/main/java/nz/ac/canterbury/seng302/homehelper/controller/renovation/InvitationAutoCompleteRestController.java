package nz.ac.canterbury.seng302.homehelper.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.user.PublicUserDetailsRenovation;
import nz.ac.canterbury.seng302.homehelper.service.renovation.InvitationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Rest controller that handles invitation username auto-completion requests. Provides an
 * endpoint to return a list of user suggestions based on the search input.
 */
@RestController
public class InvitationAutoCompleteRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RenovationService renovationService;
    private final UserService userService;
    private final InvitationService invitationService;

    @Autowired
    public InvitationAutoCompleteRestController(RenovationService renovationService, UserService userService, InvitationService invitationService) {
        this.renovationService = renovationService;
        this.userService = userService;
        this.invitationService = invitationService;
    }

    /**
     * Handles get requests for getting user autocompletion suggestions.
     *
     * @param search the name input to match for.
     * @return a list of public user details matching the search criteria.
     */
    @GetMapping("/invitation/user-matching")
    public List<PublicUserDetailsRenovation> getInvitationNames(
            @RequestParam int renovationId,
            @RequestParam(required = false, defaultValue = "") String search,
            HttpServletRequest request
    ) throws NoResourceFoundException {

        Renovation renovation = renovationService.getRenovation(renovationId)
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Renovation not found"));

        User user = UserUtil.getUserFromHttpServletRequest(userService, request);
        String normalisedSearch = search.trim().toLowerCase();
        return invitationService.findUserAutoCompletionMatches(user, renovation, normalisedSearch);
    }
}
