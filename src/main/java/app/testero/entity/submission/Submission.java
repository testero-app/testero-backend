package app.testero.entity.submission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * A user starts an Assessment via a published snapshot -> a Submission is created.
 *
 * All references point to snapshot tables (immutable, frozen at publish time):
 *   - Submission -> AssessmentSnapshot
 *   - UserAnswer -> QuestionSnapshot
 *   - UserAnswerSelectedOption -> OptionSnapshot
 *
 * Submission (the exam)
 *   └── UserAnswer (one response per question snapshot)
 *         ├── text/motivation              -> for open-ended questions
 *         └── UserAnswerSelectedOption     -> for multiple-choice questions
 *               └── OptionSnapshot         -> the selected option (frozen)
 *
 * Scoring uses snapshot data (ptsCorrect/ptsWrong from the snapshot, correct flags
 * from OptionSnapshot). Results are materialized on UserAnswer (isCorrect, pointsAwarded).
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

    /**
     * The published assessment being sat, or {@code null} for a free training session — whose
     * paper spans several pools and therefore belongs to no single snapshot. When null, the
     * questions drawn are listed in {@code submission_question}.
     */
    @Column(name = "assessment_snapshot_id")
    private UUID assessmentSnapshotId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubmissionStatus status = SubmissionStatus.IN_PROGRESS;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /**
     * Randomisation seed, fixed once when the submission starts. Makes the question draw
     * (subset, question order, option order) reproducible: every fetch of the questions for
     * this submission replays the identical paper, so a reload or resume never reshuffles.
     */
    @Column(name = "seed", nullable = false)
    private long seed;

    /** Only for sessions with no snapshot: a free training session owns its countdown. */
    @Column(name = "timer_minutes")
    private Integer timerMinutes;

    private Double score;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
