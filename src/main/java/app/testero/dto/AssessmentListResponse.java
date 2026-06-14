package app.testero.dto;

import java.util.List;

public record AssessmentListResponse(List<AssessmentListItem> assessments) {

    public record AssessmentListItem(
            String id,
            String title,
            String date,
            int timerMinutes,
            int questionsPerAssessment,
            String difficulty,
            String status,
            Double score
    ) {}
}
