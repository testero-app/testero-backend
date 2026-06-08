package app.testero.fixture;

import app.testero.entity.Option;
import app.testero.entity.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    public static final String TITLE = "Grading Test Fixture";
    public static final LocalDate DATE = LocalDate.of(2026, 6, 15);
    public static final int TIMER_MINUTES = 45;
    public static final int TOTAL_POOL = 5;
    public static final int QUESTIONS_PER_TEST = 5;
    public static final BigDecimal PTS_CORRECT = new BigDecimal("1.00");
    public static final BigDecimal PTS_WRONG = new BigDecimal("-0.25");

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

    /** Build the test entity with default scoring (1.00 / -0.25). */
    public static Test buildTest() {
        return buildTest(PTS_CORRECT, PTS_WRONG);
    }

    /** Build the test entity with custom scoring. */
    public static Test buildTest(BigDecimal ptsCorrect, BigDecimal ptsWrong) {
        Test test = new Test();
        test.setId(TEST_ID);
        test.setTitle(TITLE);
        test.setDate(DATE);
        test.setTimerMinutes(TIMER_MINUTES);
        test.setTotalPool(TOTAL_POOL);
        test.setQuestionsPerTest(QUESTIONS_PER_TEST);
        test.setPtsCorrect(ptsCorrect);
        test.setPtsWrong(ptsWrong);
        return test;
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

    /** Build a fallback "Nessuna" option for Q1. */
    public static Option buildFallbackOption(boolean correct) {
        return buildOption(Q1_OPT_FALLBACK, Q1_ID, "Option Q1-5-fallback",
                correct, true, 5);
    }
}
