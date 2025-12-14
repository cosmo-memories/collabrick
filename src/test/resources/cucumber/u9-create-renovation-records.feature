Feature: U9 - As Kaia, I want to create a record of my renovation so that I can manage all the tasks that must be
  completed

  Scenario: AC1 - Can access a renovation record form
    Given A user with the email 'user@test.com' is logged in
    When I click Create New Renovation Record
    Then I see a form to create a new renovation record


  Scenario: AC2 - Create a new renovation record
    Given A user with the email 'user@test.com' is logged in
    And I input 'RenovationName' into the renovation record 'name' field
    And I input 'RenovationDescription' into the renovation record 'description' field
    When I click the Create button
    Then A new renovation record is created with name 'RenovationName' and description 'RenovationDescription'
    And The user is redirected to the created renovation page

  Scenario: AC2.1 - Create a new renovation record with rooms
    Given A user with the email 'user@test.com' is logged in
    And I input 'RenovationName' into the renovation record 'name' field
    And I input 'RenovationDescription' into the renovation record 'description' field
    And I input 'Kitchen' into the renovation record 'room' field
    When I click the Create button
    Then A new renovation record is created with name 'RenovationName', description 'RenovationDescription' and room 'Kitchen'
    And The user is redirected to the created renovation page


  Scenario Outline: AC3 - Invalid Name input
    Given A user with the email 'user@test.com' is logged in
    And I input "<RenovationName>" into the renovation record 'name' field
    And I input 'RenovationDescription' into the renovation record 'description' field
    When I click the Create button
    Then I should see an error message indicating invalid characters in the renovation record name field
    And No Renovation record is created
    Examples:
      | RenovationName |
      | []             |
      | ,              |
      | :              |

  Scenario: AC3.1 - Empty Name Input
    Given A user with the email 'user@test.com' is logged in
    And I input "" into the renovation record 'name' field
    And I input 'RenovationDescription' into the renovation record 'description' field
    When I click the Create button
    Then I should see an error message indicating the renovation record name field cannot be blank
    And No Renovation record is created

  Scenario: AC4 - Unique renovation names
    Given A user with the email 'user@test.com' is logged in
    And There exists a renovation with name 'RenovationName' and description 'RenovationDescription'
    And I input 'RenovationName' into the renovation record 'name' field
    And I input 'RenovationDescription' into the renovation record 'description' field
    When I click the Create button
    Then I should see an error message indicating the renovation record name is not unique
    And No new Renovation record is created

  Scenario Outline: AC5 - Long description input
    Given A user with the email 'user@test.com' is logged in
    And I input 'RenovationName' into the renovation record 'name' field
    And I input "<RenovationDescription>" into the renovation record 'description' field
    When I click the Create button
    Then I should see an error message indicating the renovation description is too long
    And No Renovation record is created
    Examples:
      | RenovationDescription |
      | Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus e s           |
