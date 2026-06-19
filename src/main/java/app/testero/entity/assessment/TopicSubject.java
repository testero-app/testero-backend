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
@Table(name = "topic_subject")
@IdClass(TopicSubjectId.class)
@Getter
@Setter
@NoArgsConstructor
public class TopicSubject {

    @Id
    @Column(name = "topic_id")
    private UUID topicId;

    @Id
    @Column(name = "subject_id")
    private UUID subjectId;

    @Column
    private int position;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
