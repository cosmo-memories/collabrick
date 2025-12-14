Feature: U27A - As Lei, I want to track material costs for tasks to make sure the renovation is sticking within the expected budget.

  Scenario: AC8 - Finalise expenses and update task total
    Given A user with the email 'user1@test.com' is logged in
    And They own a renovation titled 'Kitchen Upgrade'
    And There is a task titled 'Task 1'
    And They have added an expense named 'Screws' with a price of 12.50 on '2025-01-01' with category 'Miscellaneous'
    When The user clicks the Add to Task button
    Then They are redirected to the task details page for 'Task 1'
    And The total expenditure shows 12.50

  Scenario: AC8 - Finalise multiple expenses and update task total
    Given A user with the email 'user1@test.com' is logged in
    And They own a renovation titled 'Kitchen Upgrade'
    And There is a task titled 'Task 1'
    And They have added an expense named 'Screws' with a price of 12.50 on '2025-01-02' with category 'Equipment'
    And They have added an expense named 'Cabinet' with a price of 37.00 on '2025-01-03' with category 'Permit'
    When The user clicks the Add to Task button
    Then They are redirected to the task details page for 'Task 1'
    And The total expenditure shows 49.50