package app.testero.entity.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Marker that a DEADLINE_REMINDER has already been sent to one student for one
 * administered assessment. Its presence is the de-dup signal for the scheduler.
 */
@Entity
@Table(name = "deadline_reminder_sent")
@IdClass(DeadlineReminderSentId.class)
@Getter
@Setter
@NoArgsConstructor
public class DeadlineReminderSent {

    @Id
    @Column(name = "assessment_snapshot_id")
    private UUID assessmentSnapshotId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "sent_at", insertable = false, updatable = false)
    private LocalDateTime sentAt;

    public DeadlineReminderSent(UUID assessmentSnapshotId, UUID userId) {
        this.assessmentSnapshotId = assessmentSnapshotId;
        this.userId = userId;
    }
}
