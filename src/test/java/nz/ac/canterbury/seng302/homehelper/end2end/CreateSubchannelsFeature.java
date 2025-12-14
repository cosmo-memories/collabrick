package nz.ac.canterbury.seng302.homehelper.end2end;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatChannelRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static nz.ac.canterbury.seng302.homehelper.end2end.PlaywrightCucumberTest.baseUrl;
import static nz.ac.canterbury.seng302.homehelper.end2end.PlaywrightCucumberTest.page;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class CreateSubchannelsFeature {

    private static final String INPUT = "#new-channel-input";
    private static final String SUBMIT = "#create-channel";
    private final TestContext testContext;
    private final UserRepository userRepository;
    private final RenovationRepository renovationRepository;
    private final ChatChannelRepository chatChannelRepository;
    private final PasswordEncoder passwordEncoder;

    // used to compare channel counts (for "no new channel is created")
    private Long channelCountBefore = null;

    @Autowired
    public CreateSubchannelsFeature(
            TestContext testContext,
            UserRepository userRepository,
            RenovationRepository renovationRepository,
            ChatChannelRepository chatChannelRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.testContext = testContext;
        this.userRepository = userRepository;
        this.renovationRepository = renovationRepository;
        this.chatChannelRepository = chatChannelRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private void ensureOnRenovationPage() {
        Renovation r = testContext.getRenovation();
        assertNotNull(r, "Renovation not set in context");
        if (!page.url().contains("/renovation/" + r.getId())) {
            page.navigate(baseUrl + "/renovation/" + r.getId());
        }
        page.waitForSelector("nav.sidebar");
    }


    private static final String MODAL = "#newChannelModal";
    private static final String MODAL_VISIBLE = "#newChannelModal.show";

    private void ensureModalOpen() {
        ensureOnRenovationPage();
        if (page.locator(MODAL_VISIBLE).count() > 0) return;

        page.evaluate("() => { const m=document.querySelector('#newChannelModal'); if (window.bootstrap&&m) new window.bootstrap.Modal(m).show(); }");

        page.waitForSelector(MODAL_VISIBLE);
    }


    private void waitModalClosed() {
        page.waitForSelector(MODAL_VISIBLE, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
    }

    @Given("A user registers with first name {string}, last name {string}, email {string} and password {string}")
    public void a_user_registers(String fname, String lname, String email, String password) {
        String enc = passwordEncoder.encode(password);
        User user = new User(fname, lname, email, enc, enc);
        user.setActivated(true);
        user = userRepository.save(user);
        testContext.setUser(user);
    }

    @Given("A user logs in with the email {string}, password {string}")
    public void a_user_logs_in(String email, String password) {
        page.navigate(baseUrl + "/login");
        page.fill("input[name=email]", email);
        page.fill("input[name=password]", password);
        page.click("button[type=submit]");
        page.waitForURL("**/");
    }

    @Given("They own a renovation called {string}")
    public void they_own_a_renovation_titled(String title) {
        User owner = testContext.getUser();
        assertNotNull(owner, "Owner must exist before creating a renovation");
        Renovation r = new Renovation(title, "Test description");
        r.setOwner(owner);
        r = renovationRepository.save(r);
        testContext.setRenovation(r);
    }

    @Given("They type {string} into the Channel name input")
    public void they_type_into_channel_name(String channelName) {
        ensureModalOpen();
        Locator input = page.locator(MODAL + " " + INPUT);
        input.click();
        input.fill("");
        input.fill(channelName);
        assertEquals(channelName, input.inputValue());
    }


    // Submit inside the modal
    @When("The user clicks the Create Channel button")
    public void the_user_clicks_create_channel() {
        ensureModalOpen();
        page.click(MODAL + " " + SUBMIT);
    }

    @When("The user clicks the Cancel button in the modal")
    public void the_user_clicks_cancel_in_modal() {
        ensureModalOpen();
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Cancel")).click();
        waitModalClosed();
    }


    @Then("The Add Channel modal is closed")
    public void the_named_modal_is_closed() {
        // only one modal under test; assert by visibility
        assertEquals(0, page.locator(MODAL_VISIBLE).count(), "Expected modal to be closed");
    }

    @Then("The Add Channel modal remains open")
    public void the_named_modal_remains_open() {
        assertTrue(page.locator(MODAL_VISIBLE).count() > 0, "Expected modal to remain open");
    }

    @Then("I am shown an error message {string}")
    public void i_see_error_message(String msg) {
        // your modal has #channel-error and #channel-error-message
        Locator err = page.locator("#channel-error");
        err.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertTrue(err.isVisible(), "Expected error to be visible");
        assertTrue(page.locator("#channel-error-message").innerText().contains(msg),
                "Error text did not match");
    }

    @Then("The Channel name input still shows {string}")
    public void channel_input_still_shows(String expected) {
        Locator input = page.locator(MODAL + " " + INPUT);
        assertEquals(expected, input.inputValue());
    }

    @Then("The Channel name input is empty")
    public void channel_input_is_empty() {
        Locator input = page.locator(MODAL + " " + INPUT);
        assertEquals("", input.inputValue());
    }

    @Then("No new channel is created")
    public void no_new_channel_created() {
        Renovation r = testContext.getRenovation();
        assertNotNull(r);
        long after = chatChannelRepository.findByRenovation(r).size();
        if (channelCountBefore == null) {
            channelCountBefore = after;
        }
        assertEquals(channelCountBefore.longValue(), after, "Channel count changed unexpectedly");
    }

    @Then("All modal inputs are cleared")
    public void all_modal_inputs_cleared() {
        if (page.locator(MODAL_VISIBLE).count() > 0) {
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Cancel")).click();
            waitModalClosed();
        }
        ensureModalOpen();
        assertEquals("", page.locator(MODAL + " " + INPUT).inputValue(), "Input not cleared");
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Cancel")).click();
        waitModalClosed();
    }
}