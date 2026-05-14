package org.example.workloadms.component.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.workloadms.component.WorkloadTestContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkloadSteps {

    private final WorkloadTestContext context;

    public WorkloadSteps(WorkloadTestContext context) {
        this.context = context;
    }

    @Given("the workload service is running")
    public void theWorkloadServiceIsRunning() {
    }

    @Given("workload is processed for trainer {string} date {string} duration {int} action {string}")
    public void workloadIsProcessedForTrainer(String username, String date, int duration, String action) {
        Map<String, Object> body = buildWorkloadBody(username, "First", "Trainer", true, date, duration, action);

        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/workload");

        assertThat(response.statusCode()).isEqualTo(200);
        context.setCurrentTrainerUsername(username);
    }

    @When("I process workload for trainer {string} firstName {string} lastName {string} date {string} duration {int} action {string}")
    public void iProcessWorkload(String username, String firstName, String lastName,
                                  String date, int duration, String action) {
        Map<String, Object> body = buildWorkloadBody(username, firstName, lastName, true, date, duration, action);

        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/workload");

        context.setLastResponse(response);
        context.setCurrentTrainerUsername(username);
    }

    @When("I process workload for inactive trainer {string} date {string} duration {int}")
    public void iProcessWorkloadForInactiveTrainer(String username, String date, int duration) {
        Map<String, Object> body = buildWorkloadBody(username, "Inactive", "Trainer", false, date, duration, "ADD");

        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/workload");

        context.setLastResponse(response);
    }

    @When("I get working hours for trainer {string} year {int} month {int}")
    public void iGetWorkingHours(String username, int year, int month) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:" + RestAssured.port +
                            "/api/workload/" + username + "?year=" + year + "&month=" + month))
                    .header("Authorization", "Bearer " + context.getAuthToken())
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> httpResponse =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            context.setLastHttpResponse(httpResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @When("I process workload with blank trainer username")
    public void iProcessWorkloadWithBlankUsername() {
        Map<String, Object> body = buildWorkloadBody("", "First", "Last", true, "2024-06-01", 60, "ADD");

        Response response = RestAssured
                .given()
                .redirects().follow(false)
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/workload");

        context.setLastResponse(response);
    }

    @When("I process workload with null training date")
    public void iProcessWorkloadWithNullDate() {
        Map<String, Object> body = new HashMap<>();
        body.put("trainerUsername", "trainer.nulldate");
        body.put("trainerFirstName", "First");
        body.put("trainerLastName", "Last");
        body.put("isActive", true);
        body.put("trainingDate", null);
        body.put("trainingDuration", 60);
        body.put("actionType", "ADD");

        context.setLastResponse(RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/workload"));
    }

    @When("I process workload with zero duration for trainer {string}")
    public void iProcessWorkloadWithZeroDuration(String username) {
        Map<String, Object> body = buildWorkloadBody(username, "First", "Last", true, "2024-06-01", 0, "ADD");

        context.setLastResponse(RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/workload"));
    }

    @When("I process workload with negative duration for trainer {string}")
    public void iProcessWorkloadWithNegativeDuration(String username) {
        Map<String, Object> body = buildWorkloadBody(username, "First", "Last", true, "2024-06-01", -30, "ADD");

        context.setLastResponse(RestAssured
                .given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/workload"));
    }

    @When("I process workload without authentication for trainer {string}")
    public void iProcessWorkloadWithoutAuth(String username) {
        Map<String, Object> body = buildWorkloadBody(username, "First", "Last", true, "2024-06-01", 60, "ADD");

        context.setLastResponse(RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/workload"));
    }

    @Then("the workload response status should be {int}")
    public void theWorkloadResponseStatusShouldBe(int expected) {
        if (context.getLastResponse() != null) {
            assertThat(context.getLastResponse().statusCode())
                    .as("Expected HTTP %d but got %d. Body: %s",
                            expected, context.getLastResponse().statusCode(),
                            context.getLastResponse().body().asString())
                    .isEqualTo(expected);
        } else {
            assertThat(context.getLastHttpResponse().statusCode())
                    .as("Expected HTTP %d but got %d. Body: %s",
                            expected, context.getLastHttpResponse().statusCode(),
                            context.getLastHttpResponse().body())
                    .isEqualTo(expected);
        }
    }

    @Then("the working hours should be {double}")
    public void theWorkingHoursShouldBe(double expected) throws Exception {
        String body = context.getLastHttpResponse().body();
        double actual = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(body).get("workingHours").asDouble();
        assertThat(actual).isEqualTo(expected);
    }

    @Then("when I get working hours for trainer {string} year {int} month {int} the duration should be {double}")
    public void whenIGetWorkingHoursTheDurationShouldBe(String username, int year, int month, double expected) throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:" + RestAssured.port +
                        "/api/workload/" + username + "?year=" + year + "&month=" + month))
                .header("Authorization", "Bearer " + context.getAuthToken())
                .GET()
                .build();

        java.net.http.HttpResponse<String> httpResponse =
                client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponse.statusCode()).isEqualTo(200);
        double actual = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(httpResponse.body()).get("workingHours").asDouble();
        assertThat(actual).isEqualTo(expected);
    }

    private Map<String, Object> buildWorkloadBody(String username, String firstName, String lastName,
                                                   boolean isActive, String date, int duration, String action) {
        Map<String, Object> body = new HashMap<>();
        body.put("trainerUsername", username);
        body.put("trainerFirstName", firstName);
        body.put("trainerLastName", lastName);
        body.put("isActive", isActive);
        body.put("trainingDate", date);
        body.put("trainingDuration", duration);
        body.put("actionType", action);
        return body;
    }
}
