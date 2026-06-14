package app.testero.fixture;

import app.testero.entity.assessment.Assessment;
import app.testero.entity.assessment.Difficulty;
import app.testero.entity.assessment.Option;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.snapshot.OptionSnapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Test fixture with scoring rules taken from the production seed
 * "Python Certification Exam Practice 1" (ptsCorrect=1.00, ptsWrong=-0.25).
 *
 * Question/option texts are intentionally generic ("Question 1", "Option Q1-3-correct")
 * to decouple tests from real exam content while keeping the same scoring structure.
 */
public final class PythonCertificationFixture {

    private PythonCertificationFixture() {}

    // ── Test configuration (from prod seed) ────────────────────────
    public static final UUID TEST_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");
    public static final UUID SNAPSHOT_ID = UUID.fromString("aa000000-0000-0000-0000-000000000099");
    public static final String TITLE = "Grading Test Fixture";
    public static final LocalDate DATE = LocalDate.of(2026, 6, 15);
    public static final int TIMER_MINUTES = 45;
    public static final int TOTAL_POOL = 5;
    public static final int QUESTIONS_PER_ASSESSMENT = 5;
    public static final BigDecimal PTS_CORRECT = new BigDecimal("1.00");
    public static final BigDecimal PTS_WRONG = new BigDecimal("-0.25");
    public static final Difficulty DIFFICULTY = Difficulty.INTERMEDIATE;
    public static final BigDecimal PASSING_SCORE = new BigDecimal("3.00");

    // ── Student ────────────────────────────────────────────────────
    public static final UUID STUDENT_ID = UUID.fromString("bb000000-0000-0000-0000-000000000001");

    // ── Question 1 — correct: opt C ───────────────────────────────
    public static final UUID Q1_ID = UUID.fromString("cc000000-0000-0000-0000-000000000001");
    public static final UUID Q1_OPT_A = UUID.fromString("dd000000-0000-0000-0000-0000000001a1");
    public static final UUID Q1_OPT_B = UUID.fromString("dd000000-0000-0000-0000-0000000001b1");
    public static final UUID Q1_OPT_C = UUID.fromString("dd000000-0000-0000-0000-0000000001c1"); // correct
    public static final UUID Q1_OPT_D = UUID.fromString("dd000000-0000-0000-0000-0000000001d1");

    // ── Question 2 — correct: opt D ───────────────────────────────
    public static final UUID Q2_ID = UUID.fromString("cc000000-0000-0000-0000-000000000002");
    public static final UUID Q2_OPT_A = UUID.fromString("dd000000-0000-0000-0000-0000000002a1");
    public static final UUID Q2_OPT_B = UUID.fromString("dd000000-0000-0000-0000-0000000002b1");
    public static final UUID Q2_OPT_C = UUID.fromString("dd000000-0000-0000-0000-0000000002c1");
    public static final UUID Q2_OPT_D = UUID.fromString("dd000000-0000-0000-0000-0000000002d1"); // correct

    // ── Question 3 — correct: opt B ───────────────────────────────
    public static final UUID Q3_ID = UUID.fromString("cc000000-0000-0000-0000-000000000003");
    public static final UUID Q3_OPT_A = UUID.fromString("dd000000-0000-0000-0000-0000000003a1");
    public static final UUID Q3_OPT_B = UUID.fromString("dd000000-0000-0000-0000-0000000003b1"); // correct
    public static final UUID Q3_OPT_C = UUID.fromString("dd000000-0000-0000-0000-0000000003c1");
    public static final UUID Q3_OPT_D = UUID.fromString("dd000000-0000-0000-0000-0000000003d1");

    // ── Question 4 — correct: opt A ───────────────────────────────
    public static final UUID Q4_ID = UUID.fromString("cc000000-0000-0000-0000-000000000004");
    public static final UUID Q4_OPT_A = UUID.fromString("dd000000-0000-0000-0000-0000000004a1"); // correct
    public static final UUID Q4_OPT_B = UUID.fromString("dd000000-0000-0000-0000-0000000004b1");
    public static final UUID Q4_OPT_C = UUID.fromString("dd000000-0000-0000-0000-0000000004c1");
    public static final UUID Q4_OPT_D = UUID.fromString("dd000000-0000-0000-0000-0000000004d1");

    // ── Question 5 — correct: opt A ───────────────────────────────
    public static final UUID Q5_ID = UUID.fromString("cc000000-0000-0000-0000-000000000005");
    public static final UUID Q5_OPT_A = UUID.fromString("dd000000-0000-0000-0000-0000000005a1"); // correct
    public static final UUID Q5_OPT_B = UUID.fromString("dd000000-0000-0000-0000-0000000005b1");
    public static final UUID Q5_OPT_C = UUID.fromString("dd000000-0000-0000-0000-0000000005c1");
    public static final UUID Q5_OPT_D = UUID.fromString("dd000000-0000-0000-0000-0000000005d1");

    // ── Open question (synthetic) ─────────────────────────────────
    public static final UUID Q_OPEN_ID = UUID.fromString("cc000000-0000-0000-0000-00000000ff01");

    // ── Fallback/Nessuna option for Q1 (synthetic) ────────────────
    public static final UUID Q1_OPT_FALLBACK = UUID.fromString("dd000000-0000-0000-0000-0000000001f1");

    // ── Factory methods ────────────────────────────────────────────

    /** Build the assessment entity with default scoring (1.00 / -0.25). */
    public static Assessment buildAssessment() {
        return buildAssessment(PTS_CORRECT, PTS_WRONG);
    }

