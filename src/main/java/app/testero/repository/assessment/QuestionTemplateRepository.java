package app.testero.repository.assessment;

import app.testero.entity.assessment.QuestionTemplate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionTemplateRepository extends JpaRepository<QuestionTemplate, UUID> {

    List<QuestionTemplate> findByAssessmentIdOrderByPosition(UUID assessmentId);

    /**
     * Questions carrying the given tag, restricted to templates owned by the given teacher.
     *
     * <p>The owner restriction is what keeps a teacher's tag filter inside their own bank —
     * you never see another teacher's questions, even if a tag id leaked.
     */
    @Query("SELECT qt FROM QuestionTemplate qt, QuestionTag qtag, AssessmentTemplate at "
            + "WHERE qtag.tagId = :tagId AND qtag.questionTemplateId = qt.id "
            + "AND qt.assessmentId = at.id AND at.ownerId = :ownerId "
            + "ORDER BY qt.position")
    List<QuestionTemplate> findByTagAndOwner(@Param("tagId") UUID tagId,
                                             @Param("ownerId") UUID ownerId);
}
