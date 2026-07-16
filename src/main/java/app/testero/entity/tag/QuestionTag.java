package app.testero.entity.tag;

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

/** Links a {@link Tag} to a {@code question_template}. Many-to-many. */
@Entity
@Table(name = "question_tag")
@IdClass(QuestionTagId.class)
@Getter
@Setter
@NoArgsConstructor
public class QuestionTag {

    @Id
    @Column(name = "question_template_id")
    private UUID questionTemplateId;

    @Id
    @Column(name = "tag_id")
    private UUID tagId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public QuestionTag(UUID questionTemplateId, UUID tagId) {
        this.questionTemplateId = questionTemplateId;
        this.tagId = tagId;
    }
}
