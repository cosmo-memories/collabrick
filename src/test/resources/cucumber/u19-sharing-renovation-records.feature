Feature: U19 - As Inaya, I want to be able to make my renovation record public so that I can share my progress with other.

  Scenario: AC1 - A renovation can be set to public
    Given A user with the email 'user@test.com' is logged in
    And They own a renovation titled 'Test Renovation'
    And The renovation visibility is set to 'false'
    When The user sets the renovation visibility to 'true'
    Then The renovation is set to public
    And The user is redirected to the individual renovation page

  Scenario: AC2 - A renovation can be set to private
    Given A user with the email 'user@test.com' is logged in
    And They own a renovation titled 'Test Renovation'
    And The renovation visibility is set to 'true'
    When The user sets the renovation visibility to 'false'
    Then The renovation is set to private
    And The user is redirected to the individual renovation page

  Scenario Outline: AC3.1 - An anonymous user can browse public renovations in order
    Given a user registers with first name "<first_name>", last name "<last_name>", email "<user_email>", password "<password>", retype password "<password>"
    And a user with email "<user_email>" creates a renovation with title "<title1>", description "<description1>", timestamp "<timestamp1>"
    And a user with email "<user_email>" makes the renovation with title "<title1>" public
    And a user with email "<user_email>" creates a renovation with title "<title2>", description "<description2>", timestamp "<timestamp2>"
    And a user with email "<user_email>" makes the renovation with title "<title2>" public
    And a user with email "<user_email>" creates a renovation with title "<title3>", description "<description3>", timestamp "<timestamp3>"
    And a user with email "<user_email>" creates a renovation with title "<title4>", description "<description4>", timestamp "<timestamp4>"
    When an anonymous user goes to page 1 of the browse renovations page
    Then they should see a public renovation created by "<user_email>", titled "<title2>"
    And they should see a public renovation created by "<user_email>", titled "<title1>"
    And the renovations are displayed in descending order by timestamp

    Examples:
      | first_name | last_name | user_email       | password  | title1     | description1         | timestamp1       | title2     | description2         | timestamp2       | title3      | description3         | timestamp3       | title4     | description4         | timestamp4       |
      | Jimmy      | Smith    | jimmy@gmail.com  | Abc123!!  | Bathroom   | Bathroom Makeover    | 2004-12-20 07:30 | Kitchen    | Kitchen Makeover     | 2007-12-20 07:30 | Bedroom     | Bedroom Redesign     | 2010-06-15 09:45 | Living Room | Living Room Upgrade | 2015-09-10 14:20 |

  Scenario Outline: AC3.2 - A logged in user can browse public renovations in order
    Given a user registers with first name "<first_name>", last name "<last_name>", email "<user_email>", password "<password>", retype password "<password>"
    And a user with email "<user_email>" creates a renovation with title "<title1>", description "<description1>", timestamp "<timestamp1>"
    And a user with email "<user_email>" makes the renovation with title "<title1>" public
    And a user with email "<user_email>" creates a renovation with title "<title2>", description "<description2>", timestamp "<timestamp2>"
    And a user with email "<user_email>" makes the renovation with title "<title2>" public
    And a user with email "<user_email>" creates a renovation with title "<title3>", description "<description3>", timestamp "<timestamp3>"
    And a user with email "<user_email>" creates a renovation with title "<title4>", description "<description4>", timestamp "<timestamp4>"
    And a user registers with first name "<first_name2>", last name "<last_name2>", email "<user_email2>", password "<password2>", retype password "<password2>"
    And a user with email "<user_email2>" creates a renovation with title "<title1>", description "<description1>", timestamp "<timestamp1>"
    And a user with email "<user_email2>" makes the renovation with title "<title1>" public
    When a logged in user with "<user_email2>" goes to page 1 of the browse renovations page
    Then they should see a public renovation created by "<user_email>", titled "<title2>"
    And they should see a public renovation created by "<user_email>", titled "<title1>"
    And the renovations are displayed in descending order by timestamp

    Examples:
      | first_name  | last_name  | user_email        | password  | title1     | description1         | timestamp1       | title2     | description2         | timestamp2       | title3      | description3         | timestamp3       | title4      | description4         | timestamp4     | first_name2 | last_name2 | user_email2       | password2 |
      | Jimmy       | Smith     | jimmy@gmail.com  | Abc123!!  | Bathroom   | Bathroom Makeover    | 2004-12-20 07:30 | Kitchen    | Kitchen Makeover     | 2007-12-20 07:30 | Bedroom     | Bedroom Redesign     | 2010-06-15 09:45 | Living Room | Living Room Upgrade | 2015-09-10 14:20  | Susan       | Susanson   | myemail@email.com | secure!2  |
      | Sarah       | Johnson   | sarah@gmail.com  | Xyz789!!  | Garage     | Garage Renovation    | 2012-08-25 11:15 | Office     | Office Redesign      | 2016-03-18 16:50 | Garden      | Backyard Landscaping | 2019-07-22 10:00 | Basement    | Basement Makeover    | 2023-01-05 18:30 | Harry       | Harryson   | another@email.com | secrure3% |

  Scenario Outline: AC3.3 - An anonymous user cannot browse private renovations
    Given a user registers with first name "<first_name>", last name "<last_name>", email "<user_email>", password "<password>", retype password "<password>"
    And a user with email "<user_email>" creates a renovation with title "<title>", description "<description>", timestamp "<timestamp>"
    When an anonymous user goes to page 1 of the browse renovations page
    Then they should not see a private renovation created by "<user_email>", titled "<title>"

    Examples:
      | first_name  | last_name | user_email       | password  | title    | description       | timestamp        |
      | Jimmy       | Smith     | jimmy@gmail.com  | Abc123!!  | Bathroom | Bathroom Makeover | 2004-12-20 07:30 |
      | Holly       | Johnson   | holly@gmail.com  | Xyz789!!  | Garage   | Garage Renovation | 2012-08-25 11:15 |


  Scenario Outline: AC3.4 -  A user (anonymous or logged in) can view paginated renovations
    Given a user registers with first name "<first_name>", last name "<last_name>", email "<user_email>", password "<password>", retype password "<password>"
    And a user with email "<user_email>" creates a renovation with title "<title1>", description "<description>", timestamp "<timestamp1>"
    And a user with email "<user_email>" makes the renovation with title "<title1>" public
    And a user with email "<user_email>" creates a renovation with title "<title2>", description "<description>", timestamp "<timestamp2>"
    And a user with email "<user_email>" makes the renovation with title "<title2>" public
    And a user with email "<user_email>" creates a renovation with title "<title3>", description "<description>", timestamp "<timestamp3>"
    And a user with email "<user_email>" makes the renovation with title "<title3>" public
    And a user with email "<user_email>" creates a renovation with title "<title4>", description "<description>", timestamp "<timestamp4>"
    And a user with email "<user_email>" makes the renovation with title "<title4>" public
    And a user with email "<user_email>" creates a renovation with title "<title5>", description "<description>", timestamp "<timestamp5>"
    And a user with email "<user_email>" makes the renovation with title "<title5>" public
    And a user with email "<user_email>" creates a renovation with title "<title6>", description "<description>", timestamp "<timestamp6>"
    And a user with email "<user_email>" makes the renovation with title "<title6>" public
    And a user with email "<user_email>" creates a renovation with title "<title7>", description "<description>", timestamp "<timestamp7>"
    And a user with email "<user_email>" makes the renovation with title "<title7>" public
    And a user with email "<user_email>" creates a renovation with title "<title8>", description "<description>", timestamp "<timestamp8>"
    And a user with email "<user_email>" makes the renovation with title "<title8>" public
    When an anonymous user goes to page <page_number> of the browse renovations page
    Then There are <expected_renovations> renovations on page <page_number>
    And the renovations are displayed in descending order by timestamp
    Examples:
      | first_name | last_name | user_email         | password  | title1   | timestamp1        | description                | title2   | timestamp2        | title3   | timestamp3        | title4   | timestamp4        | title5   | timestamp5        | title6   | timestamp6        | title7   | timestamp7        | title8   | timestamp8        | page_number | expected_renovations |
      | Alice      | Builder   | alice@example.com  | Pass123!  | Reno 1   | 2025-05-10 12:00  | First renovation batch     | Reno 2   | 2025-05-11 02:00  | Reno 3   | 2025-05-12 12:00  | Reno 4   | 2025-05-13 12:00  | Reno 5   | 2025-05-14 12:00  | Reno 6   | 2025-05-15 12:00  | Reno 7   | 2025-05-16 12:00  | Reno 8   | 2025-05-17 12:00  | 1           | 3                     |
      | Alice      | Builder   | alice@example.com  | Pass123!  | Reno 1   | 2025-05-10 12:00  | Second renovation batch    | Reno 2   | 2025-05-11 02:00  | Reno 3   | 2025-05-12 12:00  | Reno 4   | 2025-05-13 12:00  | Reno 5   | 2025-05-14 12:00  | Reno 6   | 2025-05-15 12:00  | Reno 7   | 2025-05-16 12:00  | Reno 8   | 2025-05-17 12:00  | 2           | 3                     |
      | Bob        | Renovator | bob@example.com    | Build!t1  | Kitchen  | 2024-12-01 08:00  | Full property renovation   | Bathroom | 2024-12-02 09:00  | LivingRm | 2024-12-03 10:00  | Garage   | 2024-12-04 11:00  | Roof     | 2024-12-05 12:00  | Patio    | 2024-12-06 13:00  | Fence    | 2024-12-07 14:00  | Garden   | 2024-12-08 15:00  | 3           | 2                     |




