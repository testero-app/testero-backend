package app.testero.dto.assessment;
import app.testero.dto.common.PaginationMetadata;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record AssessmentListResponse(List<AssessmentListItem> assessments,
                                     PaginationMetadata pagination) {

    public record AssessmentListItem(
            String id,
            String title,
            @Nullable String availableFrom,
            @Nullable String availableUntil,
            int timerMinutes,
            int questionsPerAssessment,
            @Nullable String difficulty,
            String type,
            String status,
            // null until the assessment has been completed at least once.
            @Nullable Double score,
            List<SubjectDto> subjects
    ) {}
}
