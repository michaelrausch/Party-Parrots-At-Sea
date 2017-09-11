Feature: SendChat
  Scenario: User send chat to another client
    Given There are two games running
    When the first client has sent the message "Hello world"
    Then the other client should receive the message "Hello world"
