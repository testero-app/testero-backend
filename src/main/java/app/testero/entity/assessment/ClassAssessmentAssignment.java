package app.testero.entity.assessment;

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

@Entity
@Table(name = "class_assessment_assignment")
@IdClass(ClassAssessmentAssignmentId.class)
@Getter
@Setter
@NoArgsConstructor
public class ClassAssessmentAssignment {

    @Id
    @Column(name = "class_id")
    private UUID classId;

    @Id
    @Column(name = "assessment_snapshot_id")
    private UUID assessmentSnapshotId;

    @Column(name = "available_from")
    private LocalDateTime availableFrom;

    @Column(name = "available_until")
    private LocalDateTime availableUntil;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
