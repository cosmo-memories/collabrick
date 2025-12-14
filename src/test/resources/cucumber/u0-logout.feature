Feature: U0 - As Sarah, I want to be able to log out of Home Helper when I am done so other people cannot access my account from my device.

  Scenario Outline: AC1 - Navigation contains a Logout button
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>", password "<password>"
    When A user logs in with the email "<email>" and password "<password>"
    Then There is a button labeled Logout visible on the navigation bar
    Examples:
      | email           | first_name | last_name | password |
      | johnny@ac1.net  | Johnny     | Test      | Abc123!! |
      | sammy@ac1.net   | Sammy      | Test      | Def456!! |

  Scenario Outline: AC2 - Clicking Logout button logs the user out
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>", password "<password>"
    And A user logs in with the email "<email>" and password "<password>"
    When A user clicks the Logout button on the navigation bar
    Then The user is logged out of their account
    Examples:
      | email            | first_name | last_name | password |
      | johnny@ac2.net   | Johnny     | Test      | Abc123!! |
      | sammy@ac2.net    | Sammy      | Test      | Def456!! |

  Scenario Outline: AC3 - No Logout button is shown when user is not authenticated
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>", password "<password>"
    And A user logs in with the email "<email>" and password "<password>"
    When A user clicks the Logout button on the navigation bar
    And The user is logged out of their account
    Then There is no button labeled Logout visible on the navigation bar
    Examples:
      | email            | first_name | last_name | password |
      | johnny@ac3.net   | Johnny     | Test      | Abc123!! |
      | sammy@ac3.net    | Sammy      | Test      | Def456!! |

  Scenario Outline: AC4 - Logged out users cannot access My Profile page
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>", password "<password>"
    And A user logs in with the email "<email>" and password "<password>"
    And A user clicks the Logout button on the navigation bar
    And The user is logged out of their account
    When The anonymous user attempts to view the My Profile page
    Then They are redirected to the sign in page
    Examples:
      | email            | first_name | last_name | password |
      | johnny@ac4.net   | Johnny     | Test      | Abc123!! |
      | sammy@ac4.net    | Sammy      | Test      | Def456!! |

  Scenario Outline: AC5 - Logged out users cannot access My Renovations page
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>", password "<password>"
    And A user logs in with the email "<email>" and password "<password>"
    And A user clicks the Logout button on the navigation bar
    And The user is logged out of their account
    When The anonymous user attempts to view the My Renovations page
    Then They are redirected to the sign in page
    Examples:
      | email            | first_name | last_name | password |
      | johnny@ac5.net   | Johnny     | Test      | Abc123!! |
      | sammy@ac5.net    | Sammy      | Test      | Def456!! |