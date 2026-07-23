package app.testero.dto.assessment;
import app.testero.dto.common.PaginationMetadata;

import java.util.List;

public record AssessmentListResponse(List<AssessmentListItem> assessments,
                                     PaginationMetadata pagination) {

    public record AssessmentListItem(
            String id,
            String title,
            String availableFrom,
            String availableUntil,
            int timerMinutes,
            int questionsPerAssessment,
            String difficulty,
            String type,
            String status,
            Double score,
            List<SubjectDto> subjects
    ) {}
}
