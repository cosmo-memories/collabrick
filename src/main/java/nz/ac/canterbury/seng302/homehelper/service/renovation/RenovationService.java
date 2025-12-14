package nz.ac.canterbury.seng302.homehelper.service.renovation;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.*;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.RenovationDetailsException;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.TaskDetailsExceptions;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseCategory;
import nz.ac.canterbury.seng302.homehelper.model.renovation.OwnershipFilter;
import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.*;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static nz.ac.canterbury.seng302.homehelper.validation.renovation.RenovationValidation.*;

/**
 * Service class for Renovation, defined by the @link{Service} annotation.
 * This class links automatically with @link{RenovationRepository} and @link{RoomRepository}, see
 * the @link{Autowired} annotation below
 */
@Service
public class RenovationService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RenovationRepository renovationRepository;
    private final RenovationMemberRepository renovationMemberRepository;
    private final RoomRepository roomRepository;
    private final TaskRepository taskRepository;
    private final LocationService locationService;
    private final ExpenseRepository expenseRepository;
    private final ChatChannelService chatChannelService;
    private final BrickAiService brickAiService;
    private final UserService userService;

    public RenovationService(RenovationRepository renovationRepository, RenovationMemberRepository renovationMemberRepository, RoomRepository roomRepository, TaskRepository taskRepository, LocationService locationService, ExpenseRepository expenseRepository, ChatChannelService chatChannelService, BrickAiService brickAiService, UserService userService) {
        this.renovationRepository = renovationRepository;
        this.renovationMemberRepository = renovationMemberRepository;
        this.roomRepository = roomRepository;
        this.taskRepository = taskRepository;
        this.locationService = locationService;
        this.expenseRepository = expenseRepository;
        this.chatChannelService = chatChannelService;
        this.brickAiService = brickAiService;
        this.userService = userService;
    }


    /**
     * Get all renovations that match a user
     *
     * @param user the user to find the renovations that belong to it
     * @return list of renovations that match the user
     */
    public Page<Renovation> getUserRenovations(User user, Pageable pageable) {
        return renovationRepository.findByUserId(user, pageable);
    }

    /**
     * Gets a list renovation that matches a user and a renovation id
     *
     * @param renovationId the renovation id to look for
     * @param user         the user to look for the renovation with
     * @return list renovation that matches the renovationId and the user
     */
    public List<Renovation> getRenovationByIdAndUser(Long renovationId, User user) {
        return renovationRepository.findByRenovationIdAndUser(renovationId, user);
    }

    public List<RenovationMember> getRenovationMembers(Renovation renovation) {
        return renovationMemberRepository.findByRenovation(renovation);
    }

    /**
     * Toggles whether BrickAI is allowed to access the given renovation's details,
     * then persists the updated entity.
     *
     * <p>This flips the current {@code allowBrickAI} value (true → false, false → true)
     * and invokes {@link RenovationRepository#save(Object)} to store the change.</p>
     *
     * @param renovation the renovation to update; must not be {@code null}
     * @throws NullPointerException if {@code renovation} is {@code null}
     */
    public void toggleAllowBrickAI(Renovation renovation) {
        renovation.setAllowBrickAI(!renovation.isAllowBrickAI());
        User brickAI = brickAiService.getAiUser();
        if (renovation.isAllowBrickAI()) {
            for (ChatChannel channel : renovation.getChatChannels()) {
                chatChannelService.addMemberToChatChannel(channel.getId(), brickAI.getId());
            }
            for (RenovationMember member : renovation.getMembers()) {
                brickAiService.createAiChannel(renovation, member.getUser());
            }
        } else {
            removeChatChannel(renovation, "brickAI");
            for (ChatChannel channel : renovation.getChatChannels()) {
                chatChannelService.removeMemberFromChatChannel(channel, brickAI);
            }
        }
        renovationRepository.save(renovation);
    }

    /**
     * Saves a renovation to the database.
     * Performs a data validation check on submitted renovation.
     *
     * @param renovation The renovation to be added.
     * @param user       The user that owns the renovation being added
     * @return the saved renovation.
     */
    @Transactional(rollbackOn = RenovationDetailsException.class)
    public Renovation saveRenovation(Renovation renovation, User user) throws RenovationDetailsException {
        validateRenovation(renovation, user);

        // fix to prevent spring automatically saving the renovation before we do manually
        // only connect the renovation to the user when we are ready to save
        renovation.setOwner(user);
        return renovationRepository.save(renovation);
    }

    /**
     * Edits a renovation then saves it to the database
     * Performs validation
     * @param renovation the renovation with edited details
     * @param renovationFromDB the renovation with details to be changed
     * @param user the user
     * @param location the location of the renovation
     * @param roomNames the names of the rooms submitted
     * @param roomIds the ids of already existing rooms
     * @throws RenovationDetailsException errors when renovation validation does not pass
     */
    @Transactional(rollbackOn = RenovationDetailsException.class)
    public void editRenovation(Renovation renovation, Renovation renovationFromDB, User user, Location location, List<String> roomNames, List<String> roomIds) throws RenovationDetailsException {
        renovation.setLocation(location);
        validateRenovation(renovation, user);

        renovationFromDB.setName(renovation.getName().trim());
        renovationFromDB.setDescription(renovation.getDescription().trim());
        renovationFromDB.setLocation(location);

        List<Room> rooms = renovationFromDB.getRooms();
        if (roomIds != null) {
            List<Long> roomIdLongs = roomIds.stream().map(Long::parseLong).toList();
            List<Long> roomsToRemove = rooms.stream()
                    .map(Room::getId)
                    .filter(roomId -> !roomIdLongs.contains(roomId))
                    .toList();
            for (Long roomId : roomsToRemove) {
                removeRoomFromRenovation(renovationFromDB.getId(), roomId);
            }
            if (!roomNames.isEmpty()) {
                for (int i = 0; i < roomIdLongs.size(); i++) {
                    if (roomIdLongs.get(i) == -1L) {
                        String roomName = roomNames.get(i);
                        addRoomToRenovation(renovationFromDB.getId(), roomName);
                    }
                }
            }
        }

        renovationFromDB.setOwner(user);
        renovationRepository.save(renovationFromDB);
    }

    /**
     * Adds a new renovation to the database.
     * Performs a data validation check on submitted renovation.
     * Also creates an AI chat channel between the renovation owner and BrickAI.
     *
     * @param renovation The renovation to be added.
     * @param roomNames  The list of room names to be added to renovation.
     * @param user       The user that owns the renovation being added
     * @return the saved renovation.
     */
    public Renovation createRenovation(Renovation renovation, List<String> roomNames, User user) throws RenovationDetailsException {
        validateRenovation(renovation, user);

        List<Room> rooms = roomNames.stream().filter(name -> !name.isBlank()).map(room -> new Room(renovation, room)).toList();
        renovation.getRooms().addAll(rooms);
        Renovation savedRenovation = saveRenovation(renovation, user);
        chatChannelService.createChannel(savedRenovation, "general");
        brickAiService.createAiChannel(renovation, user);
        return savedRenovation;
    }

    private void validateRenovation(Renovation renovation, User user) {
        String nameErrors = validateRenovationName(renovationRepository, renovation.getName(), renovation.getId(), user);
        String descriptionErrors = validateRenovationDescription(renovation.getDescription());
        ArrayList<String> roomErrors = new ArrayList<>();
        for (Room room: renovation.getRooms()) {
            String error = validateRoomName(room.getName());
            if (!error.isBlank()) {
                roomErrors.add(error);
            }
        }
        locationService.validateRenovationLocation(renovation.getLocation(), nameErrors, descriptionErrors, roomErrors);
        if (!nameErrors.isEmpty() || !descriptionErrors.isEmpty() || !roomErrors.isEmpty()) {
            throw new RenovationDetailsException(nameErrors, descriptionErrors, roomErrors);
        }
    }

    /**
     * Returns Renovation by id
     *
     * @param id ID of renovation to be found
     * @return returns renovation else returns Optional.empty()
     */
    public Optional<Renovation> findRenovation(long id) {
        return renovationRepository.findById(id);
    }

    /**
     * Returns Room by id
     *
     * @param id ID of the room to be found
     * @return returns room else returns Optional.empty()
     */
    public Optional<Room> findRoom(long id) {
        return roomRepository.findByRoomId(id);
    }

    /**
     * Removes a renovation from the database.
     *
     * @param renovation The renovation to be removed.
     */
    public void removeRenovation(Renovation renovation) {
        renovationRepository.delete(renovation);
    }

    /**
     * Gets a renovation by its id.
     *
     * @param id The id of the renovation.
     */
    public Optional<Renovation> getRenovation(long id) {
        return renovationRepository.findById(id);
    }

    /**
     * Adds a room to a specified renovation. If the room does not exist, it is created and saved before being added
     * to the renovation.
     *
     * @param renovationId The ID of the renovation to which the room should be added.
     * @param roomName     The name of the room to be added.
     */
    @Transactional
    public String addRoomToRenovation(long renovationId, String roomName) {

        Renovation renovation = renovationRepository.findById(renovationId).orElseThrow();
        String errors = validateRoomName(roomName.trim());
        if (errors.isEmpty()) {
            Room room = new Room(renovation, roomName.trim());
            //This cascades to add the room to the room table as required so we do not need manual entry
            renovation.addRoom(room);
        }
        return errors;

    }

    /**
     * Removes a room from a specified renovation.
     *
     * @param renovationId The ID of the renovation from which the room should be removed.
     * @param roomId       The id of the room to be removed.
     */
    @Transactional
    public void removeRoomFromRenovation(long renovationId, long roomId) {
        Renovation renovation = renovationRepository.findById(renovationId).orElseThrow();
        Room room = roomRepository.findById(roomId).orElseThrow();

        renovation.removeRoom(room);
        roomRepository.delete(room);
    }

    /**
     * Saves a task
     *
     * @param renovationId The ID of the renovation to which the task should be saved.
     * @param dueDate      the string of the dueDate
     * @param task         The task to be saved.
     */
    @Transactional
    public void saveTask(long renovationId, Task task, String dueDate) {
        String nameErrors = validateTaskName(task.getName());
        String descriptionErrors = validateTaskDescription(task.getDescription());
        String dueDateErrors = "";
        if (dueDate != null && !dueDate.isEmpty()) {
            dueDateErrors = validateTaskDueDate(dueDate);
        }
        if (nameErrors.isEmpty() && descriptionErrors.isEmpty() && dueDateErrors.isEmpty()) {
            Renovation renovation = renovationRepository.findById(renovationId).orElseThrow();
            if (dueDate != null && !dueDate.isEmpty()) {
                task.setDueDate(LocalDate.parse(dueDate));
            } else {
                task.setDueDate(null);
            }
            renovation.addTask(task);
            renovationRepository.save(renovation);
        } else {
            throw new TaskDetailsExceptions(nameErrors, descriptionErrors, dueDateErrors);
        }
    }

    /**
     * @param renoId   the id of the renovation
     * @param pageable the amount you want on a page
     * @return the tasks which will fit on the page
     */
    public Page<Task> getTaskListFiltered(long renoId, List<TaskState> states, Pageable pageable) {
        if (states == null || states.isEmpty()) {
            return taskRepository.findByRenovationId(renoId, pageable);
        } else {
            return taskRepository.findByRenovationIdAndStateIn(renoId, states, pageable);
        }
    }

    /**
     * Gets a task
     *
     * @param taskId to be found
     */
    public Task getTask(long taskId) {
        return taskRepository.findTaskById(taskId).orElseThrow();
    }

    /**
     * Gets tasks of a renovationId within a date range and is in a relevant state.
     *
     * @param startDate
     * @param endDate
     * @param renovationId
     * @param states
     * @return
     */
    public List<Task> getTasksByDateRangeAndStates(LocalDate startDate, LocalDate endDate, Long renovationId, List<TaskState> states) {
        if (states == null || states.isEmpty()) {
            return taskRepository.findTasksByDateRange(startDate, endDate, renovationId);
        } else {
            return taskRepository.findTasksByDateRangeAndStates(startDate, endDate, renovationId, states);
        }
    }

    /**
     * Add error messages to model if there are problems with the create/edit Renovation forms.
     *
     * @param location Location object
     * @param model    Model object
     * @param e        RenovationDetailsException containing error messages
     */
    public void populateErrors(@ModelAttribute("location") Location location, Model model, RenovationDetailsException e) {
        if (!e.getNameErrorMessage().isEmpty()) {
            model.addAttribute("renovationNameError", e.getNameErrorMessage());
        }
        if (!e.getDescriptionErrorMessage().isEmpty()) {
            model.addAttribute("renovationDescriptionError", e.getDescriptionErrorMessage());
        }
        locationService.populateLocationErrors(location, model, e);
    }

    /**
     * Retrieves a paginated list of public Renovation entities that match the given search query and tags.
     * The query and tags are normalised (trimmed and lowercased) before being passed to the repository
     *
     * @param query    the search string to match against renovation names or descriptions.
     * @param tags     a list of tag names to filter renovations by.
     * @param pageable pagination and sorting information.
     * @return a Page of public Renovation entities matching the criteria.
     */
    public Page<Renovation> findPublicRenovations(String query, List<String> tags, Pageable pageable) {
        String normalisedQuery = normaliseQuery(query);
        List<String> normalisedSearchTags = normaliseTags(tags);
        Page<Renovation> renovations = renovationRepository.findPublic(normalisedQuery, normalisedSearchTags, pageable);
        logger.info("Found {} matching public renovations", renovations.getNumberOfElements());
        return renovations;
    }

    /**
     * Retrieves a paginated list of Renovation entities owned by a specific user that match the given search query
     * and tags.
     * The query and tags are normalised (trimmed and lowercased) before being passed to the repository
     *
     * @param user     the User who owns the renovations.
     * @param query    the search string to match against renovation names or descriptions.
     * @param tags     a list of tag names to filter renovations by.
     * @param pageable pagination and sorting information.
     * @return a Page of Renovation entities owned by the user and matching the criteria.
     */
    public Page<Renovation> findUsersRenovations(User user, String query, List<String> tags, Pageable pageable) {
        String normalisedQuery = normaliseQuery(query);
        List<String> normalisedSearchTags = normaliseTags(tags);
        Page<Renovation> renovations = renovationRepository.findByUser(user, normalisedQuery, normalisedSearchTags, pageable);
        logger.info("Found {} matching public renovations for specific user:", renovations.getNumberOfElements());
        return renovations;
    }

    /**
     * Retrieves a paginated list of Renovation entities that a user has access to
     * The query and tags are normalised (trimmed and lowercased) before being passed to the repository
     *
     * @param user            the User who owns the renovations.
     * @param ownershipFilter the filter of who owns the renovations
     * @param pageable        pagination and sorting information.
     * @return a Page of Renovation entities owned by the user and matching the criteria.
     */
    public Page<Renovation> findUsersAccessibleRenovations(User user, OwnershipFilter ownershipFilter, Pageable pageable) {
        Page<Renovation> renovations = null;
        switch (ownershipFilter) {
            case ALL -> renovations = renovationRepository.findByUserOrMembership(user, pageable);
            case OWNED_BY_ME -> renovations = renovationRepository.findUserRenovations(user, pageable);
            case SHARED_WITH_ME -> renovations = renovationRepository.findByUserWhereUserIsMember(user, pageable);
        }
        return renovations;
    }


    /**
     * Normalizes a search query string by trimming whitespace and converting it to lowercase.
     *
     * @param query the raw query string, may be null.
     * @return a normalized query string, or an empty string if null.
     */
    private String normaliseQuery(String query) {
        return query == null ? "" : query.trim().toLowerCase();
    }

    /**
     * Normalizes a list of tag strings by trimming whitespace and converting each tag to lowercase.
     *
     * @param tags the list of raw tag strings, may be null.
     * @return a list of normalized tag strings, or an empty list if null.
     */
    private List<String> normaliseTags(List<String> tags) {
        return tags == null ? List.of() : tags
                .stream()
                .map(tag -> tag.trim().toLowerCase())
                .toList();
    }

    /**
     * Get all Expenses for a Renovation.
     *
     * @param renovation Renovation
     * @return List of Expenses
     */
    public List<Expense> getExpenses(Renovation renovation) {
        return expenseRepository.findByRenovationId(renovation.getId());
    }

    /**
     * Sum all provided Expenses.
     *
     * @param expenses List of Expenses
     * @return BigDecimal sum of Expenses
     */
    public BigDecimal getExpenseTotal(List<Expense> expenses) {
        BigDecimal total = new BigDecimal(("0.00"));
        for (Expense expense : expenses) {
            total = total.add(expense.getExpenseCost());
        }
        return total;
    }

    /**
     * Get list of Expenses for the given Renovation and ExpenseCategory.
     *
     * @param renovation Renovation
     * @param category   ExpenseCategory
     * @return List of Expenses
     */
    public List<Expense> getExpensesByCategory(Renovation renovation, ExpenseCategory category) {
        return expenseRepository.findByRenovationAndCategory(renovation, category);
    }

    /**
     * Get sum of Expenses for the given Renovation and ExpenseCategory.
     *
     * @param renovation Renovation
     * @param category   ExpenseCategory
     * @return List of Expenses
     */
    public BigDecimal getExpenseTotalByCategory(Renovation renovation, ExpenseCategory category) {
        BigDecimal sum = expenseRepository.sumByRenovationAndCategory(renovation, category);
        return (sum != null) ? sum : new BigDecimal("0.00");
    }

    /**
     * Get the total number of renovations the given user is associated with.
     * @param user      User
     * @return          Integer
     */
    public int sumRenovationsForUser(User user) {
        return renovationRepository.sumForUser(user);
    }

    /**
     * Removes all chat channels with a certain name from a renovation's list of chat channels
     *
     * @param renovation the renovation to remove chat channel from
     * @param channelName name of channel/s to remove
     */
    private void removeChatChannel(Renovation renovation, String channelName) {
        renovation.getChatChannels().removeIf(c -> c.getName().equals(channelName));
    }

}
