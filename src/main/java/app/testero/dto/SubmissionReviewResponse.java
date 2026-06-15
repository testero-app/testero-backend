package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SubmissionReviewResponse(
        String id,
        @JsonProperty("assessment_title") String assessmentTitle,
        @JsonProperty("started_at") String startedAt,
        @JsonProperty("submitted_at") String submittedAt,
        Double score,
        @JsonProperty("max_score") Double maxScore,
        List<ReviewQuestion> questions
) {
    public record ReviewQuestion(
            String id,
            String type,
            String text,
            String code,
            int position,
            String explanation,
            @JsonProperty("is_correct") Boolean isCorrect,
            @JsonProperty("selected_option_ids") List<String> selectedOptionIds,
            @JsonProperty("answer_text") String answerText,
            String motivation,
            List<ReviewOption> options,
            Double points,
            @JsonProperty("points_awarded") Double pointsAwarded,
            List<SubjectDto> subjects
    ) {}

    public record ReviewOption(
            String id,
            String text,
            int position,
            @JsonProperty("is_correct") boolean isCorrect
    ) {}
}
