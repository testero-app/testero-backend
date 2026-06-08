package app.testero.repository;

import app.testero.entity.submission.UserAnswer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, UUID> {

    List<UserAnswer> findBySubmissionId(UUID submissionId);
}
