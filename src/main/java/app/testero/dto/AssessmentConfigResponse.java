package app.testero.dto;

public record AssessmentConfigResponse(
        String assessmentId,
        String title,
        String date,
        int timerMinutes,
        int totalPool,
        int questionsPerTest,
        ScoringRules scoring
) {
    public record ScoringRules(
            double pointsPerCorrect,
            double pointsPerWrong
    ) {}
}
