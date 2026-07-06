package app.testero.repository;

import app.testero.entity.assessment.QuestionTemplateSubject;
import app.testero.entity.assessment.QuestionTemplateSubjectId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionTemplateSubjectRepository
        extends JpaRepository<QuestionTemplateSubject, QuestionTemplateSubjectId> {

    List<QuestionTemplateSubject> findByQuestionTemplateId(UUID questionTemplateId);

    List<QuestionTemplateSubject> findByQuestionTemplateIdIn(List<UUID> questionTemplateIds);

    List<QuestionTemplateSubject> findBySubjectIdIn(List<UUID> subjectIds);
}
