Feature: U27B - As Lei, I want to be able to track material costs for tasks to make sure the renovation is sticking within the expected budget

  Scenario Outline: AC2 - Given I am on the task details page, when I click the button to view all expenditures for this task,
                    then a lists appears with all expenditures for this task, with their name, category, price, date, and a running total for this task
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>", password "<password>"
    And A user logs in with the email "<email>" and password "<password>"
    And They own a renovation titled "<renovation_name>"
    And There is a task titled "<task_name>"
    And The task has an expense named "<expense_name>" with a price of "<price>" on "<date>" with category "<category>"
    When The user clicks the Delete button on "<expense_name>"
    Then The expense "<expense_name>" is deleted
      Examples:
      | first_name | last_name | email               | password   | renovation_name    | task_name     | expense_name | price  | date       | category  |
      | Alice      | Smith     | alice@example.com   | Password1! | Kitchen Reno       | Paint Walls   | Wall Paint   | 50.00  | 2025-08-01 | Material  |
      | Susan      | Susanson  | susan@susan.susan   | Abc123!!   | Wine Cellar Reno   | Buy Wine      | Wine         | 69.00  | 2025-01-01 | Equipment |
      | Steve      | Minecraft | steve@microsoft.com | Dirt@3     | Dirt Hut           | Collect Dirt  | Shovel       | 49.95  | 2009-05-17 | Material  |
      | Alex       | Minecraft | alex@microsoft.com  | Stone@4    | Cow Farm           | Wheat Farm    | Wheat        | 420.00 | 2010-07-07 | Labour    |