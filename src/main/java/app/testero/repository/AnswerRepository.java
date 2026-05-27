package app.testero.repository;

import app.testero.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    List<Answer> findBySubmissionId(UUID submissionId);
}
