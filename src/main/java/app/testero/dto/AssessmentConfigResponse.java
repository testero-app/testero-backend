package app.testero.dto;

import java.util.List;

public record AssessmentConfigResponse(
        String assessmentId,
        String title,
        String date,
        int timerMinutes,
        int totalPool,
        int questionsPerAssessment,
        ScoringRules scoring,
        List<SubjectDto> subjects
) {
    public record ScoringRules(
            double pointsPerCorrect,
            double pointsPerWrong
    ) {}
}
