package app.testero.dto;

import java.util.List;

public record AssessmentConfigResponse(
        String assessmentId,
        String title,
        String availableFrom,
        String availableUntil,
        int timerMinutes,
        int questionsPerAssessment,
        ScoringRules scoring,
        boolean shuffleQuestions,
        boolean shuffleOptions,
        Integer maxAttempts,
        List<SubjectDto> subjects
) {
    public record ScoringRules(
            double pointsPerCorrect,
            double pointsPerWrong,
            double pointsPerUnanswered
    ) {}
}
