package app.testero.entity.submission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One question of the paper drawn for a submission that has no assessment snapshot of its
 * own — a free training session, whose questions come from several published pools at once.
 *
 * <p>It references an existing {@code question_snapshot}: nothing is copied. Sessions tied to
 * a single published assessment (exams, certification simulations) do not use this table —
 * their paper is derived from the snapshot and the submission seed.
 */
@Entity
@Table(name = "submission_question")
@IdClass(SubmissionQuestionId.class)
@Getter
@Setter
@NoArgsConstructor
public class SubmissionQuestion {

    @Id
    @Column(name = "submission_id")
    private UUID submissionId;

    @Id
    @Column(name = "question_snapshot_id")
    private UUID questionSnapshotId;

    @Column(nullable = false)
    private int position;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public SubmissionQuestion(UUID submissionId, UUID questionSnapshotId, int position) {
        this.submissionId = submissionId;
        this.questionSnapshotId = questionSnapshotId;
        this.position = position;
    }
}
