package nz.ac.canterbury.seng302.homehelper.end2end;


import com.microsoft.playwright.Page;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static nz.ac.canterbury.seng302.homehelper.end2end.PlaywrightCucumberTest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateProfileFeature {

    @Given("I am signed in with email {string}, password {string}")
    public void SignInAsAUser(String email, String password) {
        Page tempPage = browserContext.newPage();
        tempPage.navigate(baseUrl + "/login");

        tempPage.fill("input[name=email]", email);
        tempPage.fill("input[name=password]", password);

        tempPage.click("button[type=submit]");
        tempPage.waitForURL("**/");

    }

    @When("I enter {string} as the {string} on the update profile form")
    public void iEnterAsTheUpdateProfileForm(String input, String form) {
        PlaywrightCucumberTest.page.locator("#" + form).fill(input);
    }

    @When("I click the submit the update profile form")
    public void iClickTheSubmitTheUpdateProfileForm() {
        PlaywrightCucumberTest.page.locator("[type=submit]").nth(1).click();
    }

    @Then("I am taken to the user page")
    public void iAmTakenToTheUserPage() {
        assertEquals(page.url(), baseUrl + "/userPage");
    }

    @Then("I see my details are updated with first name {string}, last name {string}, and email {string}")
    public void iSeeMyDetailsAreUpdated(String firstName, String lastName, String email) {
        String name = PlaywrightCucumberTest.page.locator("." + "user-location").nth(0).innerText();
        String userEmail = PlaywrightCucumberTest.page.locator("." + "user-location").nth(1).innerText();
        assertEquals(name, firstName + " " + lastName);
        assertEquals(userEmail, email);

    }

}
