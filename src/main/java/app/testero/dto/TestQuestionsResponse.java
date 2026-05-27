package app.testero.dto;

import java.util.List;

public record TestQuestionsResponse(
        String testId,
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
            List<OptionDto> options
    ) {}

    public record OptionDto(
            String id,
            String text,
            Boolean isFallback
    ) {}
}
