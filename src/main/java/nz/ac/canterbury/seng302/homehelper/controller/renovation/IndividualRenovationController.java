package nz.ac.canterbury.seng302.homehelper.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Room;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.RenovationDetailsException;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.TagException;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UnauthenticatedException;
import nz.ac.canterbury.seng302.homehelper.model.calendar.Calendar;
import nz.ac.canterbury.seng302.homehelper.model.calendar.CalendarItem;
import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.service.*;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationMemberService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.TagService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;


/**
 * Controller responsible for handling individual renovation requests.
 */
@Controller
public class IndividualRenovationController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserService userService;
    private final RenovationService renovationService;
    private final CalendarService calendarService;
    private final RenovationRepository renovationRepository;
    private final TagService tagService;
    private final PaginationService paginationService;
    private final RenovationMemberService renovationMemberService;

    @Autowired
    public IndividualRenovationController(UserService userService, RenovationService renovationService,
                                          CalendarService calendarService, RenovationRepository renovationRepository,
                                          TagService tagService, PaginationService paginationService, RenovationMemberService renovationMemberService) {
        this.userService = userService;
        this.renovationService = renovationService;
        this.calendarService = calendarService;
        this.renovationRepository = renovationRepository;
        this.tagService = tagService;
        this.paginationService = paginationService;
        this.renovationMemberService = renovationMemberService;
    }

    /**
     * Handles GET requests for displaying the task calendar of an individual renovation.
     *
     * @param id       the ID of the renovation requesting the task calendar.
     * @param monthStr the month string of the calendar to show.
     * @param yearStr  the year string of the calendar to view.
     * @param dateStr  the date string of the calendar to view.
     * @param model    the model to populate attributes for the view.
     * @param request  the HTTP servlet request,
     * @return the name of the tasks calendar fragment template.
     * @throws UnauthenticatedException if the user is not unauthenticated.
     * @throws NoResourceFoundException if the renovation with the given ID was not found.
     */
    @GetMapping("/tasks-calendar/{id}")
    public String getIndividualRenovationTaskCalendar(@PathVariable long id,
                                                      @RequestParam(name = "month", required = false) String monthStr,
                                                      @RequestParam(name = "year", required = false) String yearStr,
                                                      @RequestParam(required = false) String dateStr,
                                                      @RequestParam(name = "states", required = false) List<TaskState> states,
                                                      Model model,
                                                      HttpServletRequest request
    ) throws NoResourceFoundException, UnauthenticatedException {
        logger.info("GET /myRenovations/{}/tasks-calendar - month={}, year={}", id, monthStr, yearStr);
        Renovation renovation = renovationService.getRenovation(id)
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Renovation not found"));

        // prevent users access to a private renovations task calendar if they have not created it
        try {
            User user = UserUtil.getUserFromHttpServletRequest(userService, request);
            if (!renovation.getIsPublic() && !renovation.isMember(user)) {
                throw new NoResourceFoundException(HttpMethod.GET, "Renovation not found");
            }
        } catch (UnauthenticatedException ignored) {
        }

        BiFunction<LocalDate, LocalDate, List<CalendarItem>> itemsSupplier = (start, end) ->
                renovationService.getTasksByDateRangeAndStates(start, end, id, states).stream()
                        .map(task -> (CalendarItem) task)
                        .toList();
        Calendar calendar = calendarService.parseAndGenerateCalendar(yearStr, monthStr, dateStr, itemsSupplier);
        model.addAttribute("calendar", calendar);
        model.addAttribute("renovation", renovation);
        model.addAttribute("highlightedDate", dateStr);
        return "fragments/renovation/individualRenovationTasksCalendarFragment";
    }

    /**
     * Gets a renovation form for editing a renovation
     *
     * @param id      the id of the renovation to be edited
     * @param model   the of the renovation with the prefilled information
     * @param request the request that is made to the server to find if a user is currently authenticated
     * @return the renovation form to edit a renovation, or a redirect to login if a user is not currently authenticated
     * or a redirect to the not found page if the renovation doesn't exit or doesn't belong to the user currently logged in
     */
    @GetMapping("myRenovations/{id}/editRenovation")
    public String editRenovation(@PathVariable(name = "id") long id,
                                 Model model, HttpServletRequest request) {
        Principal userPrincipal = request.getUserPrincipal();

        if (userPrincipal != null) {
            long userId = Long.parseLong(userPrincipal.getName());
            User user = userService.findUserById(userId);
            List<Renovation> renovations = renovationService.getRenovationByIdAndUser(id, user);
            if (renovations.isEmpty()) {
                return "redirect:/notFound";
            }
            Renovation renovation = renovations.getFirst();
            model.addAttribute("renovation", renovation);
            model.addAttribute("location", renovation.getLocation());
            List<String> roomNames = new ArrayList<>();
            roomNames.add("");
            roomNames.addAll(renovation.getRooms().stream().map(Room::getName).toList());
            List<Long> roomIds = new ArrayList<>();
            roomIds.add(-1L);
            roomIds.addAll(renovation.getRooms().stream().map(Room::getId).toList());
            model.addAttribute("roomNames", roomNames);
            model.addAttribute("roomIds", roomIds);
            model.addAttribute("isNewRenovation", false);
            model.addAttribute("redirectedUrl", request.getHeader("Referer"));
            return "pages/renovation/createEditRenovationPage";
        }
        return "redirect:/login";
    }

    /**
     * Posts a renovation form from editing
     *
     * @param id            the id of the renovation that has been edited
     * @param roomNames     a new list of room names for the renovation
     * @param roomIds       a list of roomIds with -1 if the room is a new room
     * @param model         the model give the html if updating renovation is not successful
     * @param request       the request sent by the browser to get the authenticated user from
     * @param redirectedUrl the url that directed the user to the edit page
     * @return the renovation form with errors shown if unsuccessful, or the renovation that was edited
     */
    @PostMapping("myRenovations/{id}/editRenovation")
    public String submitEditRenovation(@PathVariable(name = "id") long id,
                                       @ModelAttribute(name = "renovation") Renovation renovation,
                                       @RequestParam(name = "roomName[]", required = false) List<String> roomNames,
                                       @RequestParam(name = "roomIds[]", required = false) List<String> roomIds,
                                       @RequestParam(required = false) String redirectedUrl,
                                       @ModelAttribute("location") Location location,
                                       Model model, HttpServletRequest request) {
        Renovation renovationFromDB = renovationService.getRenovation(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Renovation with the provided ID was not found"));
        try {
            Principal userPrincipal = request.getUserPrincipal();
            long userId = Long.parseLong(userPrincipal.getName());
            User user = userService.findUserById(userId);
            renovationService.editRenovation(renovation, renovationFromDB, user, location, roomNames, roomIds);
        } catch (RenovationDetailsException e) {
            model.addAttribute("renovation", renovation);
            model.addAttribute("roomNames", roomNames);
            model.addAttribute("roomIds", roomIds);
            model.addAttribute("location", location);
            model.addAttribute("isNewRenovation", false);
            model.addAttribute("redirectedUrl", redirectedUrl);

            // Add errors to model
            renovationService.populateErrors(location, model, e);
            return "pages/renovation/createEditRenovationPage";
        }

        return "redirect:" + (redirectedUrl != null ? redirectedUrl : "/myRenovations/" + id);
    }

    /**
     * POST method to create a new tag and add it to a renovation
     *
     * @param id                 the ID of the renovation
     * @param tagName            the name of the tag
     * @param request            HttpServletRequest object
     * @param redirectAttributes container for flash attributes used during a redirect
     * @return redirection string to the renovation page
     */
    @PostMapping("myRenovations/{id}")
    public String submitTag(
            @PathVariable(name = "id") long id,
            @RequestParam(name = "tagName") String tagName,
            HttpServletRequest request, RedirectAttributes redirectAttributes
    ) {
        Renovation renovation = renovationService.getRenovation(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Renovation with the provided ID was not found"));
        Tag newTag = new Tag(tagName.toLowerCase(), renovation);
        try {
            tagService.save(newTag);
            logger.info("Added tag '{}' to renovation {}", newTag.getTag(), newTag.getRenovation().getId());
        } catch (TagException e) {
            redirectAttributes.addFlashAttribute("tagError", e.getMessage());
        }
        return "redirect:" + request.getHeader("Referer");
    }

    /**
     * Delete a tag from renovation
     *
     * @param id      the ID of the renovation
     * @param tagName the name of the tag
     * @param request HttpServletRequest object
     * @return redirection string to the renovation page
     */
    @PostMapping("myRenovations/{id}/deleteTag/")
    public String deleteTag(
            @PathVariable(name = "id") long id,
            @RequestParam(name = "tag") String tagName,
            HttpServletRequest request) {
        logger.info("POST /myRenovations/{id}/deleteTag/");
        List<Tag> tags = tagService.getByRenovationID(id);
        Tag tag = tags.stream().filter(t -> t.getTag().equals(tagName)).findFirst().orElse(null);
        if (tag != null) {
            tagService.remove(tag);
            logger.info("Removed tag '{}' from renovation {}", tag.getTag(), tag.getRenovation().getId());
        }
        return "redirect:" + request.getHeader("Referer");
    }
}
