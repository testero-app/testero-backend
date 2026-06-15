package app.testero.entity.snapshot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "question_snapshot_subject")
@IdClass(QuestionSnapshotSubjectId.class)
@Getter
@Setter
@NoArgsConstructor
public class QuestionSnapshotSubject {

    @Id
    @Column(name = "question_snapshot_id")
    private UUID questionSnapshotId;

    @Id
    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(name = "weight", nullable = false)
    private BigDecimal weight = new BigDecimal("1.00");

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
