Feature: U2 - As Sarah, I want to log into the system so that I can have a personalised experience with it and enjoy its features

  Scenario: AC2 - Valid login input
    Given a user exists with first name "Jerry", last name "John", email "jerry.john@gmail.com", password "Abc123!!", retype password "Abc123!!"
    And I have navigated to the "/login" page
    When I enter "jerry.john@gmail.com" as the "email" on the login form
    And I enter "Abc123!!" as the "password" on the login form
    And I click the Sign In button on the login form
    Then I am logged in as "Jerry John"
    And I am taken to the home page