package nz.ac.canterbury.seng302.homehelper.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.RenovationDetailsException;
import nz.ac.canterbury.seng302.homehelper.model.Pagination;
import nz.ac.canterbury.seng302.homehelper.model.renovation.OwnershipFilter;
import nz.ac.canterbury.seng302.homehelper.service.PaginationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RecentlyAccessedRenovationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Controller for renovation form
 */
@Controller
public class RenovationController {
    private final Logger logger = LoggerFactory.getLogger(RenovationController.class);
    private final RenovationService renovationService;
    private final PaginationService paginationService;
    private final UserService userService;
    private final RecentlyAccessedRenovationService recentlyAccessedRenovationService;

    @Autowired
    public RenovationController(RenovationService renovationService, PaginationService paginationService, UserService userService, ChatChannelService chatChannelService, RecentlyAccessedRenovationService recentlyAccessedRenovationService) {
        this.renovationService = renovationService;
        this.paginationService = paginationService;
        this.userService = userService;
        this.recentlyAccessedRenovationService = recentlyAccessedRenovationService;
    }

    /**
     * Handles HTTP GET requests to the /myRenovations endpoint for displaying a paginated list
     * of the users renovations.
     *
     * @param page               the current page number to display, defaults to the first page
     * @param gotoPage           an optional specific page to navigate to
     * @param model              the Model object used to pass attributes to the view
     * @param request            the HttpServletRequest object containing request data and parameters
     * @param ownershipFilterStr the filter of which renovations is wanted
     * @return the name of the view template to render
     */
    @GetMapping("/myRenovations")
    public String getTemplate(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "gotoPage", required = false) String gotoPage,
            @RequestParam(name = "ownershipFilter", required = false, defaultValue = "ALL") String ownershipFilterStr,
            Model model,
            HttpServletRequest request
    ) {
        logger.info("GET /myRenovations");
        OwnershipFilter ownershipFilter;
        try {
            ownershipFilter = OwnershipFilter.valueOf(ownershipFilterStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            ownershipFilter = OwnershipFilter.ALL;
        }
        OwnershipFilter finalOwnershipFilter = ownershipFilter;
        User user = UserUtil.getUserFromHttpServletRequest(userService, request);
        Function<Pageable, Page<Renovation>> pageSupplier = pageable ->
                renovationService.findUsersAccessibleRenovations(user, finalOwnershipFilter, pageable);
        Pagination<Renovation> pagination = paginationService.paginate(page, gotoPage, pageSupplier, request);
        model.addAttribute("userId", user.getId());
        model.addAttribute("pagination", pagination);
        model.addAttribute("ownershipFilter", ownershipFilter);
        model.addAttribute("activeLink", "My Renovations");
        return "pages/renovation/myRenovationsPage";
    }

    /**
     * Posts a form response with name and description of renovation
     *
     * @param location Renovation Location
     * @param model    (map-like) representation of name, language and isJava boolean for use in thymeleaf,
     *                 with values being set to relevant parameters provided
     * @param request  the request sent by the browser to get the authenticated user from
     * @return thymeleaf ...
     */
    @PostMapping("/myRenovations/newRenovation")
    public String submitRenovation(@ModelAttribute("renovation") Renovation renovation,
                                   @RequestParam(name = "roomName[]", required = false) List<String> roomNames,
                                   @ModelAttribute("location") Location location,
                                   Model model, HttpServletRequest request) {
        logger.info("POST /myRenovations");

        User user = UserUtil.getUserFromHttpServletRequest(userService, request);
        try {
            renovation.setLocation(location);
            renovationService.createRenovation(renovation, roomNames, user);
            return "redirect:/renovation/" + renovation.getId();
        } catch (RenovationDetailsException e) {
            model.addAttribute("renovation", renovation);
            model.addAttribute("roomNames", roomNames);
            model.addAttribute("isNewRenovation", true);

            List<Integer> roomIds = Collections.nCopies(roomNames.size() + 1, -1);
            model.addAttribute("roomIds", roomIds);

            // Add error messages to model:
            renovationService.populateErrors(location, model, e);
            return "pages/renovation/createEditRenovationPage";
        }
    }

    /**
     * A post mapping to delete a renovation
     *
     * @param id          the id of the renovation to delete
     * @param model       the model only has which link to show as active
     * @param previousUrl the url that was used to access the individual renovation
     * @param request     the server request to check if the user came from myRenovations page
     * @return a redirect to my renovations
     */
    @PostMapping("/delete")
    public String deleteRenovation(@RequestParam(name = "renovationInstance") String id,
                                   @RequestParam(name = "previousUrl") String previousUrl,
                                   Model model, HttpServletRequest request) throws NoResourceFoundException {
        // just a small vulnerability fix where any user could delete any renovation
        User user = UserUtil.getUserFromHttpServletRequest(userService, request);
        Renovation renovation = renovationService.getRenovation(Long.parseLong(id))
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Renovation not found"));

        if (renovation.getOwner().equals(user)) {
            recentlyAccessedRenovationService.deleteAllRenovationAccessEntriesForRenovation(renovation.getId());
            renovationService.removeRenovation(renovation);
            logger.info("POST /delete");
        } else {
            logger.info("Renovation not found");
            throw new NoResourceFoundException(HttpMethod.GET, "Renovation not found");
        }
        model.addAttribute("activeLink", "My Renovations");
        String referer = request.getHeader("referer");
        if (referer.matches(".*/myRenovations$")) {
            return "redirect:/myRenovations";
        }
        return "redirect:" + (previousUrl != null ? previousUrl : "/myRenovations");
    }

    /**
     * Gets a new renovation form
     *
     * @param model   the model of if this is a new renovation to adjust elements in the html
     * @param request the request that is made to the server to find if a user is currently authenticated
     * @return the renovation form for a new renovation, or a redirect to login if a user is not currently authenticated
     */
    @GetMapping("/myRenovations/newRenovation")
    public String getNewRenovationForm(Model model, HttpServletRequest request) {
        logger.info("GET /myRenovations/newRenovation");
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null) {
            model.addAttribute("isNewRenovation", true);
            model.addAttribute("renovation", new Renovation("", ""));
            model.addAttribute("location", new Location());
            return "pages/renovation/createEditRenovationPage";
        }
        return "redirect:/login";
    }


}
