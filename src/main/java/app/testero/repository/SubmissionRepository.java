package app.testero.repository;

import app.testero.entity.submission.Submission;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    Optional<Submission> findByIdAndUserId(UUID id, UUID userId);

    Optional<Submission> findByAssessmentIdAndUserIdAndSubmittedAtIsNull(UUID assessmentId, UUID userId);
}
