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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("integration")
@ImportTestcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class StudentFlowIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    TestRestTemplate rest;

    @Autowired
    JdbcTemplate jdbc;

    private String token;
    private String assessmentId;
    private String submissionId;
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

        Map<String, Object> user = (Map<String, Object>) response.getBody().get("user");
        assertThat(user.get("first_name")).isEqualTo("Alice");
        assertThat(user.get("last_name")).isEqualTo("Rossi");
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

        List<Map<String, Object>> assessments =
                (List<Map<String, Object>>) response.getBody().get("assessments");
        assertThat(assessments).isNotEmpty();

        Map<String, Object> pagination =
                (Map<String, Object>) response.getBody().get("pagination");
        assertThat(pagination).containsKey("total_elements");
        assertThat(pagination).containsKey("total_pages");
        assertThat(pagination).containsKey("page");
        assertThat(pagination).containsKey("size");

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

        Map<String, Object> scoring =
                (Map<String, Object>) response.getBody().get("scoring");
        // Scoring of the seeded "Python Certification Exam Practice": a PCEP-style
        // simulation, so a wrong answer costs nothing. The retired demo assessment
        // this test used to pick up scored -0.25 per wrong answer.
        assertThat(scoring.get("pointsPerCorrect")).isEqualTo(1.0);
        assertThat(scoring.get("pointsPerWrong")).isEqualTo(0.0);
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

        List<Map<String, Object>> q =
                (List<Map<String, Object>>) response.getBody().get("questions");
        assertThat(q).isNotEmpty();

        for (Map<String, Object> question : q) {
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

    // ── 5. Start assessment ──────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("POST /assessments/{id}/start → 201, returns submission_id")
    void startAssessment() {
        ResponseEntity<Map> response = rest.exchange(
                "/assessments/{id}/start", HttpMethod.POST,
                withAuth(null), Map.class, assessmentId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKey("submission_id");
        assertThat(response.getBody()).containsKey("started_at");

        submissionId = (String) response.getBody().get("submission_id");
        assertThat(submissionId).isNotBlank();
    }

    // ── 6. Submit answers ──────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("PUT /submissions/{id} → 200, returns graded feedback")
    void submitAnswers() {
        List<Map<String, Object>> answers = questions.stream()
                .map(q -> {
                    String type = (String) q.get("type");
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

        Map<String, Object> submission = Map.of("answers", answers);

        ResponseEntity<Map> response = rest.exchange(
                "/submissions/{id}", HttpMethod.PUT,
                withAuth(submission), Map.class, submissionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("id");
        assertThat(response.getBody()).containsKey("answers");

        List<Map<String, Object>> answerResults =
                (List<Map<String, Object>>) response.getBody().get("answers");
        assertThat(answerResults).hasSameSizeAs(questions);

        for (Map<String, Object> result : answerResults) {
            assertThat(result).containsKey("is_correct");
            assertThat(result).containsKey("correct_option_snapshot_ids");
        }

        // maxScore measures the questions this submission drew, not the whole pool
        // behind the snapshot (pointsPerCorrect is 1.0 and no seeded question
        // overrides it, so it equals the number of questions answered)
        assertThat(((Number) response.getBody().get("max_score")).doubleValue())
                .isEqualTo(questions.size());
    }

    // ── 7. Get submission history ─────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("GET /submissions/mine → 200, returns submission history")
    void getSubmissionHistory() {
        ResponseEntity<Map> response = rest.exchange(
                "/submissions/mine", HttpMethod.GET,
                withAuth(null), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<Map<String, Object>> submissions =
                (List<Map<String, Object>>) response.getBody()
                        .get("submissions");
        assertThat(submissions).isNotEmpty();
        assertThat(submissions.get(0))
                .containsKey("assessment_title");
        assertThat(submissions.get(0))
                .containsKey("correct_count");
        assertThat(submissions.get(0))
                .containsKey("wrong_count");

        Map<String, Object> pagination =
                (Map<String, Object>) response.getBody().get("pagination");
        assertThat(pagination).containsKey("total_elements");
        assertThat(pagination).containsKey("total_pages");
    }

    // ── 8. Submit after incremental save ──────────────────────────

    @Test
    @Order(8)
    @DisplayName("Incremental save then submit → 200, no duplicate key violation")
    void submitAfterIncrementalSave() {
        // Start a new submission (retake)
        ResponseEntity<Map> startResponse = rest.exchange(
                "/assessments/{id}/start", HttpMethod.POST,
                withAuth(null), Map.class, assessmentId);

        assertThat(startResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String retakeSubmissionId = (String) startResponse.getBody().get("submission_id");

        // Pick the first multiple-choice question for incremental save
        Map<String, Object> firstQuestion = questions.stream()
                .filter(q -> "multiple".equals(q.get("type")))
                .findFirst()
                .orElseThrow();

        String questionSnapshotId = (String) firstQuestion.get("id");
        List<Map<String, Object>> options =
                (List<Map<String, Object>>) firstQuestion.get("options");
        String firstOptionId = (String) options.get(0).get("id");

        // Save answer incrementally
        Map<String, Object> saveBody = Map.of(
                "type", "multiple",
                "selected_option_ids", List.of(firstOptionId));

        ResponseEntity<Void> saveResponse = rest.exchange(
                "/submissions/{sid}/answers/{qid}", HttpMethod.PUT,
                withAuth(saveBody), Void.class,
                retakeSubmissionId, questionSnapshotId);

        assertThat(saveResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Now submit the full assessment with the same answer
        List<Map<String, Object>> answers = questions.stream()
                .map(q -> {
                    String type = (String) q.get("type");
                    List<Map<String, Object>> opts =
                            (List<Map<String, Object>>) q.get("options");

                    if ("multiple".equals(type) && opts != null && !opts.isEmpty()) {
                        return Map.<String, Object>of(
                                "question_id", q.get("id"),
                                "type", "multiple",
                                "selected_option_ids", List.of(opts.get(0).get("id")));
                    } else {
                        return Map.<String, Object>of(
                                "question_id", q.get("id"),
                                "type", "open",
                                "text", "Integration test answer");
                    }
                })
                .toList();

        ResponseEntity<Map> submitResponse = rest.exchange(
                "/submissions/{id}", HttpMethod.PUT,
                withAuth(Map.of("answers", answers)), Map.class, retakeSubmissionId);

        // Must not fail with 500 (duplicate key) — this was the original bug
        assertThat(submitResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(submitResponse.getBody()).containsKey("answers");

        List<Map<String, Object>> answerResults =
                (List<Map<String, Object>>) submitResponse.getBody().get("answers");
        assertThat(answerResults).hasSameSizeAs(questions);
    }

    // ── 9. Verify security ─────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("GET /assessments without token → 403")
    void unauthorizedAccess() {
        ResponseEntity<Map> response = rest.getForEntity(
                "/assessments", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ── 10. Publishing is not a student action ─────────────────────

    @Test
    @Order(10)
    @DisplayName("POST /assessments/{id}/publish as a student → 403")
    void studentCannotPublish() {
        ResponseEntity<Map> response = rest.exchange(
                "/assessments/{id}/publish", HttpMethod.POST,
                withAuth(null), Map.class, assessmentId);

        assertThat(response.getStatusCode())
                .as("a student must never be able to publish an assessment")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(11)
    @DisplayName("POST /assessments/{id}/publish: a teacher publishes a template they own → 201")
    void teacherPublishesOwnTemplate() {
        UUID teacherId = jdbc.queryForObject(
                "SELECT id FROM app_user WHERE username = ?", UUID.class, "teacher");
        UUID templateId = insertTemplateOwnedBy(teacherId);

        assertThat(publishAs("teacher", templateId))
                .as("a teacher must be able to publish their own template")
                .isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @Order(12)
    @DisplayName("POST /assessments/{id}/publish: a teacher may not publish platform content → 403")
    void teacherCannotPublishPlatformContent() {
        // The seeded Python Certification is platform content (owner_id IS NULL); only an
        // admin manages it. A teacher must not be able to publish it.
        UUID platformTemplateId = jdbc.queryForObject(
                "SELECT id FROM assessment_template WHERE title = ?",
                UUID.class, "Python Certification Exam Practice");

        assertThat(publishAs("teacher", platformTemplateId))
                .as("platform content is admin-only")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(13)
    @DisplayName("POST /assessments/{id}/publish: an admin may publish platform content → 201")
    void adminPublishesPlatformContent() {
        UUID platformTemplateId = jdbc.queryForObject(
                "SELECT id FROM assessment_template WHERE title = ?",
                UUID.class, "Python Certification Exam Practice");

        assertThat(publishAs("admin", platformTemplateId))
                .as("an admin may publish any template, including platform content")
                .isEqualTo(HttpStatus.CREATED);
    }

    // ── Review covers the drawn paper only ──────────────────────────

    @Test
    @Order(15)
    @DisplayName("GET /submissions/{id}/review → only the questions the submission drew")
    void reviewCoversDrawnQuestionsOnly() {
        ResponseEntity<Map> response = rest.exchange(
                "/submissions/{id}/review", HttpMethod.GET,
                withAuth(null), Map.class, submissionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<Map<String, Object>> reviewQuestions =
                (List<Map<String, Object>>) response.getBody().get("questions");

        // The snapshot holds the whole pool; a submission draws questionsPerAssessment
        // of it. The review must not show questions the student never saw.
        assertThat(reviewQuestions).hasSameSizeAs(questions);
        assertThat(reviewQuestions.stream().map(q -> q.get("id")).toList())
                .containsExactlyInAnyOrderElementsOf(
                        questions.stream().map(q -> q.get("id")).toList());
    }

    // ── Reproducible draw ───────────────────────────────────────────

    @Test
    @Order(14)
    @DisplayName("questions are frozen per submission — two fetches return the identical paper")
    void questionsAreFrozenPerSubmission() {
        // Ensure an in-progress submission exists (idempotent — reuses the one from start).
        rest.exchange("/assessments/{id}/start", HttpMethod.POST,
                withAuth(null), Map.class, assessmentId);

        String first = paperSignature();
        String second = paperSignature();

        // Before the fix the draw was re-randomised on every request; now the submission's
        // frozen seed makes both fetches identical — same questions, order and option order.
        assertThat(second).isEqualTo(first);
    }

    /** A stable fingerprint of the current questions payload: question ids in order, each
     *  followed by its option ids in order. */
    @SuppressWarnings("unchecked")
    private String paperSignature() {
        ResponseEntity<Map> response = rest.exchange(
                "/assessments/{id}/questions", HttpMethod.GET,
                withAuth(null), Map.class, assessmentId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<Map<String, Object>> q =
                (List<Map<String, Object>>) response.getBody().get("questions");
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> question : q) {
            sb.append('Q').append(question.get("id"));
            List<Map<String, Object>> options =
                    (List<Map<String, Object>>) question.get("options");
            if (options != null) {
                for (Map<String, Object> opt : options) {
                    sb.append('|').append(opt.get("id"));
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    // ── Helpers ─────────────────────────────────────────────────────

    private String loginAs(String username) {
        ResponseEntity<Map> response = rest.postForEntity(
                "/auth/login", Map.of("username", username, "password", "password"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) response.getBody().get("token");
    }

    /** Publish the given template as the given user; returns the HTTP status. */
    private HttpStatus publishAs(String username, UUID templateId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(loginAs(username));
        return (HttpStatus) rest.exchange(
                "/assessments/{id}/publish", HttpMethod.POST,
                new HttpEntity<>(null, headers), Void.class, templateId).getStatusCode();
    }

    /** A minimal assessment template owned by the given user; enough to be published. */
    private UUID insertTemplateOwnedBy(UUID ownerId) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO assessment_template (
                  id, title, timer_minutes, questions_per_assessment,
                  pts_correct, pts_wrong, pts_unanswered, type,
                  shuffle_questions, shuffle_options, owner_id)
                VALUES (?, 'Owned by teacher', 30, 5, 1.00, 0.00, 0.00, 'EXAM', false, false, ?)
                """, id, ownerId);
        return id;
    }

    private HttpEntity<Object> withAuth(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
