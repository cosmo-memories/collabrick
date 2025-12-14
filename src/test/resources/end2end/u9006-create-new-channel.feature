Feature: U28C - As Susan, I want to create discussion channels so I can keep conversations organized by room, trade, or project phase

  Scenario Outline: AC2 - Given I have opened the add channel modal, when I click the “Cancel” button, then the modal is closed, no channel is created, and all modal inputs are cleared
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>" and password "<password>"
    And A user logs in with the email "<email>", password "<password>"
    And They own a renovation called "<renovation_name>"
    And They type "Painting" into the Channel name input
    When The user clicks the Cancel button in the modal
    And No new channel is created

    Examples:
      | first_name | last_name | email             | password   | renovation_name |
      | Alice      | Smith     | alice@example.com | Password1! | Kitchen Reno    |

  Scenario Outline: AC3 - Given I have entered a valid channel name, when I click the "Create Channel" button, the modal closes and I can see the new channel
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>" and password "<password>"
    And A user logs in with the email "<email>", password "<password>"
    And They own a renovation called "<renovation_name>"
    And They type "<valid_name>" into the Channel name input
    When The user clicks the Create Channel button
    Then The Add Channel modal is closed

    Examples:
      | first_name | last_name | email               | password   | renovation_name | valid_name           |
      | Jane      | Smith     | jane@example.com   | Password1! | Reno 2  | General              |


  Scenario Outline: AC4 - Given I am on the add channel modal, when I click the “Create Channel” button and I have entered no channel name, then I am shown an error message
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>" and password "<password>"
    And A user logs in with the email "<email>", password "<password>"
    And They own a renovation called "<renovation_name>"
    When The user clicks the Create Channel button
    Then I am shown an error message "Channel name cannot be empty"
    And The Add Channel modal remains open
    And The Channel name input is empty

    Examples:
      | first_name | last_name | email             | password   | renovation_name |
      | Corey      | Smith     | corey@example.com | Password1! | Reno 3    |

  Scenario Outline: AC5 - Given I entered an invalid channel name, when I click "Create Channel", then I see the detailed validation error and my inputs are preserved
    Given A user registers with first name "<first_name>", last name "<last_name>", email "<email>" and password "<password>"
    And A user logs in with the email "<email>", password "<password>"
    And They own a renovation called "<renovation_name>"
    And They type "<invalid_name>" into the Channel name input
    When The user clicks the Create Channel button
    Then I am shown an error message "Channel name must only include letters, numbers, spaces, dots, hyphens, or apostrophes, and must contain at least one letter or number"
    And The Add Channel modal remains open
    And The Channel name input still shows "<invalid_name>"

    Examples:
      | first_name | last_name | email             | password   | renovation_name | invalid_name |
      | David      | Smith     | david@example.com | Password1! | Reno 4    | ***          |