package com.example.javaproject;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScamReportEndToEndTest extends PostgresIntegrationTestBase {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${local.server.port}")
    private int port;

    @Test
    void twoUsersReportingSameNumberCreatesPublicWarningAndSyncEntry() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String phoneNumber = "+372555" + ThreadLocalRandom.current().nextInt(10000, 100000);
        String firstToken = registerAndLogin("first-" + suffix + "@example.test");
        String secondToken = registerAndLogin("second-" + suffix + "@example.test");

        submitReport(firstToken, phoneNumber);
        HttpResponse<String> firstWarnings = get("/api/v1/warnings");
        assertThat(firstWarnings.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(firstWarnings.body()))
                .noneMatch(warning -> phoneNumber.equals(warning.path("phoneNumber").asText()));

        submitReport(secondToken, phoneNumber);
        HttpResponse<String> activeWarnings = get("/api/v1/warnings");
        assertThat(activeWarnings.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(activeWarnings.body()))
                .anyMatch(warning -> phoneNumber.equals(warning.path("phoneNumber").asText())
                        && "ACTIVE".equals(warning.path("status").asText()));

        HttpResponse<String> sync = get("/api/v1/sync/warnings");
        assertThat(sync.statusCode()).isEqualTo(200);
        JsonNode syncResponse = objectMapper.readTree(sync.body());
        assertThat(syncResponse.path("syncTime").isTextual()).isTrue();
        assertThat(syncResponse.path("items"))
                .anyMatch(warning -> phoneNumber.equals(warning.path("phoneNumber").asText())
                        && "ACTIVE".equals(warning.path("status").asText()));
    }

    @Test
    void acceptedDisputeWithdrawsWarningAndAppearsInIncrementalSync() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String phoneNumber = "+372555" + ThreadLocalRandom.current().nextInt(10000, 100000);
        String firstToken = registerAndLogin("dispute-first-" + suffix + "@example.test");
        String secondToken = registerAndLogin("dispute-second-" + suffix + "@example.test");
        String moderatorEmail = "moderator-" + suffix + "@example.test";
        register(moderatorEmail);
        jdbcTemplate.update("UPDATE app_user SET role = 'MODERATOR' WHERE email = ?", moderatorEmail);
        String moderatorToken = login(moderatorEmail);

        submitReport(firstToken, phoneNumber);
        submitReport(secondToken, phoneNumber);
        JsonNode activeWarning = findWarning(get("/api/v1/warnings"), phoneNumber);
        long warningId = activeWarning.path("id").asLong();

        HttpResponse<String> submittedDispute = post(
                "/api/v1/warnings/" + warningId + "/disputes",
                firstToken,
                """
                        {"reason":"The number was reassigned and the warning is no longer accurate."}
                        """
        );
        assertThat(submittedDispute.statusCode()).isEqualTo(201);
        long disputeId = objectMapper.readTree(submittedDispute.body()).path("id").asLong();

        HttpResponse<String> queue = get("/api/v1/moderation/disputes", moderatorToken);
        assertThat(queue.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(queue.body()))
                .anyMatch(dispute -> dispute.path("id").asLong() == disputeId);

        HttpResponse<String> review = put(
                "/api/v1/moderation/disputes/" + disputeId,
                moderatorToken,
                """
                        {
                          "decision": "ACCEPTED",
                          "reviewComment": "The dispute was verified."
                        }
                        """
        );
        assertThat(review.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(review.body()).path("status").asText()).isEqualTo("ACCEPTED");

        HttpResponse<String> activeWarnings = get("/api/v1/warnings");
        assertThat(activeWarnings.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(activeWarnings.body()))
                .noneMatch(warning -> phoneNumber.equals(warning.path("phoneNumber").asText()));

        HttpResponse<String> sync = get("/api/v1/sync/warnings?updatedAfter=1970-01-01T00:00:00Z");
        assertThat(sync.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(sync.body()).path("items"))
                .anyMatch(warning -> warning.path("id").asLong() == warningId
                        && "WITHDRAWN".equals(warning.path("status").asText()));
    }

    private String registerAndLogin(String email) throws Exception {
        register(email);
        return login(email);
    }

    private void register(String email) throws Exception {
        String credentials = objectMapper.writeValueAsString(new Credentials(email, "StrongPassword123!"));
        HttpResponse<String> registration = post("/api/v1/auth/register", null, credentials);
        assertThat(registration.statusCode()).isEqualTo(201);
        assertThat(objectMapper.readTree(registration.body()).path("role").asText()).isEqualTo("USER");
    }

    private String login(String email) throws Exception {
        String credentials = objectMapper.writeValueAsString(new Credentials(email, "StrongPassword123!"));
        HttpResponse<String> login = post("/api/v1/auth/login", null, credentials);
        assertThat(login.statusCode()).isEqualTo(200);
        JsonNode tokenResponse = objectMapper.readTree(login.body());
        return tokenResponse.get("accessToken").asText();
    }

    private void submitReport(String accessToken, String phoneNumber) throws Exception {
        HttpResponse<String> report = post(
                "/api/v1/reports",
                accessToken,
                """
                        {
                          "phoneNumber": "%s",
                          "categoryCode": "POLICE",
                          "incidentDate": "2026-11-24",
                          "communicationChannel": "PHONE_CALL",
                          "description": "End-to-end test report"
                        }
                        """.formatted(phoneNumber)
        );
        assertThat(report.statusCode()).isEqualTo(201);
        JsonNode createdReport = objectMapper.readTree(report.body());
        assertThat(createdReport.path("phoneNumber").asText()).isEqualTo(phoneNumber);
        assertThat(createdReport.path("categoryCode").asText()).isEqualTo("POLICE");
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        return get(path, null);
    }

    private HttpResponse<String> get(String path, String token) throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl() + path)).GET();
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return httpClient.send(
                request.build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private HttpResponse<String> post(String path, String token, String body)
            throws IOException, InterruptedException {
        return sendWithBody("POST", path, token, body);
    }

    private HttpResponse<String> put(String path, String token, String body)
            throws IOException, InterruptedException {
        return sendWithBody("PUT", path, token, body);
    }

    private HttpResponse<String> sendWithBody(String method, String path, String token, String body)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl() + path))
                .header("Content-Type", "application/json");
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        request.method(method, HttpRequest.BodyPublishers.ofString(body));
        return httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode findWarning(HttpResponse<String> response, String phoneNumber) throws IOException {
        assertThat(response.statusCode()).isEqualTo(200);
        return objectMapper.readTree(response.body()).valueStream()
                .filter(warning -> phoneNumber.equals(warning.path("phoneNumber").asText()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected warning for " + phoneNumber));
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private record Credentials(String email, String password) {
    }
}
