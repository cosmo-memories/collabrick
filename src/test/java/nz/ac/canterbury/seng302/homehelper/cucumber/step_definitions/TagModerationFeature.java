package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.FlashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class TagModerationFeature {

    @Autowired
    private MockMvc mockMvc;
    private final User testUser;
    private final Renovation testRenovation;
    private MvcResult result;

    public TagModerationFeature(TestContext testContext) {
        testUser = testContext.getUser();
        testRenovation = testContext.getRenovation();
    }

    @When("The user adds a tag named {string}")
    public void the_user_adds_a_tag_named(String tagName) throws Throwable {
        result = mockMvc.perform(post("/myRenovations/{id}", testRenovation.getId())
                        .param("tagName", tagName)
                        .with(csrf())
                        .with(user(String.valueOf(testUser.getId())).password(testUser.getPassword()).roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    @Then("The user receives an error message telling them they're not following the system language standards")
    public void the_user_receives_an_error_message_telling_them_they_not_following_the_system_language_standards() throws Throwable {
        String expectedErrorMessage = "Tag does not follow the system language standards";
        FlashMap flashMap = result.getFlashMap();
        assertEquals(expectedErrorMessage, flashMap.get("tagError"));
    }
}
