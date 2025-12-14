package nz.ac.canterbury.seng302.homehelper.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Room;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.TaskDetailsExceptions;
import nz.ac.canterbury.seng302.homehelper.model.activity.ActivityType;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseCategory;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseDto;
import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;
import nz.ac.canterbury.seng302.homehelper.repository.activity.ActivityRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.ExpenseRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationMemberRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.TaskRepository;
import nz.ac.canterbury.seng302.homehelper.service.*;
import nz.ac.canterbury.seng302.homehelper.service.activity.ActivityService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.*;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static nz.ac.canterbury.seng302.homehelper.model.activity.ActivityType.*;
import static nz.ac.canterbury.seng302.homehelper.validation.Validation.isDateValid;

@Controller
public class TaskController {

    private final RenovationService renovationService;
    private final UserService userService;
    private final TaskRepository taskRepository;
    private final TagService tagService;
    private final RenovationMemberService renovationMemberService;
    private final RenovationRepository renovationRepository;
    private final CalendarService calendarService;
    private final ExpenseRepository expenseRepository;
    private final TaskService taskService;
    private final ActivityService activityService;
    private final ActivityRepository activityRepository;

    Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    public TaskController(RenovationService renovationService, UserService userService, TaskRepository taskRepository,
                          TagService tagService, RenovationRepository renovationRepository, CalendarService calendarService,
                          ExpenseRepository expenseRepository, TaskService taskService, RenovationMemberRepository renovationMemberRepository, RenovationMemberService renovationMemberService, ActivityService activityService, ActivityRepository activityRepository) {
        this.renovationService = renovationService;
        this.userService = userService;
        this.taskRepository = taskRepository;
        this.tagService = tagService;
        this.renovationRepository = renovationRepository;
        this.calendarService = calendarService;
        this.expenseRepository = expenseRepository;
        this.taskService = taskService;
        this.renovationMemberService = renovationMemberService;
        this.activityService = activityService;
        this.activityRepository = activityRepository;
    }

