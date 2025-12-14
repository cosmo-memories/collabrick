package nz.ac.canterbury.seng302.homehelper.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Budget;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Task;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.Pagination;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseCategory;
import nz.ac.canterbury.seng302.homehelper.model.renovation.TaskState;
import nz.ac.canterbury.seng302.homehelper.model.user.PublicUserDetails;
import nz.ac.canterbury.seng302.homehelper.service.*;
import nz.ac.canterbury.seng302.homehelper.service.chat.ChatChannelService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BudgetService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.TaskService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller responsible for handling individual renovation requests.
 */
@Controller
@RequestMapping("/renovation/{renovationId}")
public class NewIndividualRenovationController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AppConfig appConfig;
    private final PaginationService paginationService;
    private final RenovationService renovationService;
    private final UserService userService;
    private final BudgetService budgetService;
    private final TaskService taskService;
    private final ChatChannelService chatChannelService;

    /**
     * Constructs a new NewIndividualRenovationController
     *
     * @param appConfig         - the application config
     * @param paginationService - the pagination service
     * @param renovationService - the renovation service
     * @param userService       - the user service
     */
    @Autowired
    public NewIndividualRenovationController(AppConfig appConfig, PaginationService paginationService,
                                             RenovationService renovationService, UserService userService,
                                             BudgetService budgetService, TaskService taskService,
                                             ChatChannelService chatChannelService) {
        this.appConfig = appConfig;
        this.paginationService = paginationService;
        this.renovationService = renovationService;
        this.userService = userService;
        this.budgetService = budgetService;
        this.taskService = taskService;
        this.chatChannelService = chatChannelService;
    }

    /**
     * Handles GET requests for the renovation overview page.
     *
     * @param model      The model object.
     * @param user       The authenticated user.
     * @param renovation The renovation being accessed.
     * @return The overview page template.
     */
    @GetMapping("")
    public String getRenovationOverview(
            Model model,
            @ModelAttribute("user") User user,
            @ModelAttribute("renovation") Renovation renovation
    ) throws NoResourceFoundException {
        Budget budget = budgetService.findByRenovationId(renovation.getId()).stream().findFirst()
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Renovation not found"));
        List<Expense> expenses = renovationService.getExpenses(renovation);
        model.addAttribute("budgetSum", budget.getBudgetSum());
        model.addAttribute("noOfExpenses", expenses.size());
        model.addAttribute("expenseSum", renovationService.getExpenseTotal(expenses));
        model.addAttribute("taskCount", taskService.countUncompletedTasksByRenovationId(renovation.getId()));
        return getRenovationPageContent(model, "overview");
    }

    /**
     * Handles GET requests for the renovation tasks page.
     *
     * @param model      The model object.
     * @param user       The authenticated user.
     * @param renovation The renovation being accessed.
     * @return The tasks page template.
     */
    @GetMapping("tasks")
    public String getRenovationTasks(
            Model model,
            @ModelAttribute("user") User user,
            @ModelAttribute("renovation") Renovation renovation,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "gotoPage", required = false) String gotoPage,
            @RequestParam(name = "states", required = false) List<TaskState> states,
            HttpServletRequest request
    ) {
        Pagination<Task> pagination = paginationService.paginate(
                page,
                gotoPage,
                pageable -> renovationService.getTaskListFiltered(renovation.getId(), states, pageable),
                request);
        model.addAttribute("states", states);
        model.addAttribute("pagination", pagination);
        return getRenovationPageContent(model, "tasks");
    }

    /**
     * Gets an individual view of a task
     *
     * @param taskId     the id of the task
     * @param model      the model object
     * @param user       the authenticated user
     * @param renovation the renovation object
     * @return the task page template
     * @throws NoResourceFoundException when no matching resource is found
     */
    @GetMapping("tasks/{taskId}")
    public String getRenovationTask(
            @PathVariable(name = "taskId") long taskId,
            Model model,
            @ModelAttribute("user") User user,
            @ModelAttribute("renovation") Renovation renovation) throws NoResourceFoundException {
        Task task = taskService.findTaskById(taskId)
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Task not found"));
        if (task.getRenovation().equals(renovation)) {
            model.addAttribute("task", task);
            model.addAttribute("expenseTotal", task.getTotalCost());
            model.addAttribute("expenseItems", task.getExpenseCount());
            return getRenovationPageContent(model, "task");
        } else {
            throw new NoResourceFoundException(HttpMethod.GET, "Task not found");
        }
    }

    /**
     * Handles HTTP GET requests for the renovation chat page.
     *
     * @param channelId  channel ID connecting to
     * @param user       user connecting
     * @param renovation renovation the channel belongs to
     * @param model      model object
     * @return The chat page template.
     * @throws NoResourceFoundException if channel doesn't exist or no permissions
     */
    @GetMapping("chat/{channelId}")
    public String getChat(
            @PathVariable long channelId,
            @ModelAttribute("user") User user,
            @ModelAttribute("renovation") Renovation renovation,
            Model model,
            @RequestParam(name = "mentionTime", required = false) String mentionTime) throws NoResourceFoundException {

        ChatChannel channel = chatChannelService.findById(channelId)
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Channel not found"));

        if (channel.getRenovation() != renovation || !channel.getMembers().contains(user)) {
            throw new NoResourceFoundException(HttpMethod.GET, "Channel not found");
        }

        ArrayList<PublicUserDetails> members = new java.util.ArrayList<>(channel.getMembers().stream()
                .map(PublicUserDetails::new)
                .toList());
        model.addAttribute("mentionTime", mentionTime);
        model.addAttribute("memberList", members);
        model.addAttribute("channel", channel);
        model.addAttribute("userId", user.getId());
        model.addAttribute("renovationId", renovation.getId());
        return getRenovationPageContent(model, "chat");
    }


    /**
     * Handles GET requests for the renovation calendar page.
     *
     * @param model      The model object.
     * @param user       The authenticated user.
     * @param renovation The renovation being accessed.
     * @return The overview page template.
     */
    @GetMapping("calendar")
    public String getRenovationCalendar(
            Model model,
            @ModelAttribute("user") User user,
            @ModelAttribute("renovation") Renovation renovation,
            @RequestParam(name = "month", required = false) String monthStr,
            @RequestParam(name = "year", required = false) String yearStr,
            @RequestParam(required = false) String dateStr,
            @RequestParam(name = "states", required = false) List<TaskState> states,
            HttpServletRequest request
    ) {
        logger.info("GET task calender- month={}, year={}", monthStr, yearStr);
        String referer = request.getHeader("referer");
        if (referer != null && referer.contains("month")) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(referer);

            String month = builder.build().getQueryParams().getFirst("month");
            if (month != null && !month.trim().isEmpty()) {
                model.addAttribute("month", month);
            }
            String year = builder.build().getQueryParams().getFirst("year");
            if (year != null && !year.trim().isEmpty()) {
                model.addAttribute("year", year);
            }
        }
        model.addAttribute("states", states);
        model.addAttribute("dateStr", dateStr);
        return getRenovationPageContent(model, "calendar");
    }

    /**
     * Handles GET requests for the renovation budget page.
     *
     * @param model      The model object.
     * @param user       The authenticated user.
     * @param renovation The renovation being accessed.
     * @param isMember   Flag indicating if the user is a member of the renovation or not.
     * @return The budget page template.
     */
    @GetMapping("budget")
    public String getRenovationBudget(
            Model model,
            @ModelAttribute("user") User user,
            @ModelAttribute("renovation") Renovation renovation,
            @ModelAttribute("isMember") boolean isMember
    ) throws NoResourceFoundException {
        if (!isMember) {
            return "redirect:/renovation/" + renovation.getId();
        }

        Budget budget = budgetService.findByRenovationId(renovation.getId()).stream().findFirst()
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Renovation not found"));

        model.addAttribute("miscExpenses",
                renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.MISCELLANEOUS));
        model.addAttribute("miscBudget", budget.getMiscellaneousBudget());

        model.addAttribute("materialExpenses",
                renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.MATERIAL));
        model.addAttribute("materialBudget", budget.getMaterialBudget());

        model.addAttribute("labourExpenses",
                renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.LABOUR));
        model.addAttribute("labourBudget", budget.getLabourBudget());

        model.addAttribute("equipmentExpenses",
                renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.EQUIPMENT));
        model.addAttribute("equipmentBudget", budget.getEquipmentBudget());

        model.addAttribute("serviceExpenses",
                renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.PROFESSIONAL_SERVICES));
        model.addAttribute("serviceBudget", budget.getProfessionalServiceBudget());

        model.addAttribute("permitExpenses",
                renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.PERMIT));
        model.addAttribute("permitBudget", budget.getPermitBudget());

        model.addAttribute("cleanupExpenses",
                renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.CLEANUP));
        model.addAttribute("cleanupBudget", budget.getCleanupBudget());

        model.addAttribute("deliveryExpenses",
                renovationService.getExpenseTotalByCategory(renovation, ExpenseCategory.DELIVERY));
        model.addAttribute("deliveryBudget", budget.getDeliveryBudget());

        model.addAttribute("budgetSum", budget.getBudgetSum());
        model.addAttribute("expenseSum", renovationService.getExpenseTotal(renovationService.getExpenses(renovation)));
        model.addAttribute("renovation", renovation);
        return getRenovationPageContent(model, "budget");
    }

    /**
     * Handles GET requests for the renovation expenses page.
     *
     * @param model      The model object.
     * @param user       The authenticated user.
     * @param renovation The renovation being accessed.
     * @param isMember   Flag indicating if the user is a member of the renovation or not.
     * @return The expenses page template.
     */
    @GetMapping("expenses")
    public String getRenovationExpenses(
            Model model,
            @ModelAttribute("user") User user,
            @ModelAttribute("renovation") Renovation renovation,
            @ModelAttribute("isMember") boolean isMember
    ) {
        if (!isMember) {
            return "redirect:/renovation/" + renovation.getId();
        }
        List<Expense> expenses = renovationService.getExpenses(renovation);
        model.addAttribute("expenses", expenses);
        model.addAttribute("expenseTotal", renovationService.getExpenseTotal(expenses));
        return getRenovationPageContent(model, "expenses");
    }

    /**
     * Handles GET requests for the renovation members page.
     *
     * @param model      The model object.
     * @param user       The authenticated user.
     * @param renovation The renovation being accessed.
     * @return The members page template.
     */
    @GetMapping("members")
    public String getRenovationMembers(
            Model model,
            @ModelAttribute("user") User user,
            @ModelAttribute("renovation") Renovation renovation
    ) {

        return getRenovationPageContent(model, "members");
    }

    /**
     * Sets the content type for the renovation layout template. Responsible for choosing which content to render
     * on the individual renovation page.
     *
     * @param model    The model object.
     * @param pageName The name of the content page.
     * @return The layout template.
     */
    private String getRenovationPageContent(Model model, String pageName) {
        model.addAttribute("contentType", pageName);
        return "renovation/layout";
    }
}
