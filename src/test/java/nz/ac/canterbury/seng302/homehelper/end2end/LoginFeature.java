package nz.ac.canterbury.seng302.homehelper.end2end;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class LoginFeature {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Given("a user exists with first name {string}, last name {string}, email {string}, password {string}, retype password {string}")
    public void a_user_exists_with_first_name_last_name_email_password_retype_password(String firstName, String lastName, String email, String password, String retypePassword) {
        password = passwordEncoder.encode(password);
        retypePassword = passwordEncoder.encode(retypePassword);
        User user = new User(firstName, lastName, email, password, retypePassword);
        user.setActivated(true);
        userRepository.save(user);
    }

    @Given("I have navigated to the {string} page")
    public void i_have_navigated_to_the_page(String page) {
        PlaywrightCucumberTest.page.navigate(PlaywrightCucumberTest.baseUrl + page);
    }

    @When("I enter {string} as the {string} on the login form")
    public void i_enter_as_the_on_the_login_form(String inputtedString, String inputId) {
        PlaywrightCucumberTest.page.locator("#" + inputId).fill(inputtedString);
    }

    @When("I click the Sign In button on the login form")
    public void i_click_the_sign_in_button_on_the_login_form() {
        PlaywrightCucumberTest.page.locator("[type=submit]").click();
    }

    @Then("I am logged in as {string}")
    public void i_am_logged_in_as(String name) {
        String displayedName = PlaywrightCucumberTest.page.locator("#username").innerText();
        Assertions.assertEquals(name, displayedName);
    }

    @Then("I am taken to the home page")
    public void i_am_taken_to_the_home_page() {
        String redirectedUrl = PlaywrightCucumberTest.page.url();
        Assertions.assertTrue(redirectedUrl.endsWith("/"));
    }
}
