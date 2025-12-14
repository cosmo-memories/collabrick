Feature: U29B - As Susan, I want to understand what this website offers on the homepage so that I can make a decision about whether to engage further with the platform.

  Scenario: AC1 - Given I am not logged in, when I open the application, then the first page to open is the home page
    Given I am not logged and want to view the home page
    When I view the home page
    Then I see the logged out home page