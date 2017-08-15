Feature: SailsToggle
  Scenario: User toggles in sail
    Given The game is running
    When the user has pressed "shift"
    Then the sails are "in"