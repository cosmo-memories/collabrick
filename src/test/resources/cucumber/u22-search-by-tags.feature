Feature: U22 - As Sarah, I want to be able to search for public renovation records by tags so that I can find renovations that are matching my interest.


  Scenario Outline: AC4.1 - A user that is not logged in can search using tags in the search bar
    Given a user registers with first name "<first_name>", last name "<last_name>", email "<user_email>", password "<password>", retype password "<password>"
    And a user with email "<user_email>" creates a renovation with title "<title1>", description "<description1>", timestamp "<timestamp1>"
    And a user with email "<user_email>" makes the renovation with title "<title1>" public
    And a user with email "<user_email>" adds a tag named "<tag_name1>" to renovation with title "<title1>"
    And a user with email "<user_email>" creates a renovation with title "<title2>", description "<description2>", timestamp "<timestamp2>"
    And a user with email "<user_email>" makes the renovation with title "<title2>" public
    And a user with email "<user_email>" adds a tag named "<tag_name1>" to renovation with title "<title2>"
    And an anonymous user adds tag "<tag_name1>" to the search bar
    When an anonymous user presses the search icon
    Then they should see a public renovation created by "<user_email>", titled "<title1>" with tag "<tag_name1>"
    And they should see a public renovation created by "<user_email>", titled "<title2>" with tag "<tag_name1>"
    And the renovations are displayed in descending order by timestamp

    Examples:
      | first_name | last_name | user_email           | password  | title1              | description1           | timestamp1          | title2               | description2          | timestamp2          | tag_name1     |
      | Alice      | Johnson   | alice@example.com    | Test@1234 | Kitchen Remodel     | Updated kitchen layout | 2025-05-10 10:00    | Bathroom Upgrade     | New tiles installed   | 2025-05-12 14:30    | Modern        |
      | Bob        | Smith     | bobsmith@example.com | Pass123!  | Basement Finishing  | Added home theater     | 2025-04-01 09:15    | Garage Overhaul      | Organized space       | 2025-04-02 11:45    | DIY           |
      | Clara      | Lee       | clara.lee@test.com   | SecurePwd | Garden Landscaping  | Eco-friendly design    | 2025-03-20 08:00    | Patio Extension      | Installed pergola     | 2025-03-22 18:00    | OutdoorLiving |
      | Susan      | Susanson  | susan@susan.com      | Susan1!   | Baby Room Decoration | New baby on the way   | 2024-09-06 16:20    | Wine Cellar Addition | The good drink        | 2024-05-20 15:09    | SusanStuff    |

  Scenario Outline: AC4.2 - A user that is not logged in can search using tags  and search string in the search bar
    Given a user registers with first name "<first_name>", last name "<last_name>", email "<user_email>", password "<password>", retype password "<password>"
    And a user with email "<user_email>" creates a renovation with title "<title1>", description "<description1>", timestamp "<timestamp1>"
    And a user with email "<user_email>" makes the renovation with title "<title1>" public
    And a user with email "<user_email>" adds a tag named "<tag_name1>" to renovation with title "<title1>"
    And a user with email "<user_email>" creates a renovation with title "<title2>", description "<description2>", timestamp "<timestamp2>"
    And a user with email "<user_email>" makes the renovation with title "<title2>" public
    And a user with email "<user_email>" adds a tag named "<tag_name1>" to renovation with title "<title2>"
    And an anonymous user adds tag "<tag_name1>" to the search bar
    And an anonymous user adds text "<search_string>" to the search bar
    When an anonymous user presses the search icon
    Then they should see a public renovation created by "<user_email>", titled "<title1>" with tag "<tag_name1>"
    And the renovations are displayed in descending order by timestamp

    Examples:
      | first_name | last_name | user_email           | password  | title1               | description1           | timestamp1          | title2               | description2          | timestamp2          | tag_name1     | search_string |
      | Alice      | Johnson   | alice@example.com    | Test@1234 | Kitchen Remodel      | Updated kitchen layout | 2025-05-10 10:00    | Bathroom Upgrade     | New tiles installed   | 2025-05-12 14:30    | Modern        | Kitchen       |
      | Bob        | Smith     | bobsmith@example.com | Pass123!  | Basement Finishing   | Added home theater     | 2025-04-01 09:15    | Garage Overhaul      | Organized space       | 2025-04-02 11:45    | DIY           | home          |
      | Clara      | Lee       | clara.lee@test.com   | SecurePwd | Garden Landscaping   | Eco-friendly design    | 2025-03-20 08:00    | Patio Extension      | Installed pergola     | 2025-03-22 18:00    | OutdoorLiving | eco           |
      | Susan      | Susanson  | susan@susan.com      | Susan1!   | Baby Room Decoration | New baby on the way    | 2024-09-06 16:20    | Wine Cellar Addition | The good drink        | 2024-05-20 15:09    | SusanStuff    | Baby          |

    Scenario Outline: AC4.3 - A user that is logged in can search using tags
      Given a user registers with first name "<first_name1>", last name "<last_name1>", email "<user_email1>", password "<password1>", retype password "<password1>"
      And a user with email "<user_email1>" creates a renovation with title "<title1>", description "<description1>", timestamp "<timestamp1>"
      And a user with email "<user_email1>" makes the renovation with title "<title1>" public
      And a user with email "<user_email1>" adds a tag named "<tag_name1>" to renovation with title "<title1>"
      And a user with email "<user_email1>" creates a renovation with title "<title2>", description "<description2>", timestamp "<timestamp2>"
      And a user with email "<user_email1>" makes the renovation with title "<title2>" public
      And a user with email "<user_email1>" adds a tag named "<tag_name1>" to renovation with title "<title2>"
      And a user registers with first name "<first_name2>", last name "<last_name2>", email "<user_email2>", password "<password2>", retype password "<password2>"
      When a logged in user with email "<user_email2>" searches using the tag named "<tag_name1>"
      Then they should see a public renovation created by "<user_email1>", titled "<title1>" with tag "<tag_name1>"
      And they should see a public renovation created by "<user_email1>", titled "<title2>" with tag "<tag_name1>"
      And the renovations are displayed in descending order by timestamp

      Examples:
        | first_name1 | last_name1 | user_email1        | password1 | title1           | description1                   | timestamp1          | title2           | description2                      | timestamp2          | tag_name1 | first_name2 | last_name2 | user_email2          | password2 |
        | Alice       | Smith      | alice@example.com  | Pass123!  | Kitchen Remodel  | Upgrading cabinets and lights  | 2024-05-01 10:00    | Bathroom Redo    | Installing new tiles and sink     | 2024-05-10 14:00    | modern    | Bob         | Johnson    | bob@example.com      | Pass123!  |
        | Mark        | Brown      | mark@example.com   | Secret!   | Garage Upgrade   | Adding storage shelves         | 2024-04-10 08:00    | Garden Makeover  | New flower beds and path lights   | 2024-04-15 16:30    | outdoor   | Carol       | White      | carol@example.com    | Secret!   |

  Scenario Outline: AC5 - A user that is not logged in searches using tags, but no renovations match
    Given a user registers with first name "<first_name>", last name "<last_name>", email "<user_email>", password "<password>", retype password "<password>"
    And a user with email "<user_email>" creates a renovation with title "<title1>", description "<description1>", timestamp "<timestamp1>"
    And a user with email "<user_email>" adds a tag named "<tag_name1>" to renovation with title "<title1>"
    And an anonymous user adds tag "<tag_name1_search>" to the search bar
    And an anonymous user adds text "<search_string>" to the search bar
    When an anonymous user presses the search icon
    Then they should see an error that says "No renovations match your search"

    Examples:
      | first_name | last_name | user_email        | password | title1            | description1                      | timestamp1          | tag_name1 | tag_name1_search | search_string |
      | Alice      | Smith     | alice@example.com | Pass123! | Kitchen Upgrade   | Modernizing the kitchen area      | 2024-03-01 10:00 | kitchen   | bathroom         |               |
      | John       | Doe       | john@example.com  | Pass123! | Basement Remodel  | Creating a cozy entertainment room| 2024-04-15 14:30 | basement  | attic            |               |
      | Sarah      | Lee       | sarah@example.com | Pass123! | Bathroom Redesign | Installing new fixtures           | 2024-05-10 09:00 | bathroom  | kitchen          |               |
      | Mike       | Brown     | mike@example.com  | Pass123! | Garden Makeover   | New landscaping and lighting      | 2024-02-20 12:00 | garden    | garage           |               |
      | Emma       | Davis     | emma@example.com  | Pass123! | Living Room Reno  | Repainting and new furniture      | 2024-01-05 08:45 | living    | living           | patio         |