    /**
     * Gets the task form to display
     *
     * @param id      the id of the renovation the task is for
     * @param dueDate the due date for the task if task form was accessed via the calendar
     * @param model   model to add information needed in the html:
     *                - the renovation this task is for
     *                - the title for the form
     *                - the name for the submit button
     *                - the tasks due date if accessed via the calendar
     * @return the task form
     */
    @GetMapping("/myRenovations/{id}/newTask")
    public String getTaskForm(@PathVariable(name = "id") long id,
                              @RequestParam(name = "taskDueDate", required = false) String dueDate,
                              @RequestHeader(value = "Referer", required = false) String pageReferer,
                              Model model, HttpServletRequest request) throws NoResourceFoundException {

        User user = UserUtil.getUserFromHttpServletRequest(userService, request);

        Renovation renovation = renovationService.getRenovation(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Renovation not found"));

        renovationMemberService.checkMembership(user, renovation);

        if (dueDate != null && isDateValid(dueDate)) {
            model.addAttribute("taskDueDate", dueDate);
        }
        if (pageReferer != null && pageReferer.contains("calendar")) {
            model.addAttribute("referer", "calendar");
        } else if (pageReferer != null && pageReferer.contains("tasks")) {
            model.addAttribute("referer", "tasks");
        }
        model.addAttribute("renovation", renovation);
        model.addAttribute("isNewTask", true);
        model.addAttribute("formTitle", "Add Task");
        model.addAttribute("buttonName", "Create");
        model.addAttribute("taskState", TaskState.NOT_STARTED);

        return "pages/renovation/createEditTaskPage";
    }

    /**
     * Gets fields from form to save task to renovation
     *
     * @param id          the id of the renovation
     * @param name        the name of the task
     * @param description the description of the task
     * @param dueDate     the due date of the task
     * @param roomIds     the ids of the rooms the task is associated with
     * @param dateInvalid a string that will be empty if the date is valid and non-empty if invalid
     * @param model       a model of the renovation to return as well as the task
     * @return a redirection to the individual renovation the task is associated with if creation is successful, or the task form with the errors on the task form visiable
     */
    @PostMapping("/myRenovations/{id}/newTask")
    public String submitTaskForm(@PathVariable(name = "id") long id,
                                 @RequestParam(name = "taskName") String name,
                                 @RequestParam(name = "taskDescription") String description,
                                 @RequestParam(name = "taskDueDate", required = false) String dueDate,
                                 @RequestParam(name = "roomId", required = false) List<Long> roomIds,
                                 @RequestParam(name = "dateInvalid") String dateInvalid,
                                 @RequestParam(name = "state") String stateName,
                                 @RequestParam(name = "referer", required = false) String referer,
                                 Model model, HttpServletRequest request) throws NoResourceFoundException {
        User user = UserUtil.getUserFromHttpServletRequest(userService, request);

        Renovation renovation = renovationService.getRenovation(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Renovation not found"));

        renovationMemberService.checkMembership(user, renovation);

        TaskState state = TaskState.fromString(stateName).orElse(TaskState.NOT_STARTED);
        try {
            Task task = new Task(renovation, name.trim(), description.trim(), state, "house.png");
            // ChatGPT was used to understand how the passing of the roomIds work with checkboxes, but it did not code this
            if (roomIds != null) {
                for (long roomId : roomIds) {
                    Room room = renovationService.findRoom(roomId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404)));
                    task.addRoom(room);
                }
            }
            if (!dateInvalid.isEmpty()) {
                renovationService.saveTask(id, task, dateInvalid);
            } else {
                renovationService.saveTask(id, task, dueDate);
            }

            // Save and send LiveUpdate:
            LiveUpdate update = new LiveUpdate(user, renovation, ActivityType.TASK_ADDED, task);
            try {
                activityService.saveLiveUpdate(update);
                activityService.sendUpdate(update);
            } catch (Exception e) {
                logger.error("Failed to send live update", e);
            }

            // Changes the URL to return to based on if the user came from the task or calendar tab
            String redirectUrl = "redirect:/renovation/" + id;
            if (referer != null && referer.contains("calendar") && dueDate != null && !dueDate.isEmpty()) {
                redirectUrl += "/calendar?dateStr=" + dueDate;
            } else {
                redirectUrl += "/tasks/" + task.getId();
            }
            return redirectUrl;
        } catch (TaskDetailsExceptions e) {
            model.addAttribute("renovation", renovation);
            model.addAttribute("taskName", name.trim());
            model.addAttribute("taskDescription", description.trim());
            model.addAttribute("taskDueDate", dueDate);
            model.addAttribute("roomIds", roomIds);
            if (!e.getNameErrorMessage().isEmpty())
                model.addAttribute("taskNameError", e.getNameErrorMessage());
            if (!e.getDescriptionErrorMessage().isEmpty())
                model.addAttribute("taskDescriptionError", e.getDescriptionErrorMessage());
            if (!e.getDueDateErrorMessage().isEmpty())
                model.addAttribute("taskDueDateError", e.getDueDateErrorMessage());
            model.addAttribute("isNewTask", true);
            model.addAttribute("taskState", state);
            model.addAttribute("referer", referer);
            return "pages/renovation/createEditTaskPage";
        }
    }

