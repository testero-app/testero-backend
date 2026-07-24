package app.testero.repository.assessment;

import app.testero.entity.assessment.OptionTemplate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionTemplateRepository extends JpaRepository<OptionTemplate, UUID> {

    List<OptionTemplate> findByQuestionTemplateIdInOrderByPosition(List<UUID> questionTemplateIds);

    List<OptionTemplate> findByQuestionTemplateIdInAndCorrectTrue(List<UUID> questionTemplateIds);
}
