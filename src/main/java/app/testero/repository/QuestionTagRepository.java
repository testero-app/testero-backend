package app.testero.repository;

import app.testero.entity.tag.QuestionTag;
import app.testero.entity.tag.QuestionTagId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionTagRepository extends JpaRepository<QuestionTag, QuestionTagId> {

    List<QuestionTag> findByQuestionTemplateId(UUID questionTemplateId);

    boolean existsByQuestionTemplateIdAndTagId(UUID questionTemplateId, UUID tagId);

    /** Tag ids attached to each of the given questions — for bulk view assembly. */
    List<QuestionTag> findByQuestionTemplateIdIn(List<UUID> questionTemplateIds);
}
