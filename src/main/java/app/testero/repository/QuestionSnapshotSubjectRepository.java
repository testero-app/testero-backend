package app.testero.repository;

import app.testero.entity.snapshot.QuestionSnapshotSubject;
import app.testero.entity.snapshot.QuestionSnapshotSubjectId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionSnapshotSubjectRepository
        extends JpaRepository<QuestionSnapshotSubject, QuestionSnapshotSubjectId> {

    List<QuestionSnapshotSubject> findByQuestionSnapshotId(UUID questionSnapshotId);

    List<QuestionSnapshotSubject> findByQuestionSnapshotIdIn(List<UUID> questionSnapshotIds);
}
