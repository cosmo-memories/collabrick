Feature: U10 - As Kaia, I want to see information about my renovation record so that I can refer to the details later.

  Scenario: AC1: Opening a renovation
    Given A user with the email 'user@test.com' is logged in
    And There exists a renovation with name 'Renovation Name' and description 'Renovation Description'
    When I click on the renovation
    Then I can see the renovation record information with name 'Renovation Name' and description 'Renovation Description'

  Scenario: AC2: Opening "My renovations"
    Given A user with the email 'user@test.com' is logged in
    And There exists a renovation with name 'Renovation Name' and description 'Renovation Description'
    When I click on "My Renovations"
    Then I can see a list of all the renovation records I have created

  Scenario: AC4: Cannot view other's renovation records
    Given A user with the email 'user@test.com' is logged in
    And a user exists with first name "Jerry", last name "John", email "jerry.john@gmail.com", password "Abc123!!", retype password "Abc123!!"
    And user "jerry.john@gmail.com" has a renovation with name 'Renovation Name' and description 'Renovation Description'
    When I try to access user "jerry.john@gmail.com" renovation
    Then I am redirected to a "Not Found" page

  Scenario: AC5: View private renovation when not logged in
    Given a user exists with first name "Jerry", last name "John", email "jerry.john@gmail.com", password "Abc123!!", retype password "Abc123!!"
    Given a user exists with first name "Jerry", last name "John", email "jerry.john@gmail.com", password "Abc123!!", retype password "Abc123!!"
    And user "jerry.john@gmail.com" has a renovation with name 'Renovation Name' and description 'Renovation Description'
    When I try to access user "jerry.john@gmail.com" renovation unauthenticated
    Then I am redirected to the Login page

