package app.testero.dto;

import java.util.List;

public record AssessmentQuestionsResponse(
        String assessmentId,
        String title,
        String date,
        int timerMinutes,
        int totalQuestions,
        List<QuestionDto> questions
) {
    public record QuestionDto(
            String id,
            String type,
            String text,
            String code,
            List<OptionDto> options,
            Double points
    ) {}

    public record OptionDto(
            String id,
            String text,
            Boolean isFallback
    ) {}
}
