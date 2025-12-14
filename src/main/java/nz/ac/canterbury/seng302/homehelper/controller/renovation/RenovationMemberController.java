package nz.ac.canterbury.seng302.homehelper.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UnauthenticatedException;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.InvalidPermissionsException;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RecentlyAccessedRenovationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationMemberService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Objects;
import java.util.Optional;

/**
 * Controller for handling requests related to renovation members.
 * Provides functionality for removing a member from a renovation.
 */
@Controller
public class RenovationMemberController {
    private final RenovationMemberService renovationMemberService;
    private final UserService userService;
    private final RenovationService renovationService;
    private final RecentlyAccessedRenovationService recentlyAccessedRenovationService;

    public RenovationMemberController(RenovationMemberService renovationMemberService, RenovationService renovationService, UserService userService, RenovationService renovationService1, RecentlyAccessedRenovationService recentlyAccessedRenovationService) {
        this.renovationMemberService = renovationMemberService;
        this.userService = userService;
        this.renovationService = renovationService1;
        this.recentlyAccessedRenovationService = recentlyAccessedRenovationService;
    }

    /**
     * Handles POST requests for removing a user from a renovation.
     *
     * @param renovationId       The ID of the renovation to remove the user from.
     * @param renovationUser     The user to be removed from the renovation.
     * @param request            The HTTP request object, used to identify the current session/user.
     * @param redirectAttributes Attributes used for passing flash attributes on redirect.
     * @return A redirect string to the renovation members page.
     * @throws Exception If the user is unauthenticated, lacks permission, or the renovation/member is not found.
     */
    @PostMapping("/renovation/{renovationId}/removeMember")
    public String removeRenovationMember(@PathVariable String renovationId,
                                         @RequestParam(name = "renovationUser") User renovationUser,
                                         HttpServletRequest request, RedirectAttributes redirectAttributes) throws Exception{
        try {
            Optional<User> optionalUser = UserUtil.getOptionalUserFromHttpServletRequest(userService, request);
            if (optionalUser.isEmpty()) { //Case: User is not logged in
                throw new UnauthenticatedException();
            }
            Renovation renovation = renovationService.getRenovation(Long.parseLong(renovationId))
                    .orElseThrow(() -> new RuntimeException("Renovation not found"));
            User user = optionalUser.get();
            if (!Objects.equals(user.getEmail(), renovation.getOwner().getEmail())) {
                throw new InvalidPermissionsException("You do not have permission to remove this member");
            }
            renovationMemberService.checkMembership(renovationUser, renovation);
            RenovationMember renovationMember = renovationMemberService.getRenovationMember(renovationUser, renovation);
            renovationMemberService.deleteRenovationMember(renovationMember);
            recentlyAccessedRenovationService.deleteUserRenovationAccessEntryForPrivateRenovation(renovation.getId(), renovationUser.getId());
            redirectAttributes.addFlashAttribute("showRemoveConfirmationToast", true);
            redirectAttributes.addFlashAttribute("removee", renovationMember.getUser().getFname() + " " + renovationMember.getUser().getLname());
            return "redirect:/renovation/" + renovationId + "/members";
        } catch(UnauthenticatedException | InvalidPermissionsException |NoResourceFoundException e) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error Removing Renovation member");
            }
        }
}
