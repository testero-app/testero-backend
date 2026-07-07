package app.testero.repository;

import app.testero.entity.assessment.AssessmentTemplateTopic;
import app.testero.entity.assessment.AssessmentTemplateTopicId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentTemplateTopicRepository
        extends JpaRepository<AssessmentTemplateTopic, AssessmentTemplateTopicId> {

    List<AssessmentTemplateTopic> findByAssessmentTemplateId(UUID assessmentTemplateId);
}
