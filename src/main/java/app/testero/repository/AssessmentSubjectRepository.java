package app.testero.repository;

import app.testero.entity.assessment.AssessmentSubject;
import app.testero.entity.assessment.AssessmentSubjectId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentSubjectRepository extends JpaRepository<AssessmentSubject, AssessmentSubjectId> {

    List<AssessmentSubject> findByAssessmentId(UUID assessmentId);

    List<AssessmentSubject> findBySubjectId(UUID subjectId);
}
