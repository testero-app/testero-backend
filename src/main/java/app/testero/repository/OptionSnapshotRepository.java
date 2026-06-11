package app.testero.repository;

import app.testero.entity.snapshot.OptionSnapshot;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionSnapshotRepository extends JpaRepository<OptionSnapshot, UUID> {

    List<OptionSnapshot> findByQuestionSnapshotIdInOrderByPosition(List<UUID> questionSnapshotIds);

    List<OptionSnapshot> findByQuestionSnapshotIdInAndCorrectTrue(List<UUID> questionSnapshotIds);
}
