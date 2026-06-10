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
@Table(name = "test_subject")
@IdClass(AssessmentSubjectId.class)
@Getter
@Setter
@NoArgsConstructor
public class AssessmentSubject {

    @Id
    @Column(name = "test_id")
    private UUID assessmentId;

    @Id
    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
