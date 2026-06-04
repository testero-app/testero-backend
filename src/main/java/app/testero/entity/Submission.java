package app.testero.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Application flow:
 *
 * - A Student starts a Test -> a Submission is created (the "blank exam sheet")
 * - For each Question in the test, the student provides a response -> an Answer is
 *   created, linked to this Submission and to the Question
 *     - If the Question is open-ended: Answer contains text (the written response)
 *     - If the Question is multiple-choice: Answer alone is not enough — the student
 *       selects one or more Options -> for each one, a record is created in
 *       AnswerSelectedOption (bridge table between Answer and Option)
 *
 * Submission (the exam)
 *   └── Answer (one response per question)
 *         ├── text/motivation          -> for open-ended questions
 *         └── AnswerSelectedOption     -> for multiple-choice questions
 *               └── Option             -> the selected option
 *
 * AnswerSelectedOption exists because a multiple-choice question can have multiple
 * correct answers (checkboxes, not just radio buttons). If it were always a single
 * option, a simple selectedOptionId on Answer would suffice.
 *
 * Scoring logic:
 *
 * The "correct" field on Option is the ground truth (defined by the teacher), while
 * "isCorrect" and "pointsAwarded" on Answer are the result computed by the system.
 *   1. The student selects Options (via AnswerSelectedOption)
 *   2. The system compares selected options against the correct Options
 *   3. The system writes on Answer: isCorrect and pointsAwarded (using ptsCorrect /
 *      ptsWrong from the Test)
 *
 * Why not just rely on Option.correct?
 *   - Scoring depends on the Test (ptsCorrect, ptsWrong) — the same question in
 *     different tests can be worth different points
 *   - For open-ended questions there are no Options -> isCorrect must be
 *     calculated/assigned manually by the teacher
 *   - Materialising the result on Answer avoids recalculating it every time and
 *     allows the teacher to override it (e.g. partial credit)
 *
 * Note: AnswerSelectedOption has neither isCorrect nor points — it is only a bridge
 * table. The overall judgement lives on Answer, which looks at the set of selected
 * options vs the correct ones.
 */
@Entity
@Table(name = "submission")
@Getter
@Setter
@NoArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    private Double score;
}
