package app.testero.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The attempt limit ({@code max_attempts}) frozen into a snapshot is enforced at start.
 *
 * <p>Covers: the limit blocks a further attempt; {@code null} means unlimited; resuming an
 * in-progress attempt does not consume a new one; and publishing copies the limit from the
 * template into the snapshot.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("integration")
@ImportTestcontainers
@SuppressWarnings("rawtypes")
class MaxAttemptsIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    TestRestTemplate rest;

    @Autowired
    JdbcTemplate jdbc;

    private static final String STUDENT = "a.rossi";

    @Test
    @DisplayName("a further attempt beyond max_attempts is rejected with 409")
    void attemptLimitBlocksBeyondMax() {
        UUID snapshot = insertSnapshot(1);          // one attempt allowed
        insertFinishedSubmission(snapshot, userId(STUDENT));  // ...already used

        ResponseEntity<Map> res = start(STUDENT, snapshot);

        assertThat(res.getStatusCode())
                .as("the attempt limit is reached, so a new start must be refused")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("max_attempts = null means unlimited")
    void nullMaxAttemptsIsUnlimited() {
        UUID snapshot = insertSnapshot(null);
        insertFinishedSubmission(snapshot, userId(STUDENT));

        ResponseEntity<Map> res = start(STUDENT, snapshot);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("resuming an in-progress attempt does not consume a new one")
    void resumingInProgressDoesNotCountAsANewAttempt() {
        UUID snapshot = insertSnapshot(1);

        ResponseEntity<Map> first = start(STUDENT, snapshot);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String submissionId = (String) first.getBody().get("submission_id");

        // Starting again returns the SAME in-progress attempt, not a 409.
        ResponseEntity<Map> again = start(STUDENT, snapshot);
        assertThat(again.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(again.getBody().get("submission_id")).isEqualTo(submissionId);
    }

    @Test
    @DisplayName("publishing copies max_attempts from the template into the snapshot")
    void publishFreezesMaxAttempts() {
        UUID teacherId = userId("teacher");
        UUID templateId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO assessment_template (
                  id, title, timer_minutes, questions_per_assessment,
                  pts_correct, pts_wrong, pts_unanswered, type,
                  shuffle_questions, shuffle_options, max_attempts, owner_id)
                VALUES (?, 'Max attempts publish', 30, 5, 1.00, 0.00, 0.00, 'EXAM', false, false, 2, ?)
                """, templateId, teacherId);

        ResponseEntity<Void> publish = rest.exchange(
                "/assessments/{id}/publish", HttpMethod.POST,
                bearer("teacher"), Void.class, templateId);
        assertThat(publish.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Integer snapshotMax = jdbc.queryForObject(
                "SELECT max_attempts FROM assessment_snapshot WHERE assessment_template_id = ?",
                Integer.class, templateId);
        assertThat(snapshotMax).as("the limit is frozen into the snapshot at publish").isEqualTo(2);
    }

    // ── Helpers ────────────────────────────────────────────────────

    private ResponseEntity<Map> start(String username, UUID snapshotId) {
        return rest.exchange("/assessments/{id}/start", HttpMethod.POST,
                bearer(username), Map.class, snapshotId);
    }

    private UUID insertSnapshot(Integer maxAttempts) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO assessment_snapshot (
                  id, content_hash, title, timer_minutes, questions_per_assessment,
                  pts_correct, pts_wrong, type, max_attempts, published_at)
                VALUES (?, ?, 'Attempt-limited', 30, 5, 1.00, 0.00, 'EXAM', ?, NOW())
                """, id, "hash-" + id, maxAttempts);
        return id;
    }

    private void insertFinishedSubmission(UUID snapshotId, UUID userId) {
        jdbc.update("""
                INSERT INTO submission (id, user_id, assessment_snapshot_id, status, started_at, submitted_at)
                VALUES (?, ?, ?, 'SUBMITTED', NOW(), NOW())
                """, UUID.randomUUID(), userId, snapshotId);
    }

    private UUID userId(String username) {
        return jdbc.queryForObject("SELECT id FROM app_user WHERE username = ?", UUID.class, username);
    }

    private HttpEntity<Object> bearer(String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login(username));
        return new HttpEntity<>(null, headers);
    }

    private String login(String username) {
        ResponseEntity<Map> res = rest.postForEntity(
                "/auth/login", Map.of("username", username, "password", "password"), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) res.getBody().get("token");
    }
}
