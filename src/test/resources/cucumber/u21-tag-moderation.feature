Feature: U21 - As Inaya, I want to be able to know when I input an inappropriate tag so that the system remains free from inappropriate language.

  Scenario Outline: AC1 - Add tag with inappropriate word (no slight variations) error
    Given A user with the email 'user@test.com' is logged in
    And They own a renovation titled 'Test Renovation'
    When The user adds a tag named <tagName>
    Then The user receives an error message telling them they're not following the system language standards

    Examples:
    | tagName |
    | 'shit' |
    | 'fuck' |
    | 'slutty'   |
    | 'cunt'     |

  Scenario Outline: AC2 - Add tag with inappropriate word (slight variation) error
    Given A user with the email 'user@test.com' is logged in
    And They own a renovation titled 'Test Renovation'
    When The user adds a tag named <tagName>
    Then The user receives an error message telling them they're not following the system language standards

    Examples:
    | tagName |
    | 'sh*t' |
    | 'f*ck' |
    | 'sh!t' |
    | '$lut' |
    | '$kank' |
    | '$lutty' |
    | '5h!t' |