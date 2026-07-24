package app.testero.repository.assessment;

import app.testero.entity.snapshot.AssessmentSnapshotSubject;
import app.testero.entity.snapshot.AssessmentSnapshotSubjectId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentSnapshotSubjectRepository
        extends JpaRepository<AssessmentSnapshotSubject, AssessmentSnapshotSubjectId> {

    List<AssessmentSnapshotSubject> findByAssessmentSnapshotId(UUID assessmentSnapshotId);
}
