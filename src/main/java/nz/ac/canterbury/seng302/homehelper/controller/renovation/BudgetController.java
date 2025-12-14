package nz.ac.canterbury.seng302.homehelper.controller.renovation;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Budget;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.BudgetException;
import nz.ac.canterbury.seng302.homehelper.exceptions.auth.UnauthenticatedException;
import nz.ac.canterbury.seng302.homehelper.model.activity.ActivityType;
import nz.ac.canterbury.seng302.homehelper.model.renovation.BudgetDto;
import nz.ac.canterbury.seng302.homehelper.service.activity.ActivityService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BudgetService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.utility.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static nz.ac.canterbury.seng302.homehelper.validation.renovation.RenovationBudgetValidation.validateBudgetField;

@Controller
public class BudgetController {
    private final BudgetService budgetService;
    private final UserService userService;
    private final RenovationService renovationService;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ActivityService activityService;

    public BudgetController(BudgetService budgetService, RenovationService renovationService, UserService userService, RenovationService renovationService1, ActivityService activityService) {
        this.budgetService = budgetService;
        this.userService = userService;
        this.renovationService = renovationService1;
        this.activityService = activityService;
    }

    /**
     * Get the edit budget form
     *
     * @param id      the ID of the renovation
     * @param model   the model to pass information to the html page
     * @param request the request from the client to get the logged-in user
     * @return the edit budget page
     */
    @GetMapping("renovation/{id}/editBudget")
    public String editBudget(@PathVariable(name = "id") long id, HttpServletRequest request, Model model) throws NoResourceFoundException {
        User user = UserUtil.getUserFromHttpServletRequest(userService, request);
        Renovation renovation = renovationService.getRenovation(id)
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Renovation not found"));
        if (user != renovation.getOwner()) {
            return "redirect:/renovation/" + id + "/budget";
        }
        model.addAttribute("id", id);
        Budget budget = budgetService.findByRenovationId(id).stream().findFirst()
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Renovation not found"));
        model.addAttribute("budget", budget);
        return "pages/renovation/editBudgetPage";
    }

    /**
     * Post for a budget, checks a budgetDTO is valid then updates a renovations budget
     *
     * @param id        renovations id
     * @param budgetDto data transfer object of budget information
     * @param model     the model to pass information to the html page
     * @param request   the request from the client to get the logged-in user
     * @return the editBudgetPage on error, or redirect to myRenovations/{id} on success
     */
    @PostMapping("/renovation/{id}/editBudget")
    public String postEditBudget(@PathVariable(name = "id") long id,
                                 @ModelAttribute BudgetDto budgetDto, HttpServletRequest request,
                                 Model model) {
        logger.info("POST /renovation/{id}/editBudget/");

        try {
            User user = UserUtil.getUserFromHttpServletRequest(userService, request);
            Renovation renovation = renovationService.getRenovation(id)
                    .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Renovation not found"));
            if (user != renovation.getOwner()) {
                throw new UnauthenticatedException();
            }
            Budget budget = validateBudgetField(budgetDto);
            long budgetId = renovation.getBudget().getId();
            budgetService.updateBudget(budgetId, budget);

            // Save and send LiveUpdate:
            LiveUpdate update = new LiveUpdate(user, renovation, ActivityType.BUDGET_EDITED);
            try {
                activityService.saveLiveUpdate(update);
                activityService.sendUpdate(update);
            } catch (Exception e) {
                logger.error("Failed to send live update", e);
            }
        } catch (BudgetException e) {
            model.addAttribute("miscellaneousError", e.getFieldErrors().get("Miscellaneous"));
            model.addAttribute("materialError", e.getFieldErrors().get("Material"));
            model.addAttribute("labourError", e.getFieldErrors().get("Labour"));
            model.addAttribute("equipmentError", e.getFieldErrors().get("Equipment"));
            model.addAttribute("professionalServiceError", e.getFieldErrors().get("Professional Service"));
            model.addAttribute("permitError", e.getFieldErrors().get("Permit"));
            model.addAttribute("cleanupError", e.getFieldErrors().get("Cleanup"));
            model.addAttribute("deliveryError", e.getFieldErrors().get("Delivery"));
            model.addAttribute("budget", budgetDto);
            model.addAttribute("id", id);
            return "pages/renovation/editBudgetPage";
        } catch (NoResourceFoundException e) {
            throw new RuntimeException(e);
        }

        return "redirect:/renovation/" + id + "/budget";
    }

}
