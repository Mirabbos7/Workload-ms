package org.example.workloadms.integration.steps;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.example.workloadms.component.WorkloadTestContext;
import org.example.workloadms.entity.Trainer;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkloadIntegrationSteps {

    private final WorkloadTestContext context;
    private final MongoTemplate mongoTemplate;

    public WorkloadIntegrationSteps(WorkloadTestContext context, MongoTemplate mongoTemplate) {
        this.context = context;
        this.mongoTemplate = mongoTemplate;
    }

    @Given("workload is processed for trainer {string} action {string} date {string} duration {int}")
    public void workloadIsProcessedForTrainer(String username, String action, String date, int duration) {
        sendWorkloadMessage(username, action, date, duration);
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Trainer trainer = mongoTemplate.findOne(
                            Query.query(Criteria.where("username").is(username)),
                            Trainer.class);
                    assertThat(trainer).isNotNull();
                });
    }

    @When("a workload message is sent to the queue for trainer {string} action {string} date {string} duration {int}")
    public void whenWorkloadMessageIsSentToQueue(String username, String action, String date, int duration) {
        sendWorkloadMessage(username, action, date, duration);
    }

    @When("an invalid workload message with blank username is sent to the queue")
    public void anInvalidWorkloadMessageWithBlankUsernameIsSentToQueue() {
        Map<String, Object> body = new HashMap<>();
        body.put("trainerUsername", "");
        body.put("trainerFirstName", "Test");
        body.put("trainerLastName", "Trainer");
        body.put("isActive", true);
        body.put("trainingDate", "2024-06-01");
        body.put("trainingDuration", 60);
        body.put("actionType", "ADD");

        context.setLastResponse(RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/workload"));
    }

    @When("an invalid workload message with zero duration is sent to the queue for trainer {string}")
    public void anInvalidWorkloadMessageWithZeroDurationIsSentToQueue(String username) {
        Map<String, Object> body = new HashMap<>();
        body.put("trainerUsername", username);
        body.put("trainerFirstName", "Test");
        body.put("trainerLastName", "Trainer");
        body.put("isActive", true);
        body.put("trainingDate", "2024-06-01");
        body.put("trainingDuration", 0);
        body.put("actionType", "ADD");

        context.setLastResponse(RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/workload"));
    }

    @Then("the workload collection should contain trainer {string}")
    public void theWorkloadCollectionShouldContainTrainer(String username) {
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Trainer trainer = mongoTemplate.findOne(
                            Query.query(Criteria.where("username").is(username)),
                            Trainer.class);
                    assertThat(trainer).isNotNull();
                });
    }

    @Then("the workload collection should not contain trainer {string}")
    public void theWorkloadCollectionShouldNotContainTrainer(String username) {
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        Trainer trainer = mongoTemplate.findOne(
                Query.query(Criteria.where("username").is(username)),
                Trainer.class);
        assertThat(trainer).isNull();
    }

    @Then("the working hours for trainer {string} in year {int} month {int} should be {double}")
    public void theWorkingHoursForTrainerShouldBe(String username, int year, int month, double expectedHours) {
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Trainer trainer = mongoTemplate.findOne(
                            Query.query(Criteria.where("username").is(username)),
                            Trainer.class);
                    assertThat(trainer).isNotNull();

                    int totalMinutes = trainer.getYearList().stream()
                            .filter(y -> y.getYear().equals(String.valueOf(year)))
                            .flatMap(y -> y.getMonthList().stream())
                            .filter(m -> m.getMonth().equals(String.valueOf(month)))
                            .mapToInt(Trainer.Month::getTrainingSummaryDuration)
                            .sum();

                    double actualHours = totalMinutes / 60.0;
                    assertThat(actualHours).isEqualTo(expectedHours);
                });
    }

    private void sendWorkloadMessage(String username, String action, String date, int duration) {
        Map<String, Object> body = new HashMap<>();
        body.put("trainerUsername", username);
        body.put("trainerFirstName", "Test");
        body.put("trainerLastName", "Trainer");
        body.put("isActive", true);
        body.put("trainingDate", date);
        body.put("trainingDuration", duration);
        body.put("actionType", action);

        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/workload");

        assertThat(response.statusCode()).isEqualTo(200);
    }
}