package app.testero.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the state the Liquibase seeds leave the database in, under the dev context.
 *
 * <p>The "Programming Basics — Demo" assessment seeded by v1.0 → v1.7 is superseded by the
 * Python Certification content (v1.8-001) and removed by the v1.8-002 cleanup changeset.
 * The cleanup deletes data rather than editing the original seed files, because those belong
 * to changesets that are already applied and whose checksums must stay valid.
 */
@SpringBootTest
@ActiveProfiles("integration")
@ImportTestcontainers
class SeedDataIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    JdbcTemplate jdbc;

    private static final String PYTHON_CERT = "Python Certification Exam Practice";
    private static final String LEGACY_DEMO = "Programming Basics — Demo";

    // ── Product content (v1.8-001) ─────────────────────────────────

    @Test
    @DisplayName("Python Certification content is seeded with its questions, options and subjects")
    void pythonCertificationContentIsSeeded() {
        assertThat(count("assessment_template WHERE title = ?", PYTHON_CERT))
                .as("Python Certification assessment template").isEqualTo(1);
        assertThat(count("question_template")).as("questions").isEqualTo(67);
        assertThat(count("option_template")).as("options").isEqualTo(268);
        assertThat(count("subject")).as("subjects").isEqualTo(23);
        assertThat(count("topic")).as("topic hierarchy levels").isEqualTo(3);
    }

    @Test
    @DisplayName("every seeded question is linked to a subject and belongs to the Python assessment")
    void seededQuestionsAreWellFormed() {
        assertThat(count("question_template qt JOIN assessment_template a "
                + "ON a.id = qt.assessment_template_id WHERE a.title = ?", PYTHON_CERT))
                .as("questions attached to the Python assessment").isEqualTo(67);

        assertThat(count("question_template qt WHERE NOT EXISTS ("
                + "SELECT 1 FROM question_template_subject qts "
                + "WHERE qts.question_template_id = qt.id)"))
                .as("questions with no subject link").isZero();

        assertThat(count("question_template qt WHERE NOT EXISTS ("
                + "SELECT 1 FROM option_template o "
                + "WHERE o.question_template_id = qt.id AND o.is_correct)"))
                .as("questions with no correct option").isZero();
    }

    // ── Legacy demo cleanup (v1.8-002) ─────────────────────────────

    @Test
    @DisplayName("the legacy demo assessment is removed, leaving no orphaned rows behind")
    void legacyDemoDataIsRemoved() {
        assertThat(count("assessment_template WHERE title = ?", LEGACY_DEMO))
                .as("legacy demo assessment").isZero();
        assertThat(count("topic WHERE title = 'Programming Basics'"))
                .as("legacy demo topic").isZero();
        assertThat(count("subject WHERE label = 'Programming'"))
                .as("legacy demo subject").isZero();
        assertThat(count("submission")).as("legacy demo submissions").isZero();
        assertThat(count("assessment_snapshot WHERE title = ?", LEGACY_DEMO))
                .as("legacy demo snapshot").isZero();

        assertThat(count("question_template qt LEFT JOIN assessment_template a "
                + "ON a.id = qt.assessment_template_id WHERE a.id IS NULL"))
                .as("questions orphaned by the cleanup").isZero();
        assertThat(count("option_template o LEFT JOIN question_template qt "
                + "ON qt.id = o.question_template_id WHERE qt.id IS NULL"))
                .as("options orphaned by the cleanup").isZero();
        assertThat(count("question_snapshot qs LEFT JOIN assessment_snapshot s "
                + "ON s.id = qs.assessment_snapshot_id WHERE s.id IS NULL"))
                .as("question snapshots orphaned by the cleanup").isZero();
    }

    // ── Dev publish (v1.8-003) ─────────────────────────────────────

    @Test
    @DisplayName("the Python assessment is published and assigned, so a student can sit it")
    void pythonCertificationIsPublishedToTheDemoClass() {
        assertThat(count("assessment_snapshot WHERE title = ?", PYTHON_CERT))
                .as("published snapshot").isEqualTo(1);
        assertThat(count("question_snapshot")).as("snapshotted questions").isEqualTo(67);
        assertThat(count("option_snapshot")).as("snapshotted options").isEqualTo(268);

        assertThat(count("class_assessment_assignment ca "
                + "JOIN assessment_snapshot s ON s.id = ca.assessment_snapshot_id "
                + "JOIN user_class c ON c.id = ca.class_id "
                + "WHERE s.title = ? AND c.name = 'Demo-2026'", PYTHON_CERT))
                .as("assignment of the Python assessment to the demo class").isEqualTo(1);

        assertThat(count("question_snapshot qs WHERE NOT EXISTS ("
                + "SELECT 1 FROM option_snapshot o "
                + "WHERE o.question_snapshot_id = qs.id AND o.is_correct)"))
                .as("snapshotted questions with no correct option").isZero();
    }

    // ── Dev fixtures survive the cleanup ───────────────────────────

    @Test
    @DisplayName("the dev users and classes are left intact by the cleanup")
    void devFixturesSurvive() {
        assertThat(count("app_user")).as("seeded dev users").isPositive();
        assertThat(count("user_class")).as("seeded dev classes").isPositive();
    }

    private int count(String fromClause, Object... args) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM " + fromClause, Integer.class, args);
        return n == null ? 0 : n;
    }
}
