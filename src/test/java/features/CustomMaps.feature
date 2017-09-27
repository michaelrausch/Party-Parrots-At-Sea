Feature: Multiple Maps

  Scenario:
    Given that the game has multiple race xml files
    Then all of them can be seen

  Scenario:
    Given that I choose a race
    Then that race's course is received by clients

  Scenario:
    Given that I choose a name for the server
    Then that name is sent to the client

  Scenario:
    Given that the client has received a race
    Then the name of that race shown to the host is the course name