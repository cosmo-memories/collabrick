Feature: U24 - As Lei, I want to be able to manage the state of my tasks for a renovation record so that I’ll be able to
  assign individuals to be responsible for the task.

  Scenario Outline: AC2 - When I am creating a new task I can add a state to the task
    Given a user registers with first name "<first_name>", last name "<last_name>", email "<user_email>", password "<password>", retype password "<password>"
    And a user with email "<user_email>" creates a renovation with title "<title1>", description "<description1>", timestamp "<timestamp1>"
    When a user with email "<user_email>" creates a new task with name "<task_name>" and description "<task_description>" with state "<state>" for renovation "<title1>"
    And a user with email "<user_email>" goes to the individual renovation tasks page of renovation with name "<title1>"
    Then A user with email "<user_email>" has a task for renovation "<title1>" called "<task_name>" with state "<state>"

    Examples:
      | first_name | last_name | user_email            | password       | title1        | description1                 | timestamp1        | task_name         | task_description                | state        |
      | Alice      | Smith     | alice.smith@test.com  | Password123!   | Kitchen Reno  | Update kitchen cabinets      | 2025-05-20 10:00  | Paint Cabinets    | Paint cabinet surfaces white    | Not Started  |
      | Bob        | Jones     | bob.jones@test.com    | SecurePass456! | Bathroom Reno | Install new shower head      | 2025-05-21 11:15  | Install Plumbing  | Connect water supply lines      | In Progress  |
      | Carol      | Lee       | carol.lee@test.com    | MyPass789?     | Deck Expansion| Extend deck by 10 feet       | 2025-05-22 09:45  | Lay Deck Boards   | Lay wooden boards               | Blocked      |
      | Dave       | Kim       | dave.kim@test.com     | S3cur3P@ss     | Patio Cover   | Build patio cover roof       | 2025-05-23 14:00  | Install Rafters   | Install supporting rafters      | Completed    |
      | Eve        | Patel     | eve.patel@test.com    | StrongPwd!     | Garage Door   | Replace garage door opener   | 2025-05-24 16:30  | Test Opener       | Test remote-operated opener     | Cancelled    |