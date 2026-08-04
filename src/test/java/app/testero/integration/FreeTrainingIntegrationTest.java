package app.testero.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Free training draws from the pools a class may practise on, referencing their questions
 * instead of copying them.
 *
 * <p>The regression this guards is the one that motivated the change: starting a session used
 * to copy every drawn question into a private snapshot — 170 rows for a 28-question session,
 * useless to anyone else. Here the row counts must not move at all.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("integration")
@ImportTestcontainers
@SuppressWarnings({"rawtypes", "unchecked"})
class FreeTrainingIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    TestRestTemplate rest;

    @Autowired
    JdbcTemplate jdbc;

    private static final String STUDENT = "a.rossi";

    @Test
    @DisplayName("starting a session copies no questions")
    void startWritesNoSnapshotRows() {
        long questionsBefore = count("question_snapshot");
        long optionsBefore = count("option_snapshot");
        long snapshotsBefore = count("assessment_snapshot");

        ResponseEntity<Map> res = start(Map.of("question_count", 10, "timer_enabled", true));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(count("question_snapshot"))
                .as("the questions are referenced, not copied")
                .isEqualTo(questionsBefore);
        assertThat(count("option_snapshot")).isEqualTo(optionsBefore);
        assertThat(count("assessment_snapshot"))
                .as("a free session belongs to no assessment, so none is created")
                .isEqualTo(snapshotsBefore);
    }

    @Test
    @DisplayName("the session has no snapshot and owns its paper")
    void sessionOwnsItsPaper() {
        ResponseEntity<Map> res = start(Map.of("question_count", 8, "timer_enabled", false));
        UUID submissionId = UUID.fromString((String) res.getBody().get("submission_id"));

        UUID snapshotId = jdbc.queryForObject(
                "SELECT assessment_snapshot_id FROM submission WHERE id = ?", UUID.class, submissionId);
        assertThat(snapshotId).as("no assessment behind a free session").isNull();

        Long drawn = jdbc.queryForObject(
                "SELECT count(*) FROM submission_question WHERE submission_id = ?",
                Long.class, submissionId);
        assertThat(drawn).as("the paper is recorded on the session itself").isEqualTo(8L);

        assertThat(res.getBody().get("timer_minutes"))
                .as("no timer was asked for").isNull();
    }

    @Test
    @DisplayName("the questions endpoint replays the paper that was drawn")
    void questionsAreReplayedInOrder() {
        ResponseEntity<Map> started = start(Map.of("question_count", 6, "timer_enabled", true));
        String submissionId = (String) started.getBody().get("submission_id");

        List<String> first = questionIds(submissionId);
        List<String> again = questionIds(submissionId);

        assertThat(first).hasSize(6);
        assertThat(again)
                .as("a reload must not reshuffle the paper")
                .containsExactlyElementsOf(first);

        List<String> stored = jdbc.queryForList(
                "SELECT question_snapshot_id FROM submission_question "
                        + "WHERE submission_id = ? ORDER BY position",
                String.class, UUID.fromString(submissionId));
        assertThat(first).containsExactlyElementsOf(stored);
    }

    @Test
    @DisplayName("two sessions with the same filters get independent draws")
    void drawsAreIndependent() {
        Map<String, Object> body = Map.of("question_count", 20, "timer_enabled", false);
        String one = (String) start(body).getBody().get("submission_id");
        String two = (String) start(body).getBody().get("submission_id");

        assertThat(one).isNotEqualTo(two);
        assertThat(questionIds(one))
                .as("each session draws for itself")
                .isNotEqualTo(questionIds(two));
    }

    @Test
    @DisplayName("exams are never drawn from, even when assigned to the class")
    void examsAreNeverPractisable() {
        UUID examSnapshot = insertExamSnapshotAssignedToClassOf(STUDENT);
        UUID examQuestion = insertQuestion(examSnapshot, "Domanda riservata all'esame");

        ResponseEntity<Map> res = start(Map.of("question_count", 100, "timer_enabled", false));
        String submissionId = (String) res.getBody().get("submission_id");

        assertThat(questionIds(submissionId))
                .as("a student must not meet the questions of a test they have yet to sit")
                .doesNotContain(examQuestion.toString());
    }

    @Test
    @DisplayName("a session with no material is refused rather than started empty")
    void noMaterialIsRefused() {
        UUID unknownTopic = UUID.randomUUID();
        ResponseEntity<Map> res = start(Map.of(
                "topic_id", unknownTopic.toString(), "question_count", 5, "timer_enabled", false));

        assertThat(res.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    // ── Helpers ────────────────────────────────────────────────────

    private ResponseEntity<Map> start(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login(STUDENT));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange("/training/start", HttpMethod.POST,
                new HttpEntity<>(body, headers), Map.class);
    }

    private List<String> questionIds(String submissionId) {
        ResponseEntity<Map> res = rest.exchange("/submissions/{id}/questions", HttpMethod.GET,
                bearer(STUDENT), Map.class, submissionId);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> questions = (List<Map<String, Object>>) res.getBody().get("questions");
        return questions.stream().map(q -> (String) q.get("id")).toList();
    }

    private UUID insertExamSnapshotAssignedToClassOf(String username) {
        UUID snapshot = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO assessment_snapshot (
                  id, content_hash, title, timer_minutes, questions_per_assessment,
                  pts_correct, pts_wrong, type, published_at)
                VALUES (?, ?, 'Verifica di aprile', 30, 5, 1.00, 0.00, 'EXAM', NOW())
                """, snapshot, "hash-" + snapshot);
        jdbc.update("""
                INSERT INTO class_assessment_assignment (class_id, assessment_snapshot_id)
                SELECT sp.class_id, ? FROM student_profile sp
                JOIN app_user u ON u.id = sp.user_id WHERE u.username = ?
                """, snapshot, username);
        return snapshot;
    }

    private UUID insertQuestion(UUID snapshotId, String text) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO question_snapshot (
                  id, assessment_snapshot_id, type, text, position, points)
                VALUES (?, ?, 'multiple', ?, 0, 1.00)
                """, id, snapshotId, text);
        return id;
    }

    private long count(String table) {
        return jdbc.queryForObject("SELECT count(*) FROM " + table, Long.class);
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
