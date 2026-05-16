Feature: Workload Service Integration
  As a GYM platform
  I want the Workload service to process messages from ActiveMQ
  So that trainer working hours are tracked correctly

  Background:
    Given the workload service is running

  Scenario: ActiveMQ message with ADD action is processed and saved to MongoDB
    When a workload message is sent to the queue for trainer "mq.trainer.one" action "ADD" date "2024-06-15" duration 60
    Then the workload collection should contain trainer "mq.trainer.one"
    And the working hours for trainer "mq.trainer.one" in year 2024 month 6 should be 1.0

  Scenario: ActiveMQ message with DELETE action reduces working hours in MongoDB
    Given workload is processed for trainer "mq.trainer.two" action "ADD" date "2024-07-10" duration 120
    When a workload message is sent to the queue for trainer "mq.trainer.two" action "DELETE" date "2024-07-10" duration 60
    Then the working hours for trainer "mq.trainer.two" in year 2024 month 7 should be 1.0

  Scenario: Multiple ADD messages accumulate hours in MongoDB
    Given workload is processed for trainer "mq.trainer.three" action "ADD" date "2024-08-01" duration 60
    And workload is processed for trainer "mq.trainer.three" action "ADD" date "2024-08-15" duration 90
    Then the working hours for trainer "mq.trainer.three" in year 2024 month 8 should be 2.5

  Scenario: Hours tracked independently per month
    Given workload is processed for trainer "mq.trainer.four" action "ADD" date "2024-09-01" duration 60
    And workload is processed for trainer "mq.trainer.four" action "ADD" date "2024-10-01" duration 90
    Then the working hours for trainer "mq.trainer.four" in year 2024 month 9 should be 1.0
    And the working hours for trainer "mq.trainer.four" in year 2024 month 10 should be 1.5

  Scenario: Invalid message with blank username goes to DLQ and is not saved
    When an invalid workload message with blank username is sent to the queue
    Then the workload collection should not contain trainer ""

  Scenario: Invalid message with zero duration goes to DLQ and is not saved
    When an invalid workload message with zero duration is sent to the queue for trainer "mq.trainer.bad"
    Then the workload collection should not contain trainer "mq.trainer.bad"