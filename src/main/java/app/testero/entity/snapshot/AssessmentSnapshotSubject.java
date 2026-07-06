package app.testero.entity.snapshot;

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
@Table(name = "assessment_snapshot_subject")
@IdClass(AssessmentSnapshotSubjectId.class)
@Getter
@Setter
@NoArgsConstructor
public class AssessmentSnapshotSubject {

    @Id
    @Column(name = "assessment_snapshot_id")
    private UUID assessmentSnapshotId;

    @Id
    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
