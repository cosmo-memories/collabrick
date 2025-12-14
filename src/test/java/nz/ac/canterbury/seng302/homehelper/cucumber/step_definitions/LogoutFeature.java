package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Transactional
public class LogoutFeature {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockHttpSession session;

    private final TestContext testContext;

    public LogoutFeature(TestContext testContext) {
        this.testContext = testContext;
    }

    @Given("A user registers with first name {string}, last name {string}, email {string}, password {string}")
    public void a_user_registers_with_first_name_last_name_email_password(String fname, String lname, String email, String password) {
        String encryptedPassword = passwordEncoder.encode(password);
        User user = new User(fname, lname, email, encryptedPassword, encryptedPassword);
        user.setActivated(true);
        user = userRepository.save(user);
        testContext.setUser(user);
    }

    @When("A user logs in with the email {string} and password {string}")
    public void a_user_logs_in_with_the_email_and_password(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/do_login")
                        .param("email", email)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        session = (MockHttpSession) result.getRequest().getSession(false);
        testContext.setSession(session);

        assertNotNull(session);
        assertFalse(session.isInvalid());
        // ChatGPT helped figure out how to actually get the page content after redirect
        MvcResult redirected = mockMvc.perform(
                        get("/")
                                .session(session))
                .andExpect(status().isOk())
                .andReturn();

        testContext.setResult(redirected);
    }

    @Then("There is a button labeled Logout visible on the navigation bar")
    public void there_is_a_button_labeled_logout_visible_on_the_navigation_bar() throws UnsupportedEncodingException {
        String content = testContext.getResult().getResponse().getContentAsString();
        // Regex borrowed/modified from U2 Login cucumber tests
        Pattern pattern = Pattern.compile(
                "<a[^>]*href=[\"']/logout[\"'][^>]*>\\s*Logout\\s*</a>",
                Pattern.CASE_INSENSITIVE
        );
        assertTrue(pattern.matcher(content).find());
    }

    @When("A user clicks the Logout button on the navigation bar")
    public void a_user_clicks_the_logout_button_on_the_navigation_bar() throws Exception {
        MvcResult result = mockMvc.perform(post("/logout")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        testContext.setResult(result);
    }

    @Then("The user is logged out of their account")
    public void the_user_is_logged_out_of_their_account() throws Exception {
        MvcResult redirected = mockMvc.perform(
                        get("/")
                                .session(session))
                .andExpect(status().isOk())
                .andReturn();
        testContext.setResult(redirected);
        assertTrue(session.isInvalid());
    }

    @Then("There is no button labeled Logout visible on the navigation bar")
    public void there_is_no_button_labeled_logout_visible_on_the_navigation_bar() throws Exception {
        String content = testContext.getResult().getResponse().getContentAsString();
        assertNotEquals("", content);
        Pattern pattern = Pattern.compile(
                "<a[^>]*href=[\"']/logout[\"'][^>]*>\\s*Logout\\s*</a>",
                Pattern.CASE_INSENSITIVE
        );
        assertFalse(pattern.matcher(content).find());
    }

    @When("The anonymous user attempts to view the My Profile page")
    public void the_anonymous_user_attempts_to_view_the_my_profile_page() throws Exception {
        MvcResult result = mockMvc.perform(get("/myProfile"))
                .andReturn();
        testContext.setResult(result);
    }

    @When("The anonymous user attempts to view the My Renovations page")
    public void the_anonymous_user_attempts_to_view_the_my_renovations_page() throws Exception {
        MvcResult result = mockMvc.perform(get("/myRenovations"))
                .andReturn();
        testContext.setResult(result);
    }

    @Then("They are redirected to the sign in page")
    public void they_are_redirected_to_the_sign_in_page() {
        String redirected = testContext.getResult().getResponse().getRedirectedUrl();
        assert redirected != null;
        assertEquals("/login", URI.create(redirected).getPath());
    }
}
