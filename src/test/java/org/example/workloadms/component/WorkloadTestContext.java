package org.example.workloadms.component;

import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;

@Component
@Getter
@Setter
public class WorkloadTestContext {

    private Response lastResponse;
    private String currentTrainerUsername;
    private String authToken;
    private HttpResponse<String> lastHttpResponse;

    public void reset() {
        lastResponse = null;
        currentTrainerUsername = null;
        authToken = null;
        lastHttpResponse = null;
    }
}
