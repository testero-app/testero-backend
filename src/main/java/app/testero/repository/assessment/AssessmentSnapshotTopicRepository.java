package app.testero.repository.assessment;

import app.testero.entity.snapshot.AssessmentSnapshotTopic;
import app.testero.entity.snapshot.AssessmentSnapshotTopicId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentSnapshotTopicRepository
        extends JpaRepository<AssessmentSnapshotTopic, AssessmentSnapshotTopicId> {

    List<AssessmentSnapshotTopic> findByAssessmentSnapshotId(
            UUID assessmentSnapshotId);
}
