package app.testero.repository;

import app.testero.entity.snapshot.QuestionSnapshot;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionSnapshotRepository extends JpaRepository<QuestionSnapshot, UUID> {

    List<QuestionSnapshot> findByAssessmentSnapshotIdOrderByPosition(UUID assessmentSnapshotId);
}
