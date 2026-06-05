package app.testero.repository;

import app.testero.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, UUID> {

    List<UserAnswer> findBySubmissionId(UUID submissionId);
}
