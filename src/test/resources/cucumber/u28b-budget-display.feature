Feature: U28B - As Lei, I want to be able to see a visual representation of my current spending so that I can make sure I keep within my budget

  Scenario Outline: AC1 - Given I am on the budgeting tab, when I view the tab,
  then I can see each element of my budget listed out and the total sum of the budget is shown
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>", password "<password>"
    And A user logs in with the email "<email>" and password "<password>"
    And They own a renovation titled "<renovation_name>"
    And The renovation has a budget of "<amount>" in each category
    When The user views the budget page for the renovation
    Then Each category is shown with a budget of "<amount>"
    Examples:
      | first_name | last_name | email               | password   | renovation_name    | amount |
      | Alice      | Smith     | alice@example.com   | Password1! | Kitchen Reno       | 420.00 |
      | Susan      | Susanson  | susan@susan.susan   | Abc123!!   | Wine Cellar Reno   | 69.69  |
      | Steve      | Minecraft | steve@microsoft.com | Dirt@3     | Dirt Hut           | 100.00 |
      | Alex       | Minecraft | alex@microsoft.com  | Stone@4    | Cow Farm           | 0.00   |

  Scenario Outline: AC2 - Given I am on the budgeting tab, when I view the tab,
  then I can see my expenses total for each category and a total overall
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>", password "<password>"
    And A user logs in with the email "<email>" and password "<password>"
    And They own a renovation titled "<renovation_name>"
    And There is a task titled 'Task 1'
    And They have added an expense named 'Screws' with a price of 12.50 on '2025-01-02' with category 'Equipment'
    And They have added an expense named 'Cabinet' with a price of 37.00 on '2025-01-03' with category 'Permit'
    And The user clicks the Add to Task button
    When The user views the budget page for the renovation
    Then The budget page shows they have total expenses of 49.50
    And The 'Equipment' category has total expenses of 12.50
    And The 'Permit' category has total expenses of 37.00
    Examples:
      | first_name | last_name | email               | password   | renovation_name    |
      | Alice      | Smith     | alice@example.com   | Password1! | Kitchen Reno       |

  Scenario Outline: AC5 - Given a category that has gone over budget, when I view the budget tab,
  then the over-budget category is displayed in red on the expenses pie chart, and the category name is also shown in red in the written budget summary.
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>", password "<password>"
    And A user logs in with the email "<email>" and password "<password>"
    And They own a renovation titled "<renovation_name>"
    And The renovation has a budget of "<amount>" in each category
    And There is a task titled 'Task 1'
    And They have added an expense named 'Expensive Stuff' with a price of 1000.00 on '2025-01-02' with category 'Equipment'
    And The user clicks the Add to Task button
    When The user views the budget page for the renovation
    Then The 'Equipment' category has total expenses of 1000.00
    And The 'Equipment' category is shown in red
    Examples:
      | first_name | last_name | email               | password   | renovation_name    | amount |
      | Alice      | Smith     | alice@example.com   | Password1! | Kitchen Reno       | 100.00 |

  Scenario Outline: AC7 - Given there are no categories of budget that are above 0, when I view the budgeting tab,
  then the pie chart is not shown and in its place a message is shown that says the user has no budget yet.
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>", password "<password>"
    And A user logs in with the email "<email>" and password "<password>"
    And They own a renovation titled "<renovation_name>"
    And The renovation has a budget of "<amount>" in each category
    When The user views the budget page for the renovation
    Then The budget graph is not shown
    Examples:
      | first_name | last_name | email               | password   | renovation_name    | amount |
      | Alice      | Smith     | alice@example.com   | Password1! | Kitchen Reno       | 0.00   |

  Scenario Outline: AC8 - Given my total expenses have gone over the total budget,
  when I view the budgeting tab, then the total for the expenses appears in red.
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>", password "<password>"
    And A user logs in with the email "<email>" and password "<password>"
    And They own a renovation titled "<renovation_name>"
    And The renovation has a budget of "<amount>" in each category
    And There is a task titled 'Task 1'
    And They have added an expense named 'Expensive Stuff' with a price of 1000.00 on '2025-01-02' with category 'Equipment'
    And The user clicks the Add to Task button
    When The user views the budget page for the renovation
    Then The expense total for the renovation is shown in red
    Examples:
      | first_name | last_name | email               | password   | renovation_name    | amount |
      | Alice      | Smith     | alice@example.com   | Password1! | Kitchen Reno       | 100.00 |