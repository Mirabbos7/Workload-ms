Feature: Workload Management
  As a GYM workload microservice
  I want to process trainer workload data
  So that training hours can be tracked per trainer per month

  Background:
    Given the workload service is running

  Scenario: Successfully add workload for a trainer (ADD action)
    When I process workload for trainer "trainer.one" firstName "Trainer" lastName "One" date "2024-06-15" duration 60 action "ADD"
    Then the workload response status should be 200

  Scenario: Successfully get trainer working hours after adding workload
    Given workload is processed for trainer "trainer.two" date "2024-06-15" duration 120 action "ADD"
    When I get working hours for trainer "trainer.two" year 2024 month 6
    Then the workload response status should be 200
    And the working hours should be 2.0

  Scenario: Successfully delete workload for a trainer (DELETE action)
    Given workload is processed for trainer "trainer.three" date "2024-06-15" duration 120 action "ADD"
    When I process workload for trainer "trainer.three" firstName "Trainer" lastName "Three" date "2024-06-15" duration 60 action "DELETE"
    Then the workload response status should be 200
    And when I get working hours for trainer "trainer.three" year 2024 month 6 the duration should be 1.0

  Scenario: Successfully accumulate workload across multiple sessions
    Given workload is processed for trainer "trainer.four" date "2024-07-10" duration 60 action "ADD"
    And workload is processed for trainer "trainer.four" date "2024-07-20" duration 90 action "ADD"
    When I get working hours for trainer "trainer.four" year 2024 month 7
    Then the workload response status should be 200
    And the working hours should be 2.5

  Scenario: Successfully handle workload for inactive trainer (subtracts duration)
    Given workload is processed for trainer "trainer.five" date "2024-08-01" duration 120 action "ADD"
    When I process workload for inactive trainer "trainer.five" date "2024-08-01" duration 60
    Then the workload response status should be 200

  Scenario: Duration does not go below zero when deleting more than available
    Given workload is processed for trainer "trainer.six" date "2024-09-01" duration 30 action "ADD"
    When I process workload for trainer "trainer.six" firstName "Trainer" lastName "Six" date "2024-09-01" duration 120 action "DELETE"
    Then the workload response status should be 200
    And when I get working hours for trainer "trainer.six" year 2024 month 9 the duration should be 0.0

  Scenario: Successfully get working hours for January independently
    Given workload is processed for trainer "trainer.seven.jan" date "2024-01-15" duration 60 action "ADD"
    When I get working hours for trainer "trainer.seven.jan" year 2024 month 1
    Then the workload response status should be 200
    And the working hours should be 1.0

  Scenario: Successfully get working hours for February independently
    Given workload is processed for trainer "trainer.seven.feb" date "2024-02-15" duration 90 action "ADD"
    When I get working hours for trainer "trainer.seven.feb" year 2024 month 2
    Then the workload response status should be 200
    And the working hours should be 1.5

  Scenario: Fail to process workload with blank trainer username
    When I process workload with blank trainer username
    Then the workload response status should be 400

  Scenario: Fail to process workload with null training date
    When I process workload with null training date
    Then the workload response status should be 400

  Scenario: Fail to process workload with zero duration
    When I process workload with zero duration for trainer "trainer.bad"
    Then the workload response status should be 400

  Scenario: Fail to process workload with negative duration
    When I process workload with negative duration for trainer "trainer.bad2"
    Then the workload response status should be 400

  Scenario: Fail to get working hours for non-existent trainer
    When I get working hours for trainer "nobody.here999" year 2024 month 6
    Then the workload response status should be 404

  Scenario: Fail to get working hours for non-existent year
    Given workload is processed for trainer "trainer.eight" date "2024-06-01" duration 60 action "ADD"
    When I get working hours for trainer "trainer.eight" year 2023 month 6
    Then the workload response status should be 404

  Scenario: Fail to get working hours for non-existent month
    Given workload is processed for trainer "trainer.nine" date "2024-06-01" duration 60 action "ADD"
    When I get working hours for trainer "trainer.nine" year 2024 month 1
    Then the workload response status should be 404

  Scenario: Fail to get working hours with invalid month (0)
    When I get working hours for trainer "trainer.ten" year 2024 month 0
    Then the workload response status should be 400

  Scenario: Fail to get working hours with invalid month (13)
    When I get working hours for trainer "trainer.eleven" year 2024 month 13
    Then the workload response status should be 400

  Scenario: Fail to get working hours with future year
    When I get working hours for trainer "trainer.twelve" year 2199 month 6
    Then the workload response status should be 400

  Scenario: Fail to process workload without authentication
    When I process workload without authentication for trainer "trainer.noauth"
    Then the workload response status should be 401