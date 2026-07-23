package app.testero.integration;

import app.testero.scheduling.NotificationScheduler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the deadline-reminder scheduler against a real database, so the derived queries
 * and JPQL (which the unit test mocks away) are actually run. Uses the dev seed: class
 * "Demo-2026" with its students and one published assessment.
 *
 * <p>All ids in the seed are {@code gen_random_uuid()}, so they are resolved at runtime by
 * looking them up, never hard-coded. Deadlines are set from a JVM-side {@link LocalDateTime},
 * matching the scheduler's own clock, so a UTC-vs-local mismatch on the
 * {@code timestamp without time zone} column cannot make the window comparison flaky.
 */
@SpringBootTest
@ActiveProfiles("integration")
@ImportTestcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeadlineReminderIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    NotificationScheduler scheduler;

    @Autowired
    JdbcTemplate jdbc;

    private String snapshotId;
    private String classId;
    private String rossiId;

    @BeforeAll
    void resolveSeedIds() {
        snapshotId = jdbc.queryForObject(
                "SELECT caa.assessment_snapshot_id::text FROM class_assessment_assignment caa "
                        + "JOIN assessment_snapshot s ON s.id = caa.assessment_snapshot_id "
                        + "WHERE s.title = 'Python Certification Exam Practice' LIMIT 1", String.class);
        classId = jdbc.queryForObject(
                "SELECT id::text FROM user_class WHERE name = 'Demo-2026'", String.class);
        rossiId = jdbc.queryForObject(
                "SELECT id::text FROM app_user WHERE username = 'a.rossi'", String.class);
    }

    private void setDeadline(LocalDateTime when) {
        jdbc.update("UPDATE class_assessment_assignment SET available_until = ? "
                + "WHERE assessment_snapshot_id = ?::uuid", Timestamp.valueOf(when), snapshotId);
    }

    private void resetState() {
        jdbc.update("DELETE FROM deadline_reminder_sent WHERE assessment_snapshot_id = ?::uuid", snapshotId);
        jdbc.update("DELETE FROM notification WHERE event = 'DEADLINE_REMINDER'");
        jdbc.update("DELETE FROM submission WHERE assessment_snapshot_id = ?::uuid", snapshotId);
    }

    private long deadlineReminders(String userId) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM notification WHERE user_id = ?::uuid AND event = 'DEADLINE_REMINDER'",
                Long.class, userId);
    }

    private long markers() {
        return jdbc.queryForObject(
                "SELECT count(*) FROM deadline_reminder_sent WHERE assessment_snapshot_id = ?::uuid",
                Long.class, snapshotId);
    }

    private long studentsInClass() {
        return jdbc.queryForObject(
                "SELECT count(*) FROM student_profile WHERE class_id = ?::uuid", Long.class, classId);
    }

    @Test
    @Order(1)
    @DisplayName("reminds every non-submitting student once, and never twice")
    void remindsOnce() {
        resetState();
        setDeadline(LocalDateTime.now().plusHours(2));
        long students = studentsInClass();
        assertThat(students).isGreaterThan(0);

        scheduler.sendDeadlineReminders();

        assertThat(deadlineReminders(rossiId)).isEqualTo(1);
        assertThat(markers()).isEqualTo(students);

        // Running again must not remind anyone a second time.
        scheduler.sendDeadlineReminders();

        assertThat(deadlineReminders(rossiId)).isEqualTo(1);
        assertThat(markers()).isEqualTo(students);
    }

    @Test
    @Order(2)
    @DisplayName("does not remind a student who has already submitted")
    void skipsSubmitter() {
        resetState();
        setDeadline(LocalDateTime.now().plusHours(2));
        // Give rossi a submission for this snapshot.
        jdbc.update("INSERT INTO submission (id, user_id, assessment_snapshot_id, status, started_at) "
                + "VALUES (gen_random_uuid(), ?::uuid, ?::uuid, 'SUBMITTED', now())",
                rossiId, snapshotId);

        scheduler.sendDeadlineReminders();

        assertThat(deadlineReminders(rossiId)).isZero();
        // The other students still get reminded.
        assertThat(markers()).isEqualTo(studentsInClass() - 1);
    }

    @Test
    @Order(3)
    @DisplayName("does not remind when the deadline is outside the window")
    void noReminderOutsideWindow() {
        resetState();
        setDeadline(LocalDateTime.now().plusDays(3));

        scheduler.sendDeadlineReminders();

        assertThat(deadlineReminders(rossiId)).isZero();
        assertThat(markers()).isZero();
    }

    @Test
    @Order(4)
    @DisplayName("renders the reminder in the recipient's language (translate-at-send)")
    void reminderIsLocalised() {
        resetState();
        setDeadline(LocalDateTime.now().plusHours(2));
        jdbc.update("UPDATE app_user SET language = 'en' WHERE id = ?::uuid", rossiId);
        try {
            scheduler.sendDeadlineReminders();

            String title = jdbc.queryForObject(
                    "SELECT title FROM notification WHERE user_id = ?::uuid AND event = 'DEADLINE_REMINDER'",
                    String.class, rossiId);
            // English bundle: "Assessment due soon"; Italian would be "Verifica in scadenza".
            assertThat(title).isEqualTo("Assessment due soon");
        } finally {
            jdbc.update("UPDATE app_user SET language = 'it' WHERE id = ?::uuid", rossiId);
        }
    }
}
