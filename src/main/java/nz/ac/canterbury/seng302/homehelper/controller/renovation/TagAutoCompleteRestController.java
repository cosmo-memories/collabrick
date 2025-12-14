package nz.ac.canterbury.seng302.homehelper.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.service.renovation.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller that handles tag auto-completion requests.
 * Provides an endpoint to return a list of tag suggestions based on user input.
 */
@RestController
public class TagAutoCompleteRestController {

    private final TagService tagService;

    /**
     * Constructs a TagAutoCompleteRestController with the given TagService.
     *
     * @param tagService the TagService used to perform tag searches
     */
    @Autowired
    public TagAutoCompleteRestController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * Endpoint for fetching tag suggestions based on a partial input string.
     *
     * @param tag     the partial tag input entered by the user
     * @param request the request sent by the browser to get the authenticated user from
     * @return a list of suggested tag strings matching the input
     */
    @GetMapping("/tagAutoComplete")
    public List<String> tagAutoComplete(@RequestParam() String tag,
                                        HttpServletRequest request) {
        List<String> tags = new ArrayList<>();
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null) {
            tags = tagService.search(tag, Long.parseLong(userPrincipal.getName()));
        } else {
            tags = tagService.search(tag, (long) -1);
        }
        return tags;
    }

    /**
     * Gets a list of autocompleted suggestions when the user searches for a tag
     *
     * @param searchTerm the search input from the user
     * @return a list of the tag names which is used by the frontend
     */
    @GetMapping("/tagAutoCompleteRenovation")
    public List<String> tagAutoCompleteRenovation(@RequestParam("term") String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        return tagService.getDistinctTagsBySearchTerm(searchTerm);
    }
}
