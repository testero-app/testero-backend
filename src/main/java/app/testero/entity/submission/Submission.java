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

    @Column(name = "assessment_snapshot_id", nullable = false)
    private UUID assessmentSnapshotId;

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
