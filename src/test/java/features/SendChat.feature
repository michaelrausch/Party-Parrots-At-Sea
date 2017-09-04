Feature: SendChat
  Scenario: User send chat to another client
    Given The are two games running
    When the user has pressed sends the message "Hello world" in a text box
    Then the other client should recieve the message "Hello world"