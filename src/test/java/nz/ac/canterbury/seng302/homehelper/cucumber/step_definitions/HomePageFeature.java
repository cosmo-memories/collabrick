package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class HomePageFeature {

    private @Autowired MockMvc mockMvc;
    private @Autowired TestContext testContext;
    private MvcResult mvcResult;

    @Given("I am not logged and want to view the home page")
    public void i_am_not_logged_and_want_to_view_the_home_page() {

    }

    @When("I view the home page")
    public void i_view_the_home_page() throws Exception {
        User user = testContext.getUser();
        MockHttpServletRequestBuilder requestBuilder = get("/");
         if (user != null) {
             requestBuilder.with(user(String.valueOf(user.getId())));
         }

        mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I see the logged out home page")
    public void i_see_the_logged_out_home_page() {
        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertEquals("pages/homePage", modelAndView.getViewName());
    }

    @Then("I see the logged in home page")
    public void i_see_the_logged_in_home_page() {
        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertNotNull(modelAndView);
        assertEquals("pages/dashboard", modelAndView.getViewName());
    }
}
