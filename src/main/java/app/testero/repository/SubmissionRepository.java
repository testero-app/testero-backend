package app.testero.repository;

import app.testero.entity.submission.Submission;
import app.testero.entity.submission.SubmissionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    Optional<Submission> findByIdAndUserId(UUID id, UUID userId);

    Optional<Submission> findByAssessmentSnapshotIdAndUserIdAndStatus(
            UUID assessmentSnapshotId, UUID userId, SubmissionStatus status);

    List<Submission> findByUserIdAndStatusInOrderBySubmittedAtDesc(
            UUID userId, List<SubmissionStatus> statuses);

    Page<Submission> findByUserIdAndStatusInOrderBySubmittedAtDesc(
            UUID userId, List<SubmissionStatus> statuses, Pageable pageable);

    List<Submission> findByStatus(SubmissionStatus status);

    List<Submission> findByUserIdAndAssessmentSnapshotIdIn(UUID userId, List<UUID> snapshotIds);
}
