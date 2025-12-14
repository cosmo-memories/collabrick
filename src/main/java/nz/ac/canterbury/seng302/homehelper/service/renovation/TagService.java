package nz.ac.canterbury.seng302.homehelper.service.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Tag;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.TagException;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.TagRepository;
import nz.ac.canterbury.seng302.homehelper.service.ProfanityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static nz.ac.canterbury.seng302.homehelper.validation.Validation.isTagValid;

/**
 * Service class for Tags
 */
@Service
public class TagService {

    final int MAX_SUGGESTIONS = 10;
    private final TagRepository tagRepository;
    private final RenovationRepository renovationRepository;
    private final ProfanityService profanityService;
    Logger logger = LoggerFactory.getLogger(TagService.class);

    /**
     * Constructor; takes a tagRepository, renovationService, and RenovationRepository
     *
     * @param tagRepository        Tag Repository object
     * @param renovationRepository Renovation Repository object
     */
    @Autowired
    public TagService(TagRepository tagRepository, RenovationRepository renovationRepository, ProfanityService profanityService) {
        this.tagRepository = tagRepository;
        this.renovationRepository = renovationRepository;
        this.profanityService = profanityService;
    }

    /**
     * Find tags that match the supplied renovation ID
     *
     * @param id ID of renovation
     * @return List of matching tags
     */
    public List<Tag> getByRenovationID(long id) {
        return tagRepository.findByRenovation(id);
    }

    /**
     * Find renovations that match the supplied tag text
     *
     * @param tag Tag text string
     * @return List of matching renovations
     */
    public List<Tag> getByTag(String tag) {
        return tagRepository.findByTag(tag);
    }

    /**
     * Save new tag to the database
     *
     * @param newTag Tag object to save
     * @throws TagException Exception thrown if tag cannot be saved
     */
    public void save(Tag newTag) throws TagException {
        Renovation renovation = newTag.getRenovation();
        if (!isTagValid(newTag.getTag())) {
            throw new TagException("Tag must contain at least one letter and have a maximum length of 32 characters");
        }
        if (newTag.getRenovation().getTags().size() >= 5) {
            throw new TagException("Renovations can't have more than 5 tags.");
        }

        // Needs this to check for duplicates, contains() doesn't always work for how the Tags are set up
        boolean tagExists = renovation.getTags().stream()
                .anyMatch(tag -> tag.getTag().equalsIgnoreCase(newTag.getTag()));
        if (tagExists) {
            throw new TagException("Can't create a duplicate tag.");
        }

        if (profanityService.containsProfanity(newTag.getTag())) {
            throw new TagException("Tag does not follow the system language standards");
        }
        renovation.addTag(newTag);
        renovationRepository.save(renovation);
    }

    /**
     * Remove tag from database
     *
     * @param tag Tag to be removed
     */
    public void remove(Tag tag) {
        Renovation reno = tag.getRenovation();
        if (reno != null && reno.getTags().contains(tag)) {
            reno.getTags().remove(tag);
            renovationRepository.save(reno);
        }
    }

    /**
     * Gets all distinct tag names from tags owned by the user
     *
     * @param searchTerm the search input from the user
     * @return a list of the tag names
     */
    public List<String> getDistinctTagsBySearchTerm(String searchTerm) {
        return tagRepository.getDistinctTagsBySearchTerm(searchTerm);
    }

    /**
     * Search for tags that match the search string, only tags that belong to public renovations,
     * or renovation owned by logged-in user
     *
     * @param query the search string
     * @param id    the user id that is making the search
     */
    public List<String> search(String query, Long id) {
        List<String> matches = tagRepository.searchForOwnedAndPublicTags(query, id);
        int end = Math.min(matches.size(), MAX_SUGGESTIONS);
        return matches.subList(0, end);
    }

    /**
     * Gets a list of renovations from the tag repository that matches a list of tags
     *
     * @param tags       the tags the renovation must have
     * @param searchTerm the term the user is searching by
     * @param pageable   to ensure the renovations are returned in a pageable form
     * @return a list of public renovations that match the given tags
     */
    public Page<Renovation> getPublicRenovationsByTags(List<String> tags, String searchTerm, Pageable pageable) {
        return tagRepository.findPublicRenovationByTags(tags, tags.size(), searchTerm, pageable);
    }
}
