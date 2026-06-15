package app.testero.repository;

import app.testero.entity.assessment.QuestionSubject;
import app.testero.entity.assessment.QuestionSubjectId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionSubjectRepository extends JpaRepository<QuestionSubject, QuestionSubjectId> {

    List<QuestionSubject> findByQuestionId(UUID questionId);

    List<QuestionSubject> findByQuestionIdIn(List<UUID> questionIds);
}
