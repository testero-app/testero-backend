package app.testero.integration;

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
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end coverage of the teacher tag vocabulary and question tagging.
 *
 * <p>Focus is the ownership/scoping rules — a teacher owns their tags and may only tag
 * questions in templates they own; students are shut out; teachers cannot touch each other's
 * vocabularies.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("integration")
@ImportTestcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class TagIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    TestRestTemplate rest;

    @Autowired
    JdbcTemplate jdbc;

    private UUID ownedQuestionId;   // a question in a template owned by "teacher"
    private UUID platformQuestionId; // a question in the platform Python cert (owner null)
    private String secondTeacher;   // username of a second teacher
    private String tagId;

    private UUID teacherId() {
        return jdbc.queryForObject("SELECT id FROM app_user WHERE username = ?", UUID.class, "teacher");
    }

    // ── Fixtures ───────────────────────────────────────────────────

    @Test
    @Order(1)
    void setUp() {
        UUID teacherId = teacherId();

        UUID templateId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO assessment_template (
                  id, title, timer_minutes, questions_per_assessment,
                  pts_correct, pts_wrong, pts_unanswered, type,
                  shuffle_questions, shuffle_options, owner_id)
                VALUES (?, 'Tag test bank', 30, 5, 1.00, 0.00, 0.00, 'EXAM', false, false, ?)
                """, templateId, teacherId);

        ownedQuestionId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO question_template (id, assessment_template_id, type, text, position)
                VALUES (?, ?, 'multiple', 'Owned question', 1)
                """, ownedQuestionId, templateId);

        platformQuestionId = jdbc.queryForObject("""
                SELECT qt.id FROM question_template qt
                  JOIN assessment_template at ON at.id = qt.assessment_template_id
                 WHERE at.title = 'Python Certification Exam Practice'
                 LIMIT 1
                """, UUID.class);

        // A second teacher, to prove tags are private per teacher.
        UUID t2 = UUID.randomUUID();
        secondTeacher = "teacher2_" + t2.toString().substring(0, 8);
        jdbc.update("INSERT INTO app_user (id, username, password_hash, must_change_password) VALUES (?, ?, "
                + "'$2b$12$cgovw6Y3PVXIvRX2LKnwj.rvvCPY4ffHjZGyzCc0HcLjb/a25WOIm', false)", t2, secondTeacher);
        jdbc.update("INSERT INTO app_user_role (user_id, role_id) "
                + "SELECT ?, id FROM app_role WHERE name = 'TEACHER'", t2);
    }

    // ── Happy path ─────────────────────────────────────────────────

    @Test
    @Order(2)
    void teacherCreatesTag() {
        ResponseEntity<Map> res = rest.exchange("/tags", HttpMethod.POST,
                auth("teacher", Map.of("name", "Ricorsione")), Map.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        tagId = (String) res.getBody().get("id");
        assertThat(tagId).isNotBlank();
        assertThat(res.getBody().get("name")).isEqualTo("Ricorsione");
    }

    @Test
    @Order(3)
    void teacherTagsOwnQuestionAndFiltersByIt() {
        ResponseEntity<Void> attach = rest.exchange(
                "/questions/{q}/tags/{t}", HttpMethod.POST,
                auth("teacher", null), Void.class, ownedQuestionId, tagId);
        assertThat(attach.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<List> filtered = rest.exchange(
                "/questions?tagId={t}", HttpMethod.GET,
                auth("teacher", null), List.class, tagId);
        assertThat(filtered.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(filtered.getBody()).hasSize(1);
        Map<String, Object> q = (Map<String, Object>) filtered.getBody().get(0);
        assertThat(q.get("id")).isEqualTo(ownedQuestionId.toString());
        List<Map<String, Object>> tags = (List<Map<String, Object>>) q.get("tags");
        assertThat(tags).extracting(t -> t.get("name")).containsExactly("Ricorsione");
    }

    // ── Rules ──────────────────────────────────────────────────────

    @Test
    @Order(4)
    void duplicateTagNameIsRejected() {
        ResponseEntity<Map> res = rest.exchange("/tags", HttpMethod.POST,
                auth("teacher", Map.of("name", "Ricorsione")), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(5)
    void studentCannotCreateTags() {
        ResponseEntity<Map> res = rest.exchange("/tags", HttpMethod.POST,
                auth("a.rossi", Map.of("name", "Sneaky")), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(6)
    void teacherCannotTagAQuestionInATemplateTheyDoNotOwn() {
        // platformQuestionId belongs to the Python cert (owner null) — admin-only.
        ResponseEntity<Map> res = rest.exchange(
                "/questions/{q}/tags/{t}", HttpMethod.POST,
                auth("teacher", null), Map.class, platformQuestionId, tagId);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(7)
    void tagsArePrivate_anotherTeacherCannotTouchThem() {
        // Second teacher does not see teacher-1's tag in their own list...
        ResponseEntity<List> ownList = rest.exchange("/tags", HttpMethod.GET,
                auth(secondTeacher, null), List.class);
        assertThat(ownList.getBody()).isEmpty();

        // ...and cannot rename or delete it.
        ResponseEntity<Map> rename = rest.exchange("/tags/{t}", HttpMethod.PATCH,
                auth(secondTeacher, Map.of("name", "Hijack")), Map.class, tagId);
        assertThat(rename.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<Void> delete = rest.exchange("/tags/{t}", HttpMethod.DELETE,
                auth(secondTeacher, null), Void.class, tagId);
        assertThat(delete.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(8)
    void ownerCanDeleteTag_whichCascadesTheLink() {
        ResponseEntity<Void> delete = rest.exchange("/tags/{t}", HttpMethod.DELETE,
                auth("teacher", null), Void.class, tagId);
        assertThat(delete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Integer links = jdbc.queryForObject(
                "SELECT count(*) FROM question_tag WHERE tag_id = ?::uuid", Integer.class, tagId);
        assertThat(links).as("question_tag rows cascade when the tag is deleted").isZero();
    }

    // ── Helpers ────────────────────────────────────────────────────

    private HttpEntity<Object> auth(String username, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login(username));
        return new HttpEntity<>(body, headers);
    }

    private String login(String username) {
        ResponseEntity<Map> res = rest.postForEntity(
                "/auth/login", Map.of("username", username, "password", "password"), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) res.getBody().get("token");
    }
}