    /**
     * Gets task form to display, inserts into to model task details
     *
     * @param id          the id of the renovation this task is associated with
     * @param taskId      the id of the task that is being edited
     * @param model       the model of what to display on the edit form
     * @param pageReferer the referer which contains the page number the task to be edited was on
     * @return the task form
     */
    @GetMapping("/myRenovations/{id}/editTask/{taskId}")
    public String getEditTaskForm(@PathVariable(name = "id") long id,
                                  @PathVariable(name = "taskId") long taskId,
                                  @RequestHeader(value = "Referer", required = false) String pageReferer,
                                  Model model, HttpServletRequest request) throws NoResourceFoundException {

        User user = UserUtil.getUserFromHttpServletRequest(userService, request);

        Renovation renovation = renovationService.getRenovation(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Renovation not found"));

        renovationMemberService.checkMembership(user, renovation);

        model.addAttribute("renovation", renovation);
        Task task = renovationService.getTask(taskId);
        // ChatGPT was used to understand a referer
        int page = 1;
        if (pageReferer != null && pageReferer.contains("page=")) {
            try {
                page = Integer.parseInt(pageReferer.replaceAll(".*page=(\\d+).*", "$1"));

            } catch (NumberFormatException e) {
                // Leave page as 1
            }
        }
        if (pageReferer != null && pageReferer.contains("calendar")) {
            model.addAttribute("referer", "calendar");
        } else if (pageReferer != null && pageReferer.contains("tasks/")) {
            model.addAttribute("referer", "task");
        } else if (pageReferer != null && pageReferer.contains("tasks")) {
            model.addAttribute("referer", "tasks");
        }

        if (task.getDueDate() != null) {
            model.addAttribute("taskDueDate", task.getDueDate().toString());
        }
        model.addAttribute("task", task);
        model.addAttribute("taskName", task.getName().trim());
        model.addAttribute("taskDescription", task.getDescription().trim());
        List<Long> roomIds = task.getRooms().stream().map(Room::getId).toList();
        model.addAttribute("roomIds", roomIds);
        model.addAttribute("isNewTask", false);
        model.addAttribute("pageNumber", page);
        model.addAttribute("taskState", task.getState());
        return "pages/renovation/createEditTaskPage";
    }

    /**
     * Posts details from task form, edits a task.
     *
     * @param id          the id of the renovation
     * @param taskId      the id of the task
     * @param name        the name of the task from the edited form
     * @param description the description of the task from the edited form
     * @param dueDate     the due date of the task from the edited form
     * @param roomIds     a list of the ids of the rooms that are associated with the task
     * @param pageNumber  the page number the task the user edited was on
     * @param dateInvalid a string that will be empty if the date is valid and non-empty if invalid
     * @param model       the model of the task information to display if there is an error in the form
     * @param referer     the page that the user was on before going to the edit task page (tasks or calendar)
     * @param stateName   the name of the state/status the task is in
     * @param request     the request to see where to send a user back to after editing
     * @return if there are no errors in the form redirect to the individual renovation, or the task form with errors preventing submission
     */
    @PostMapping("/myRenovations/{id}/editTask/{taskId}")
    public String submitEditTaskForm(@PathVariable(name = "id") long id,
                                     @PathVariable(name = "taskId") long taskId,
                                     @RequestParam(name = "taskName") String name,
                                     @RequestParam(name = "taskDescription") String description,
                                     @RequestParam(name = "taskDueDate", required = false) String dueDate,
                                     @RequestParam(name = "roomId", required = false) List<Long> roomIds,
                                     @RequestParam(name = "pageNumber") int pageNumber,
                                     @RequestParam(name = "dateInvalid") String dateInvalid,
                                     @RequestParam(name = "referer", required = false) String referer,
                                     @RequestParam(name = "state") String stateName,
                                     Model model, HttpServletRequest request) throws NoResourceFoundException {

        User user = UserUtil.getUserFromHttpServletRequest(userService, request);
        Renovation renovation = renovationService.getRenovation(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Renovation not found"));

        renovationMemberService.checkMembership(user, renovation);

        Task task = renovationService.getTask(taskId);

        TaskState oldState = task.getState();
        TaskState newState = TaskState.fromString(stateName).orElse(TaskState.NOT_STARTED);
        String oldName = task.getName();
        String oldDescription = task.getDescription();
        LocalDate oldDueDate = null;
        if (task.getDueDate() != null) {
            oldDueDate = task.getDueDate();
        }
        List<Long> oldRooms = task.getRooms().stream().map(Room::getId).toList();

        try {
            task.setName(name);
            task.setDescription(description);
            task.setState(newState);
            if (roomIds != null) {
                task.setRooms(null);
                for (long roomId : roomIds) {
                    Room room = renovationService.findRoom(roomId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404)));
                    task.addRoom(room);
                }
            } else {
                task.setRooms(null);
            }
            if (!dateInvalid.isEmpty()) {
                renovationService.saveTask(id, task, dateInvalid);
            } else {
                renovationService.saveTask(id, task, dueDate);
                LocalDate newDate = null;
                if (dueDate != null && !dueDate.isEmpty()) {
                    newDate = LocalDate.parse(dueDate);
                }
                // Save and send LiveUpdate:
                if (!Objects.equals(oldName, name) || !Objects.equals(oldDescription, description) ||
                        !Objects.equals(oldDueDate, newDate) || !Objects.equals(oldRooms, roomIds)) {
                    LiveUpdate update = new LiveUpdate(user, renovation, ActivityType.TASK_EDITED, task);
                    try {
                        activityService.saveLiveUpdate(update);
                        activityService.sendUpdate(update);
                    } catch (Exception e) {
                        logger.error("Failed to send live update", e);
                    }
                }

                // If state was changed, send update:
                if (oldState != newState) {

                    LiveUpdate taskUpdate = switch (oldState) {
                        case NOT_STARTED -> new LiveUpdate(user, renovation, TASK_CHANGED_FROM_NOT_STARTED, task);
                        case IN_PROGRESS -> new LiveUpdate(user, renovation, TASK_CHANGED_FROM_IN_PROGRESS, task);
                        case COMPLETED -> new LiveUpdate(user, renovation, TASK_CHANGED_FROM_COMPLETED, task);
                        case CANCELLED -> new LiveUpdate(user, renovation, TASK_CHANGED_FROM_CANCELLED, task);
                        case BLOCKED -> new LiveUpdate(user, renovation, TASK_CHANGED_FROM_BLOCKED, task);
                    };

                    try {
                        activityService.saveLiveUpdate(taskUpdate);
                        activityService.sendUpdate(taskUpdate);
                    } catch (Exception e) {
                        logger.error("Failed to send live update", e);
                    }
                }

            }

            // Changes the URL to return to based on if the user came from the task or calendar tab
            String redirectUrl = "redirect:/renovation/" + id;
            if (referer != null && referer.contains("calendar")) {
                redirectUrl += "/calendar?dateStr=" + dueDate;
            } else {
                redirectUrl += "/tasks/" + taskId;
            }
            return redirectUrl;
        } catch (TaskDetailsExceptions e) {
            model.addAttribute("renovation", renovation);
            model.addAttribute("task", task);
            model.addAttribute("taskName", name.trim());
            model.addAttribute("taskDescription", description.trim());
            model.addAttribute("taskDueDate", dueDate);
            model.addAttribute("roomIds", roomIds);
            model.addAttribute("pageNumber", pageNumber);
            if (!e.getNameErrorMessage().isEmpty())
                model.addAttribute("taskNameError", e.getNameErrorMessage());
            if (!e.getDescriptionErrorMessage().isEmpty())
                model.addAttribute("taskDescriptionError", e.getDescriptionErrorMessage());
            if (!e.getDueDateErrorMessage().isEmpty())
                model.addAttribute("taskDueDateError", e.getDueDateErrorMessage());
            model.addAttribute("isNewTask", false);
            model.addAttribute("taskState", newState);
            model.addAttribute("referer", referer);
            return "pages/renovation/createEditTaskPage";
        }
    }

