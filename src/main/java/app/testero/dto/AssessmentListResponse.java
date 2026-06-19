package app.testero.dto;

import java.util.List;

public record AssessmentListResponse(List<AssessmentListItem> assessments,
                                     PaginationMetadata pagination) {

    public record AssessmentListItem(
            String id,
            String title,
            String date,
            int timerMinutes,
            int questionsPerAssessment,
            String difficulty,
            String type,
            String status,
            Double score,
            List<SubjectDto> subjects
    ) {}
}
