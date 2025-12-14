Feature: U2 - As Sarah, I want to log into the system so that I can have a personalised experience with it and enjoy its features

  Scenario: AC1 - Home page contains a Sign In button
    # make this specific to login step definitions so we don't have to worry about shared contexts
    # (sharing states between step definition classes)
    Given I want to login and I am on the system's main URL
    Then I see a button labelled Sign In to view the login form


  Scenario: AC2 - Valid login input
    Given a user exists with first name "Jerry", last name "John", email "jerry.john@gmail.com", password "Abc123!!", retype password "Abc123!!"
    When I enter "jerry.john@gmail.com" as the "email" on the login form
    And I enter "Abc123!!" as the "password" on the login form
    And I click the Sign In button on the login form
    And I am logged in

  Scenario: AC4 - Invalid email input
    Given I am on the login form
    When I enter "email@.com" as the "email" on the login form
    And I click the Sign In button on the login form
    Then I should see an error message for the field "email" "Email address must be in the form 'jane@doe.nz'" on the login form
    And I am not logged in

  Scenario: AC5 - Unknown email input
    Given a user does not exist with email "john.smith2@gmail.com"
    And I am on the login form
    When I enter "john.smith2@gmail.com" as the "email" on the login form
    And I click the Sign In button on the login form
    Then I should see a credentials error message "The email address is unknown, or the password is invalid" on the login form
    And I am not logged in

  Scenario: AC6 - Known email, invalid password input
    Given a user exists with first name "John", last name "Smith", email "john.smith1@gmail.com", password "Abc123!", retype password "Abc123!"
    When I enter "john.smith1@gmail.com" as the "email" on the login form
    And I click the Sign In button on the login form
    Then I should see a credentials error message "The email address is unknown, or the password is invalid" on the login form
    And I am not logged in