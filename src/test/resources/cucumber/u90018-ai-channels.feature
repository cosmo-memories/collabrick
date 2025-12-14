Feature: U90018 - As Susan, I would like to have an AI assistant so that I can ask general questions about my renovation and receive relevant answers.

  Scenario: AC1 - Given I am on a renovation that I am a member of, when I click the chat channel BrickAI, then I am taken to a chat channel labelled BrickAI.
    Given A user with the email 'user@test.com' is logged in
    And An account for BrickAI exists
    And I input 'RenovationName' into the renovation record 'name' field
    And I input 'RenovationDescription' into the renovation record 'description' field
    When I click the Create button
    And I click on the renovation
    Then A chat named brickAI is created where "user@test.com" and brickAI are the only members
    And I can see a chat channel named brickAI

  Scenario: AC2 - Given I am on the BrickAI chat channel, when I click the membership list, then the only members are myself and BrickAI.
    Given A user with the email 'user@test.com' is logged in
    And An account for BrickAI exists
    And I input 'RenovationName' into the renovation record 'name' field
    And I input 'RenovationDescription' into the renovation record 'description' field
    And I click the Create button
    And I click on the renovation
    And A new renovation record is created with name 'RenovationName' and description 'RenovationDescription'
    When I click on the brickAI chat channel
    Then The channel member list shows only myself and BrickAI