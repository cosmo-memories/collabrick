Feature: U4 - As Sarah, I want to edit my user profile so that I can keep my details accurate.



  Scenario: AC2 - Valid profile update
    Given a user exists with first name "Jerry", last name "John", email "jerry.john@gmail.com", password "Abc123!!", retype password "Abc123!!"
    And I am signed in with email "jerry.john@gmail.com", password "Abc123!!"
    And I have navigated to the "/userPage/edit" page
    When I enter "James" as the "fname" on the update profile form
    And I enter "Li" as the "lname" on the update profile form
    And I enter "jerry.john@hotmail.com" as the "email" on the update profile form
    And I click the submit the update profile form
    Then I am taken to the user page
    And I see my details are updated with first name "James", last name "Li", and email "jerry.john@hotmail.com"

