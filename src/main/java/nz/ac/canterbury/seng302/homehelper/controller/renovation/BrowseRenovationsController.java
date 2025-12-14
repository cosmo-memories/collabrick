package nz.ac.canterbury.seng302.homehelper.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.Pagination;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationMemberService;
import nz.ac.canterbury.seng302.homehelper.service.PaginationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Controller responsible for handling browsing of renovation requests.
 */
@Controller
public class BrowseRenovationsController {
    private final Logger logger = LogManager.getLogger(getClass());
    private final UserService userService;
    private final RenovationService renovationService;
    private final PaginationService paginationService;
    private final RenovationMemberService renovationMemberService;
    private final AppConfig appConfig;

    @Autowired

    public BrowseRenovationsController(UserService userService, RenovationService renovationService,
                                       PaginationService paginationService, AppConfig appConfig, RenovationMemberService renovationMemberService) {
        this.userService = userService;
        this.renovationService = renovationService;
        this.paginationService = paginationService;
        this.appConfig = appConfig;
        this.renovationMemberService = renovationMemberService;
    }

    /**
     * Handles HTTP GET requests to the /browse endpoint for displaying a paginated list
     * of public renovations.
     *
     * @param page     the current page number to display, defaults to the first page
     * @param gotoPage an optional specific page to navigate to
     * @param search   an optional search query string used to filter renovations by keyword
     * @param tags     an optional list of tags used to filter renovations
     * @param onlyMine an optional flag indicating whether to show only the users own renovations.
     * @param model    the Model object used to pass attributes to the view
     * @param request  the HttpServletRequest object containing request data and parameters
     * @return the name of the view template to render
     */
    @GetMapping("/browse")
    public String getBrowseRenovations(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "gotoPage", required = false) String gotoPage,
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "tags", required = false) List<String> tags,
            @RequestParam(defaultValue = "false") boolean onlyMine,
            Model model, HttpServletRequest request) {
        logger.info("GET /browse");
        model.addAttribute("activeLink", "Browse Renovations");
        model.addAttribute("onlyMine", onlyMine);

        String normalizedSearch = search.trim();
        if (!normalizedSearch.isEmpty()) {
            model.addAttribute("searchUrl", "&search=" + normalizedSearch);
        }
        model.addAttribute("search", normalizedSearch);
        model.addAttribute("tags", tags);

        Optional<User> user = onlyMine ? UserUtil.getOptionalUserFromHttpServletRequest(userService, request) : Optional.empty();
        if (onlyMine && user.isEmpty()) {
            return "redirect:" + appConfig.buildUriFromRequest(request)
                    .replaceQueryParam("onlyMine")
                    .build();
        }

        Function<Pageable, Page<Renovation>> pageSupplier = pageable ->
                onlyMine ?
                        renovationService.findUsersRenovations(user.get(), search, tags, pageable) :
                        renovationService.findPublicRenovations(search, tags, pageable);
        Pagination<Renovation> pagination = paginationService.paginate(page, gotoPage, pageSupplier, request);
        model.addAttribute("pagination", pagination);

        if (pagination.getItems().isEmpty()) {
            model.addAttribute("searchError", "No renovations match your search");
        }

        return "pages/renovation/browseRenovationsPage";
    }
}
