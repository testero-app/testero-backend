package app.testero.repository;

import app.testero.entity.notification.DeadlineReminderSent;
import app.testero.entity.notification.DeadlineReminderSentId;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadlineReminderSentRepository
        extends JpaRepository<DeadlineReminderSent, DeadlineReminderSentId> {

    boolean existsByAssessmentSnapshotIdAndUserId(UUID assessmentSnapshotId, UUID userId);
}
