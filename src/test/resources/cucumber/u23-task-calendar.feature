Feature: U23 - As Kaia, I want to see a calendar displaying my upcoming tasks, so I know which ones need to be prioritised.

  Scenario: AC3 - Access new task form through task calendar
    Given A user with the email 'user1@test.com' is logged in
    And They own a renovation titled 'Test Renovation 1'
    When The user double clicks the date '2025-06-05' on the task calendar
    Then They are directed to a new task form
    And The date is prefilled with '2025-06-05'

  Scenario: AC4 - Access edit task form through task calendar
    Given A user with the email 'user1@test.com' is logged in
    And They own a renovation titled 'Test Renovation 1'
    And There is a task titled 'Task 1'
    When The user double clicks on a task in the calendar
    Then They are directed to the edit task form for that task
    And The date is prefilled with '2026-01-01'

  Scenario: AC5 - Cancel button on edit task form returns to calendar with date highlighted
    Given A user with the email 'user1@test.com' is logged in
    And They own a renovation titled 'Test Renovation 1'
    And There is a task titled 'Task 1'
    And They are directed to the edit task form for that task
    When The user clicks the cancel button on the edit task form
    Then They land on the calendar tab
    And The calendar highlights the date '2026-01-01'

  Scenario: AC6 - Submit on edit task form returns to calendar with date highlighted
    Given A user with the email 'user1@test.com' is logged in
    And They own a renovation titled 'Test Renovation 1'
    And There is a task titled 'Task 1'
    And They are directed to the edit task form for that task
    And They change the due date to '2027-01-01'
    When The user clicks the submit button on the edit task form
    Then They are redirected to the renovation's calendar tab
    And The calendar highlights the date '2027-01-01'
