package app.testero.dto.assessment;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record AssessmentConfigResponse(
        String assessmentId,
        String title,
        @Nullable String availableFrom,
        @Nullable String availableUntil,
        int timerMinutes,
        int questionsPerAssessment,
        ScoringRules scoring,
        boolean shuffleQuestions,
        boolean shuffleOptions,
        // null = unlimited attempts.
        @Nullable Integer maxAttempts,
        List<SubjectDto> subjects
) {
    public record ScoringRules(
            double pointsPerCorrect,
            double pointsPerWrong,
            double pointsPerUnanswered
    ) {}
}
