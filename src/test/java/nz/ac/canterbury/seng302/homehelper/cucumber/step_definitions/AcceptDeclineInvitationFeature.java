package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.InvitationStatus;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.InvitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Transactional
public class AcceptDeclineInvitationFeature {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvitationRepository invitationRepository;

    private final TestContext testContext;

    private UUID invitationId;

    public AcceptDeclineInvitationFeature(TestContext testContext) {
        this.testContext = testContext;
    }

    /**
     * Ensures that the database is fully reset between scenario outline examples
     */
    @Before
    public void resetDatabase() {
        testContext.renovationRepository.deleteAll();
        testContext.userRepository.deleteAll();
        invitationRepository.deleteAll();
    }

    @Given("No account exists with the email {string}")
    public void no_account_exists_with_the_email(String email) {
        assertTrue(testContext.userRepository.findByEmail(email).isEmpty());
    }

    @Given("A user with email {string} is invited to join renovation {string} owned by user with email {string}")
    public void a_user_with_is_invited_to_join_renovation_owned_by_user_with_email(String inviteeEmail, String renovationName, String renovationOwnerEmail) {
        User renovationOwner = new User("Renovation owner", "", renovationOwnerEmail, "Password123!", "Password123!");
        testContext.userRepository.save(renovationOwner);
        Renovation renovation = new Renovation(renovationName, "Description");
        renovation.setOwner(renovationOwner);
        testContext.renovationRepository.save(renovation);

        User invitedUser = testContext.userRepository.findByEmail(inviteeEmail).getFirst();
        Invitation invitation = new Invitation(invitedUser, renovation);
        invitationRepository.save(invitation);
        invitationId = invitation.getId();
    }

    @Given("{string} is sent an invitation to join renovation {string} owned by user with email {string}")
    public void is_sent_an_invitation_to_join_renovation_owned_by_user_with_email(String inviteeEmail, String renovationName, String renovationOwnerEmail) {
        User renovationOwner = new User("Renovation owner", "", renovationOwnerEmail, "Password123!", "Password123!");
        testContext.userRepository.save(renovationOwner);
        Renovation renovation = new Renovation(renovationName, "Description");
        renovation.setOwner(renovationOwner);
        testContext.renovationRepository.save(renovation);
        Invitation invitation = new Invitation(inviteeEmail, renovation);
        invitationRepository.save(invitation);
        invitationId = invitation.getId();
    }

    @Given("A user with email {string} exists but is not logged in")
    public void a_user_with_email_exists_but_is_not_logged_in(String inviteeEmail) {
        User invitedUser = new User("Invited", "", inviteeEmail, "Password123!", "Password123!");
        testContext.userRepository.save(invitedUser);
    }

    @When("A user with {string} clicks the Decline button on the email invitation")
    public void a_user_with_clicks_the_decline_button_on_the_email_invitation(String string) throws Exception {
        Invitation invitation = invitationRepository.findById(invitationId).get();

        testContext.setResult(mockMvc.perform(get("/decline-invitation")
                        .queryParam("token", invitation.getId().toString())
                        .with(user(String.valueOf(testContext.getUser().getId()))
                                .password(testContext.getUser().getPassword())
                                .roles("USER"))
                        .with(csrf()))
                .andReturn());
    }


    @When("An anonymous user clicks the Decline button on the email invitation sent to {string}")
    public void an_anonymous_user_clicks_the_decline_button_on_the_email_invitation_sent_to(String string) throws Exception {
        Invitation invitation = invitationRepository.findById(invitationId).get();

        testContext.setResult(mockMvc.perform(get("/decline-invitation")
                        .queryParam("token", invitation.getId().toString()))
                .andReturn());
    }


    @When("A user with email {string} that is not logged in clicks the Decline button on the email invitation")
    public void a_user_with_email_that_is_not_logged_in_clicks_the_decline_button_on_the_email_invitation(String inviteeEmail) throws Exception {
        Invitation invitation = invitationRepository.findById(invitationId).get();

        testContext.setResult(mockMvc.perform(get("/decline-invitation")
                        .queryParam("token", invitation.getId().toString()))
                .andReturn());
    }


    @Then("The invitation sent to {string} invitation is declined")
    public void the_invitation_sent_to_invitation_is_declined(String inviteeEmail) throws Exception {
        Invitation invitation = invitationRepository.findById(invitationId).get();
        assertSame(InvitationStatus.DECLINED, invitation.getInvitationStatus());
    }

    @Then("The user is redirected to the declined invitation page")
    public void the_user_is_redirected_to_the_declined_invitation_page() {
        assertThat(testContext.getResult().getModelAndView().getViewName())
                .isEqualTo("pages/declinedInvitationPage");

    }
}
