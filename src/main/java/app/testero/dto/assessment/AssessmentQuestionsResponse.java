package app.testero.dto.assessment;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record AssessmentQuestionsResponse(
        // null for a free training session: its paper belongs to no single assessment.
        @Nullable String assessmentId,
        String title,
        @Nullable String availableFrom,
        @Nullable String availableUntil,
        int timerMinutes,
        int totalQuestions,
        List<QuestionDto> questions
) {
    public record QuestionDto(
            String id,
            String type,
            String text,
            @Nullable String code,
            // null for open questions — only multiple-choice ones carry options.
            @Nullable List<OptionDto> options,
            @Nullable Double points,
            List<SubjectDto> subjects
    ) {}

    public record OptionDto(
            String id,
            String text,
            Boolean isFallback
    ) {}
}
