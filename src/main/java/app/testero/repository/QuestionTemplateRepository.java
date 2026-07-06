package app.testero.repository;

import app.testero.entity.assessment.QuestionTemplate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionTemplateRepository extends JpaRepository<QuestionTemplate, UUID> {

    List<QuestionTemplate> findByAssessmentIdOrderByPosition(UUID assessmentId);
}
