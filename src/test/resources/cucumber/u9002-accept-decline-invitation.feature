Feature: U9002 - As Gareth, I want to be able to accept or reject a renovation invitation so that I have control over the projects I commit to and can manage my workload effectively.

  Scenario: AC1.1 - Decline invitation when no account exists for invitee
    Given No account exists with the email "newuser@test.com"
    And "newuser@test.com" is sent an invitation to join renovation "Kitchen Remodel" owned by user with email "owner@test.com"
    When An anonymous user clicks the Decline button on the email invitation sent to "newuser@test.com"
    Then The invitation sent to "newuser@test.com" invitation is declined
    And The user is redirected to the declined invitation page


  Scenario: AC1.2 - Decline invitation when invitee is logged in
    Given A user with the email "loggedin@test.com" is logged in
    And A user with email "loggedin@test.com" is invited to join renovation "Bathroom Upgrade" owned by user with email "owner@test.com"
    When A user with "loggedin@test.com" clicks the Decline button on the email invitation
    Then The invitation sent to "loggedin@test.com" invitation is declined
    And The user is redirected to the declined invitation page


  Scenario: AC1.3 - Decline invitation to user with no account when logged in as another user
    Given a user registers with first name "John", last name "Smith", email "invitee@test.com", password "Abc123!!", retype password "Abc123!!"
    And A user with the email "someoneelse@test.com" is logged in
    And A user with email "invitee@test.com" is invited to join renovation "Garage Extension" owned by user with email "owner@test.com"
    When A user with "invitee@test.com" clicks the Decline button on the email invitation
    Then The invitation sent to "invitee@test.com" invitation is declined
    And The user is redirected to the declined invitation page


  Scenario: AC1.4 - Decline invitation when invitee exists but is not logged in
    Given A user with email "offline@test.com" exists but is not logged in
    And A user with email "offline@test.com" is invited to join renovation "Living Room Update" owned by user with email "owner@test.com"
    When A user with email "offline@test.com" that is not logged in clicks the Decline button on the email invitation
    Then The invitation sent to "offline@test.com" invitation is declined
    And The user is redirected to the declined invitation page
