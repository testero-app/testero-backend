package app.testero.repository.submission;

import app.testero.entity.submission.SubmissionQuestion;
import app.testero.entity.submission.SubmissionQuestionId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionQuestionRepository
        extends JpaRepository<SubmissionQuestion, SubmissionQuestionId> {

    List<SubmissionQuestion> findBySubmissionIdOrderByPositionAsc(UUID submissionId);

    boolean existsBySubmissionIdAndQuestionSnapshotId(UUID submissionId, UUID questionSnapshotId);
}