    /** Build the assessment entity with custom scoring. */
    public static Assessment buildAssessment(BigDecimal ptsCorrect, BigDecimal ptsWrong) {
        Assessment assessment = new Assessment();
        assessment.setId(TEST_ID);
        assessment.setTitle(TITLE);
        assessment.setDate(DATE);
        assessment.setTimerMinutes(TIMER_MINUTES);
        assessment.setTotalPool(TOTAL_POOL);
        assessment.setQuestionsPerAssessment(QUESTIONS_PER_ASSESSMENT);
        assessment.setPtsCorrect(ptsCorrect);
        assessment.setPtsWrong(ptsWrong);
        assessment.setDifficulty(DIFFICULTY);
        assessment.setPassingScore(PASSING_SCORE);
        return assessment;
    }

    /** Build the assessment snapshot entity with default scoring (1.00 / -0.25). */
    public static AssessmentSnapshot buildAssessmentSnapshot() {
        return buildAssessmentSnapshot(PTS_CORRECT, PTS_WRONG);
    }

    /** Build the assessment snapshot entity with custom scoring. */
    public static AssessmentSnapshot buildAssessmentSnapshot(BigDecimal ptsCorrect, BigDecimal ptsWrong) {
        AssessmentSnapshot snapshot = new AssessmentSnapshot();
        snapshot.setId(SNAPSHOT_ID);
        snapshot.setAssessmentId(TEST_ID);
        snapshot.setContentHash("fixture-hash");
        snapshot.setVersion(1);
        snapshot.setTitle(TITLE);
        snapshot.setTimerMinutes(TIMER_MINUTES);
        snapshot.setQuestionsPerAssessment(QUESTIONS_PER_ASSESSMENT);
        snapshot.setPtsCorrect(ptsCorrect);
        snapshot.setPtsWrong(ptsWrong);
        snapshot.setDifficulty(DIFFICULTY);
        snapshot.setPassingScore(PASSING_SCORE);
        snapshot.setPublishedAt(LocalDateTime.of(2026, 6, 15, 0, 0));
        return snapshot;
    }

    /** Correct options for Q1 through Q5 (one correct each). */
    public static List<Option> allCorrectOptions() {
        return List.of(
                buildOption(Q1_OPT_C, Q1_ID, "Option Q1-3-correct", true, false, 3),
                buildOption(Q2_OPT_D, Q2_ID, "Option Q2-4-correct", true, false, 4),
                buildOption(Q3_OPT_B, Q3_ID, "Option Q3-2-correct", true, false, 2),
                buildOption(Q4_OPT_A, Q4_ID, "Option Q4-1-correct", true, false, 1),
                buildOption(Q5_OPT_A, Q5_ID, "Option Q5-1-correct", true, false, 1)
        );
    }

    /** Correct options for a specific subset of questions. */
    public static List<Option> correctOptionsFor(UUID... questionIds) {
        List<UUID> ids = List.of(questionIds);
        return allCorrectOptions().stream()
                .filter(opt -> ids.contains(opt.getQuestionId()))
                .toList();
    }

    /** Correct option snapshots for Q1 through Q5 (one correct each). */
    public static List<OptionSnapshot> allCorrectOptionSnapshots() {
        return List.of(
                buildOptionSnapshot(Q1_OPT_C, Q1_ID, "Option Q1-3-correct", true, 3),
                buildOptionSnapshot(Q2_OPT_D, Q2_ID, "Option Q2-4-correct", true, 4),
                buildOptionSnapshot(Q3_OPT_B, Q3_ID, "Option Q3-2-correct", true, 2),
                buildOptionSnapshot(Q4_OPT_A, Q4_ID, "Option Q4-1-correct", true, 1),
                buildOptionSnapshot(Q5_OPT_A, Q5_ID, "Option Q5-1-correct", true, 1)
        );
    }

    /** Correct option snapshots for a specific subset of question snapshot IDs. */
    public static List<OptionSnapshot> correctOptionSnapshotsFor(UUID... questionSnapshotIds) {
        List<UUID> ids = List.of(questionSnapshotIds);
        return allCorrectOptionSnapshots().stream()
                .filter(opt -> ids.contains(opt.getQuestionSnapshotId()))
                .toList();
    }

    /** Build a single option entity. */
    public static Option buildOption(UUID id, UUID questionId, String text,
                                     boolean correct, boolean fallback, int position) {
        Option opt = new Option();
        opt.setId(id);
        opt.setQuestionId(questionId);
        opt.setText(text);
        opt.setCorrect(correct);
        opt.setFallback(fallback);
        opt.setPosition(position);
        return opt;
    }

    /** Build a single option snapshot entity. */
    public static OptionSnapshot buildOptionSnapshot(UUID id, UUID questionSnapshotId,
                                                      String text, boolean correct, int position) {
        OptionSnapshot opt = new OptionSnapshot();
        opt.setId(id);
        opt.setQuestionSnapshotId(questionSnapshotId);
        opt.setText(text);
        opt.setCorrect(correct);
        opt.setPosition(position);
        return opt;
    }

    /** Build a fallback "Nessuna" option for Q1. */
    public static Option buildFallbackOption(boolean correct) {
        return buildOption(Q1_OPT_FALLBACK, Q1_ID, "Option Q1-5-fallback",
                correct, true, 5);
    }

    /** Build a fallback "Nessuna" option snapshot for Q1. */
    public static OptionSnapshot buildFallbackOptionSnapshot(boolean correct) {
        OptionSnapshot opt = buildOptionSnapshot(Q1_OPT_FALLBACK, Q1_ID, "Option Q1-5-fallback",
                correct, 5);
        opt.setFallback(true);
        return opt;
    }
}
