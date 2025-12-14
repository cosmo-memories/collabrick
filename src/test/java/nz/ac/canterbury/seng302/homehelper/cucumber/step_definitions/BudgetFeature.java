package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Budget;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class BudgetFeature {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BudgetService budgetService;

    private final TestContext testContext;
    private final Renovation renovation;
    private MockHttpSession authenticationSession;


    public BudgetFeature(TestContext testContext) {
        this.testContext = testContext;
        renovation = testContext.getRenovation();
    }

    @And("The renovation has a budget of {string} in each category")
    public void theRenovationHasABudgetOfIn(String amount) {
        budgetService.updateBudget(renovation.getBudget().getId(), new Budget(
                new BigDecimal(amount),
                new BigDecimal(amount),
                new BigDecimal(amount),
                new BigDecimal(amount),
                new BigDecimal(amount),
                new BigDecimal(amount),
                new BigDecimal(amount),
                new BigDecimal(amount)
        ));
    }

    @When("The user views the budget page for the renovation")
    public void theUserViewsTheBudgetPageForTheRenovation() throws Exception {
        testContext.setResult(mockMvc.perform(MockMvcRequestBuilders.get("/renovation/" + renovation.getId() + "/budget")
                        .session(testContext.getSession())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn());
    }

    @Then("Each category is shown with a budget of {string}")
    public void eachCategoryIsShownWithABudgetOf(String amount) {
        Map<String, Object> model = Objects.requireNonNull(testContext.getResult().getModelAndView()).getModel();
        BigDecimal expected = new BigDecimal(amount);

        assertEquals(expected, model.get("miscBudget"));
        assertEquals(expected, model.get("materialBudget"));
        assertEquals(expected, model.get("labourBudget"));
        assertEquals(expected, model.get("equipmentBudget"));
        assertEquals(expected, model.get("serviceBudget"));
        assertEquals(expected, model.get("permitBudget"));
        assertEquals(expected, model.get("cleanupBudget"));
        assertEquals(expected, model.get("deliveryBudget"));
    }

    @Then("The budget page shows they have total expenses of {double}")
    public void theBudgetPageShowsTheyHaveTotalExpensesOf(double amount) {
        Map<String, Object> model = Objects.requireNonNull(testContext.getResult().getModelAndView()).getModel();
        BigDecimal expected = BigDecimal.valueOf(amount);
        BigDecimal actual = (BigDecimal) model.get("expenseSum");
        assertEquals(0, expected.compareTo(actual));
    }

    @And("The {string} category has total expenses of {double}")
    public void theEquipmentCategoryHasTotalExpensesOf(String category, double amount) {
        Map<String, Object> model = Objects.requireNonNull(testContext.getResult().getModelAndView()).getModel();
        BigDecimal expected = BigDecimal.valueOf(amount);
        BigDecimal actual = (BigDecimal) model.get(category.toLowerCase() + "Expenses");
        assertEquals(0, expected.compareTo(actual));
    }

    @And("The {string} category is shown in red")
    public void theEquipmentCategoryIsShownInRed(String category) throws UnsupportedEncodingException {
        String htmlContent = testContext.getResult().getResponse().getContentAsString();
        // Red title is shown using:
        // <h5 th:classappend="${currentValue > max} ? ' color-red fw-bold' : '' " th:text="${title}"></h5>
        // Regex from ChatGPT
        String regex = "<[^>]*class=[\"'][^\"']*red[^\"']*[\"'][^>]*>\\s*" + Pattern.quote(category) + "\\s*</[^>]+>";
        assertTrue(Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(htmlContent).find());
    }

    @Then("The budget graph is not shown")
    public void theBudgetGraphIsNotShown() throws UnsupportedEncodingException {
        String htmlContent = testContext.getResult().getResponse().getContentAsString();
        String message = "Set a budget for this renovation to view your budget breakdown";
        assertTrue(Pattern.compile(message, Pattern.CASE_INSENSITIVE).matcher(htmlContent).find());
    }

    @And("The expense total for the renovation is shown in red")
    public void theExpenseTotalForTheRenovationIsShownInRed() throws UnsupportedEncodingException {
        String htmlContent = testContext.getResult().getResponse().getContentAsString();
        Map<String, Object> model = Objects.requireNonNull(testContext.getResult().getModelAndView()).getModel();


        // Class 'spent-over' has the red styling
        // ChatGPT regex
        String regex = "<div[^>]*class=[\"'][^\"']*\\bbudget-box\\b[^\"']*\\bspent-over\\b[^\"']*[\"'][^>]*>\\s*" +
                "<span[^>]*>\\s*" + "\\$" + model.get("expenseSum").toString() + "\\s*</span>";

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        assertTrue(pattern.matcher(htmlContent).find());
    }
}
