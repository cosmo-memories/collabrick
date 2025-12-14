Feature: U1 - As Sarah, I want to register on Home Helper so that I can use its awesome features.

  Scenario: AC1 - Home page contains a Sign In button
    # make this specific to register step definitions so we don't have to worry about shared contexts
    # (sharing states between step definition classes)
    Given I want to register and I am on the system's main URL
    Then I see a button labelled Register to view the register form

  Scenario: AC3.1 - Blank first name input
    Given I am on the registration form
    When I enter " " as the "first name" on the registration form
    And I click the Sign Up button on the registration form
    Then I should see an error message for the field "first name" "First name cannot be empty" on the registration form
    And no account should be created

  Scenario Outline: AC3.2 - Invalid first name input
    Given I am on the registration form
    When I enter "<value>" as the "first name" on the registration form
    And I click the Sign Up button on the registration form
    Then I should see an error message for the field "first name" "First name must only include letters, spaces, hyphens, or apostrophes, and must contain at least one letter" on the registration form
    And no account should be created

    Examples:
      | value  |
      | J&ane  |
      | J@ane! |
      | Doe#   |

  Scenario Outline: AC4.1 - Too long first name input
    Given I am on the registration form
    When I enter "<value>" as the "first name" on the registration form
    And I click the Sign Up button on the registration form
    Then I should see an error message for the field "first name" "First name must be 64 characters long or less" on the registration form
    And no account should be created

    Examples:
      | value                                                                                |
      | Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa                    |
      | JaneDoeJaneDoeJaneDoeJaneDoeJaneDoeJaneDoeJaneDoeJaneDoeJaneDoeJaneDoeJaneDoeJaneDoe |

  Scenario Outline: AC4.1 - Too long last name input
    Given I am on the registration form
    When I enter "<value>" as the "last name" on the registration form
    And I click the Sign Up button on the registration form
    Then I should see an error message for the field "last name" "Last name must be 64 characters long or less" on the registration form
    And no account should be created

    Examples:
      | value                                                                                |
      | Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa                    |
      | JaneDoeJaneDoeJaneDoeJaneDoeJaneDoeJaneDoeJaneDoeJaneDoeJaneDoeJaneDoeJaneDoeJaneDoe |

  Scenario Outline: AC5 - Invalid email input
    Given I am on the registration form
    When I enter "<value>" as the "email" on the registration form
    And I click the Sign Up button on the registration form
    Then I should see an error message for the field "email" "Email address must be in the form 'jane@doe.nz'" on the registration form
    And no account should be created

    Examples:
      | value                   |
      | jane.doe@doe@nz         |
      | jane.doe@doecom         |
      | @doe.nz                 |
      | jane.doe@doe@domain.com |
      | jane.doe@@domain.com    |
      | jane.doe@domaincom      |
      | jane@doe@domain.com     |
      | jane.doe@.com           |
      | jane..doe@doe.com       |
      | jane@doe@doe.com        |
      | @domain.com             |
      | jane.doe@doe.com.       |
      | jane.doe@.domain.com    |
      | jane@domain@domain.com  |
      | jane@domain-.com        |

  Scenario: AC6 - Using an email that is already registered
    Given a user exists with first name "John", last name "Smith", email "john.smith@gmail.com", password "Abc123!", retype password "Abc123!"
    And I am on the registration form
    When I enter "john.smith@gmail.com" as the "email" on the registration form
    And I click the Sign Up button on the registration form
    Then I should see an error message for the field "email" "This email address is already in use" on the registration form
    And no account should be created

  Scenario Outline: AC7 - Mismatch password and retype password input
    Given I am on the registration form
    When I enter "<password>" as the "password" on the registration form
    And I enter "<retype password>" as the "retype password" on the registration form
    And I click the Sign Up button on the registration form
    Then I should see an error message for the field "password" "Passwords do not match" on the registration form
    And no account should be created

    Examples:
      | password   | retype password |
      | Abc123!!   | 123Abc!!        |
      | Password1! | Password2!      |
      | Pa$$w0rd   | Pa$$word        |
      | abcDEF123! | abcdef123!      |

  Scenario Outline: AC8 - Weak password input
    Given I am on the registration form
    When I enter "<password>" as the "password" on the registration form
    And I enter "<password>" as the "retype password" on the registration form
    And I click the Sign Up button on the registration form
    Then I should see an error message for the field "password" "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character." on the registration form
    And no account should be created

    Examples:
      | password         |
      | Short1!          |
      | lowercase123!    |
      | UPPERCASE123!    |
      | NoNumberPass!    |
      | NoSpecialChar123 |
