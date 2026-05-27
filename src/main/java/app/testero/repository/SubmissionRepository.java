package app.testero.repository;

import app.testero.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    Optional<Submission> findByIdAndStudentId(UUID id, UUID studentId);
}
