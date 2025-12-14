package nz.ac.canterbury.seng302.homehelper.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RecentlyAccessedRenovationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Controller
public class RenovationPublicityController {

    private final RenovationService renovationService;
    private final UserService userService;

    private final RecentlyAccessedRenovationService recentlyAccessedRenovationService;

    @Autowired
    public RenovationPublicityController(RenovationService renovationService, UserService userService, RecentlyAccessedRenovationService recentlyAccessedRenovationService) {
        this.renovationService = renovationService;
        this.userService = userService;
        this.recentlyAccessedRenovationService = recentlyAccessedRenovationService;
    }

    /**
     * PostMapping for setting a renovation to public or private
     *
     * @param renovationId the id of the renovation to change visibility
     * @param isPublic     true if the renovation is to be public, false if it is to be private
     * @param request      the request sent, need to retrieve the user to ensure they own the renovation
     * @return redirect back to the renovation whose publicity was being changed
     */
    @PostMapping("/renovation/{renovationId}/setVisibility")
    public String setRenovationVisibility(@PathVariable(name = "renovationId") long renovationId,
                                          @RequestParam(name = "isPublic") Boolean isPublic,
                                          HttpServletRequest request) throws NoResourceFoundException {
        User user = UserUtil.getUserFromHttpServletRequest(userService, request);
        Renovation renovation = renovationService.getRenovation(renovationId)
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Renovation not found"));

        if (!renovation.getOwner().equals(user)) {
            throw new NoResourceFoundException(HttpMethod.GET, "Renovation not found");
        }

        if (isPublic != null) {
            renovation.setPublic(isPublic);
            renovationService.saveRenovation(renovation, user);
            recentlyAccessedRenovationService.deleteNonMemberAccessesFromPrivateRenovation(renovationId, isPublic);

        }
        return "redirect:/renovation/" + renovationId;
    }
}
