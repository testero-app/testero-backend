package app.testero.repository;

import app.testero.entity.assessment.Question;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByAssessmentIdOrderByPosition(UUID assessmentId);
}
