package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BrickAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class AiChannelFeature {

    private final TestContext testContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BrickAiService brickAiService;

    public AiChannelFeature(TestContext testContext) {
        this.testContext = testContext;
    }

    @Then("I can see a chat channel named brickAI")
    public void iCanSeeAChatChannelNamedBrickAI() throws UnsupportedEncodingException {
        String content = testContext.getResult().getResponse().getContentAsString();
        Pattern pattern = Pattern.compile(".*brickAI.*", Pattern.CASE_INSENSITIVE);
        assertTrue(pattern.matcher(content).find());
    }

    @Then("A chat named brickAI is created where {string} and brickAI are the only members")
    public void aChatNamedBrickAIIsCreatedWhereAndBrickAIAreTheOnlyMembers(String email) {
        ChatChannel channel = brickAiService.getAiChannel(testContext.getRenovation(), testContext.getUser()).orElseThrow(() -> new RuntimeException("No channel found"));
        assertTrue(channel.getMembers().contains(testContext.getUser()));
        assertTrue(channel.getMembers().contains(brickAiService.getAiUser()));
        assertEquals(2, channel.getMembers().size());
    }

    @And("An account for BrickAI exists")
    public void anAccountForBrickAIExists() {
        brickAiService.createAiUser();
    }

    @When("I click on the brickAI chat channel")
    public void iClickOnTheBrickAIChatChannel() throws Exception {
        Renovation renovation = testContext.getRenovation();
        User user = testContext.getUser();
        testContext.setResult(mockMvc.perform(get("/renovation/" + renovation.getId() + "/chat/" + brickAiService.getAiChannel(renovation, user).get().getId())
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn());
    }

    @Then("The channel member list shows only myself and BrickAI")
    public void theChannelMemberListShowsOnlyMyselfAndBrickAI() throws UnsupportedEncodingException {
        String content = testContext.getResult().getResponse().getContentAsString();
        Pattern pattern = Pattern.compile(".*<div class=\"team-member-name\">BrickAI </div>.*", Pattern.CASE_INSENSITIVE);
        Pattern pattern2 = Pattern.compile(".*<div class=\"team-member-name\">Test user</div>.*", Pattern.CASE_INSENSITIVE);
        Pattern pattern3 = Pattern.compile(".*class=\"fs-6\">2.*", Pattern.CASE_INSENSITIVE);
        assertTrue(pattern.matcher(content).find());
        assertTrue(pattern2.matcher(content).find());
        assertTrue(pattern3.matcher(content).find());
    }
}
