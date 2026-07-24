package app.testero.dto.assessment;
import app.testero.dto.tag.TagResponse;

import java.util.List;

/**
 * A question template with its tags, for the teacher-facing "browse my bank by tag" view.
 *
 * <p>Deliberately separate from the student question DTO, which hides correct answers — this
 * one is only ever returned to the owning teacher, so it may carry the full template.
 */
public record TaggedQuestionResponse(
        String id,
        String type,
        String text,
        Integer position,
        List<TagResponse> tags
) {}
