Feature: U29A - As Sarah, when I’m logged in I want to see a home page with key and recent information about aspects of renovations I am a part of so that I can quickly access information about all my renovations.

  Scenario Outline: AC1 - Given I am logged in, when I open the application, then the first page to open is the logged in home page
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>", password "<password>"
    And A user logs in with the email "<email>" and password "<password>"
    When I view the home page
    Then I see the logged in home page
    Examples:
      | first_name | last_name | email               | password   |
      | Alice      | Smith     | alice@example.com   | Password1! |