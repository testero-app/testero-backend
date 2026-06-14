package app.testero.entity.snapshot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "question_snapshot")
@Getter
@Setter
@NoArgsConstructor
public class QuestionSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "assessment_snapshot_id", nullable = false)
    private UUID assessmentSnapshotId;

    @Column(name = "original_question_id")
    private UUID originalQuestionId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(columnDefinition = "text")
    private String code;

    @Column(columnDefinition = "text")
    private String explanation;

    @Column(nullable = false)
    private int position;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
