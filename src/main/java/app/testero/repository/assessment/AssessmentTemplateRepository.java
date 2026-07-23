package app.testero.repository.assessment;

import app.testero.entity.assessment.AssessmentTemplate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentTemplateRepository extends JpaRepository<AssessmentTemplate, UUID> {
}
