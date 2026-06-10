package app.testero.entity.submission;

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
 * - A user starts an Assessment -> a Submission is created (the "blank exam sheet")
 * - For each Question in the assessment, the user provides a response -> a UserAnswer is
 *   created, linked to this Submission and to the Question
 *     - If the Question is open-ended: UserAnswer contains text (the written response)
 *     - If the Question is multiple-choice: UserAnswer alone is not enough — the user
 *       selects one or more Options -> for each one, a record is created in
 *       UserAnswerSelectedOption (bridge table between UserAnswer and Option)
 *
 * Submission (the exam)
 *   └── UserAnswer (one response per question)
 *         ├── text/motivation              -> for open-ended questions
 *         └── UserAnswerSelectedOption     -> for multiple-choice questions
 *               └── Option                 -> the selected option
 *
 * UserAnswerSelectedOption exists because a multiple-choice question can have multiple
 * correct answers (checkboxes, not just radio buttons). If it were always a single
 * option, a simple selectedOptionId on UserAnswer would suffice.
 *
 * Scoring logic:
 *
 * The "correct" field on Option is the ground truth (defined by the teacher), while
 * "isCorrect" and "pointsAwarded" on UserAnswer are the result computed by the system.
 *   1. The user selects Options (via UserAnswerSelectedOption)
 *   2. The system compares selected options against the correct Options
 *   3. The system writes on UserAnswer: isCorrect and pointsAwarded (using ptsCorrect /
 *      ptsWrong from the Assessment)
 *
 * Why not just rely on Option.correct?
 *   - Scoring depends on the Assessment (ptsCorrect, ptsWrong) — the same question in
 *     different assessments can be worth different points
 *   - For open-ended questions there are no Options -> isCorrect must be
 *     calculated/assigned manually by the teacher
 *   - Materialising the result on UserAnswer avoids recalculating it every time and
 *     allows the teacher to override it (e.g. partial credit)
 *
 * Note: UserAnswerSelectedOption has neither isCorrect nor points — it is only a bridge
 * table. The overall judgement lives on UserAnswer, which looks at the set of selected
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

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "test_id", nullable = false)
    private UUID assessmentId;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    private Double score;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