    /**
     * Gets the submitted icon change and redirects back to the renovation view
     *
     * @return thymeleaf individual renovation
     */
    @PostMapping("/renovation/{id}/saveIcon")
    public String submitTaskIcon(@PathVariable(name = "id") long id,
                                 @RequestParam(name = "taskId") long taskId,
                                 @RequestParam(name = "selectedIcon") String imageFilename) {
        Task task = renovationService.getTask(taskId);
        if (imageFilename != null) {
            task.setIconFileName(imageFilename);
            LocalDate dueDate = task.getDueDate();
            String dueDateString = null;
            if (dueDate != null) {
                dueDateString = dueDate.toString();
            }
            renovationService.saveTask(id, task, dueDateString);
        }
        return "redirect:/renovation/" + id + "/tasks";
    }

    /**
     * Get individual Task page containing task details and expenses information.
     * Will return a Not Found page if Task ID does not belong to the given Renovation.
     *
     * @param id     Renovation ID
     * @param taskId Task ID
     * @param model  Model
     * @return Individual task page
     */
    @GetMapping("/myRenovations/{renovationId}/task/{taskId}")
    public String getEditTaskForm(@PathVariable(name = "renovationId") long id,
                                  @PathVariable(name = "taskId") long taskId,
                                  Model model, HttpServletRequest request) throws NoResourceFoundException {
        User user = UserUtil.getUserFromHttpServletRequest(userService, request);

        Renovation renovation = renovationService.getRenovation(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Renovation not found"));

        renovationMemberService.checkMembership(user, renovation);

        model.addAttribute("renovation", renovation);

        Task task = taskRepository.findTaskById(taskId)
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Task not found"));
        model.addAttribute("task", task);
        if (task.getRenovation().equals(renovation)) {
            model.addAttribute("expenseTotal", task.getTotalCost());
            model.addAttribute("expenseItems", task.getExpenseCount());
            return "pages/task/individualTaskPage";
        } else {
            return "pages/notFoundPage";
        }
    }

    /**
     * Displays the form to add expenses to a task.
     *
     * @param id      ID of the renovation
     * @param taskId  ID of the task
     * @param model   Spring model
     * @param request HTTP request to retrieve user
     * @return expense form page or not found page
     * @throws NoResourceFoundException if task or renovation not found
     */
    @GetMapping("/myRenovations/{id}/addTaskExpenses/{taskId}")
    public String getTaskExpensesForm(@PathVariable(name = "id") long id,
                                      @PathVariable(name = "taskId") long taskId,
                                      Model model, HttpServletRequest request) throws NoResourceFoundException {

        User user = UserUtil.getUserFromHttpServletRequest(userService, request);
        Renovation renovation = renovationService.getRenovation(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Renovation not found"));

        renovationMemberService.checkMembership(user, renovation);

        Task task = taskRepository.findTaskById(taskId)
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Task not found"));

        model.addAttribute("expense", new Expense());
        model.addAttribute("renovation", renovation);
        model.addAttribute("task", task);

        if (task.getRenovation().equals(renovation)) {
            return "pages/task/taskAddExpensesPage";
        } else {
            return "pages/notFoundPage";
        }
    }

    /**
     * Handles submission of expense list to be added to a task.
     *
     * @param id       ID of the renovation
     * @param taskId   ID of the task
     * @param expenses list of expenses (as DTOs) to add
     * @return HTTP 200 OK or 400 Bad Request with error message
     */
    @PostMapping("/myRenovations/{id}/addTaskExpenses/{taskId}")
    @ResponseBody
    public ResponseEntity<?> submitTaskExpensesForm(@PathVariable(name = "id") long id,
                                                    @PathVariable(name = "taskId") long taskId,
                                                    @RequestBody List<ExpenseDto> expenses,
                                                    HttpServletRequest request) throws NoResourceFoundException {
        Renovation renovation = renovationService.getRenovation(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Renovation not found"));
        User user = UserUtil.getUserFromHttpServletRequest(userService, request);

        renovationMemberService.checkMembership(user, renovation);

        Task task = taskRepository.findTaskById(taskId)
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Task not found"));

        for (ExpenseDto expense : expenses) {

            try {
                ExpenseCategory category = ExpenseCategory.fromDisplayName(expense.category.trim());
                BigDecimal cost = new BigDecimal(String.format("%s", expense.price));
                LocalDate date = LocalDate.parse(expense.date);

                Expense expenseToAdd = new Expense(task, expense.name, category, cost, date);
                taskService.saveExpense(expenseToAdd);

                // Save and send LiveUpdate:
                LiveUpdate update = new LiveUpdate(user, renovation, ActivityType.EXPENSE_ADDED, expenseToAdd, task);
                try {
                    activityService.saveLiveUpdate(update);
                    activityService.sendUpdate(update);
                } catch (Exception e) {
                    logger.error("Failed to send live update", e);
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Expense not added");
            }
        }

        return ResponseEntity.ok("renovation/" + id + "/tasks/" + taskId);
    }

    /**
     * Delete expense from a task.
     *
     * @param taskId    Task ID
     * @param expenseId Expense ID
     * @return Individual task page
     */
    @PostMapping("task/{taskId}/expense/{expenseId}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteExpense(@PathVariable(name = "taskId") long taskId,
                                           @PathVariable(name = "expenseId") long expenseId) {
        try {
            taskService.removeExpenseFromTask(expenseId, taskId);
            return ResponseEntity.ok().body("Deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Expense not found");
        }
    }
}
