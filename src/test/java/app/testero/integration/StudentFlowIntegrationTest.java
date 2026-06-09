package app.testero.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("integration")
@ImportTestcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentFlowIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    TestRestTemplate rest;

    private String token;
    private String assessmentId;
    private List<Map<String, Object>> questions;

    // ── 1. Login ───────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("POST /auth/login → 200, returns JWT and user info")
    void login() {
        var body = Map.of("username", "a.rossi", "password", "password");

        ResponseEntity<Map> response = rest.postForEntity(
                "/auth/login", body, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("token");
        assertThat(response.getBody()).containsKey("user");

        token = (String) response.getBody().get("token");
        assertThat(token).isNotBlank();

        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) response.getBody().get("user");
        assertThat(user.get("name")).isEqualTo("Alice Rossi");
        assertThat(user.get("username")).isEqualTo("a.rossi");
        assertThat(user.get("class_name")).isEqualTo("Demo-2026");
    }

    // ── 2. Get assessments ─────────────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("GET /assessments → 200, returns available assessments")
    void getAssessments() {
        ResponseEntity<Map> response = rest.exchange(
                "/assessments", HttpMethod.GET,
                withAuth(null), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> assessments =
                (List<Map<String, Object>>) response.getBody().get("assessments");
        assertThat(assessments).isNotEmpty();

        assessmentId = (String) assessments.get(0).get("id");
        assertThat(assessmentId).isNotBlank();
    }

    // ── 3. Get config ──────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("GET /assessments/{id}/config → 200, scoring rules present")
    void getConfig() {
        ResponseEntity<Map> response = rest.exchange(
                "/assessments/{id}/config", HttpMethod.GET,
                withAuth(null), Map.class, assessmentId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        Map<String, Object> scoring =
                (Map<String, Object>) response.getBody().get("scoring");
        assertThat(scoring.get("pointsPerCorrect")).isEqualTo(1.0);
        assertThat(scoring.get("pointsPerWrong")).isEqualTo(-0.25);
    }

    // ── 4. Get questions ───────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("GET /assessments/{id}/questions → 200, no correct answers exposed")
    void getQuestions() {
        ResponseEntity<Map> response = rest.exchange(
                "/assessments/{id}/questions", HttpMethod.GET,
                withAuth(null), Map.class, assessmentId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> q =
                (List<Map<String, Object>>) response.getBody().get("questions");
        assertThat(q).isNotEmpty();

        for (Map<String, Object> question : q) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> options =
                    (List<Map<String, Object>>) question.get("options");
            if (options != null) {
                for (Map<String, Object> opt : options) {
                    assertThat(opt).doesNotContainKey("correct");
                }
            }
        }

        questions = q;
    }

    // ── 5. Submit answers ──────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("POST /submissions → 201, returns graded feedback")
    void submitAnswers() {
        List<Map<String, Object>> answers = questions.stream()
                .map(q -> {
                    String type = (String) q.get("type");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> options =
                            (List<Map<String, Object>>) q.get("options");

                    if ("multiple".equals(type) && options != null && !options.isEmpty()) {
                        return Map.<String, Object>of(
                                "question_id", q.get("id"),
                                "type", "multiple",
                                "selected_option_ids", List.of(options.get(0).get("id"))
                        );
                    } else {
                        return Map.<String, Object>of(
                                "question_id", q.get("id"),
                                "type", "open",
                                "text", "Test answer"
                        );
                    }
                })
                .toList();

        Map<String, Object> submission = Map.of(
                "assessment_id", assessmentId,
                "answers", answers
        );

        ResponseEntity<Map> response = rest.exchange(
                "/submissions", HttpMethod.POST,
                withAuth(submission), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKey("id");
        assertThat(response.getBody()).containsKey("answers");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answerResults =
                (List<Map<String, Object>>) response.getBody().get("answers");
        assertThat(answerResults).hasSameSizeAs(questions);

        for (Map<String, Object> result : answerResults) {
            assertThat(result).containsKey("is_correct");
            assertThat(result).containsKey("correct_option_ids");
        }
    }

    // ── 6. Verify security ─────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("GET /assessments without token → 403")
    void unauthorizedAccess() {
        ResponseEntity<Map> response = rest.getForEntity(
                "/assessments", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ── Helpers ─────────────────────────────────────────────────────

    private HttpEntity<Object> withAuth(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
