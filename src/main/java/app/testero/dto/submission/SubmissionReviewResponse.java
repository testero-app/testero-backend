package app.testero.dto.submission;
import app.testero.dto.assessment.SubjectDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record SubmissionReviewResponse(
        String id,
        @JsonProperty("assessment_title") String assessmentTitle,
        @JsonProperty("started_at") @Nullable String startedAt,
        @JsonProperty("submitted_at") @Nullable String submittedAt,
        @Nullable Double score,
        @JsonProperty("max_score") @Nullable Double maxScore,
        List<ReviewQuestion> questions
) {
    public record ReviewQuestion(
            String id,
            String type,
            String text,
            @Nullable String code,
            int position,
            @Nullable String explanation,
            // Null whenever the question was left unanswered, and for open questions.
            @JsonProperty("is_correct") @Nullable Boolean isCorrect,
            @JsonProperty("selected_option_ids") List<String> selectedOptionIds,
            @JsonProperty("answer_text") @Nullable String answerText,
            @Nullable String motivation,
            List<ReviewOption> options,
            @Nullable Double points,
            @JsonProperty("points_awarded") @Nullable Double pointsAwarded,
            List<SubjectDto> subjects
    ) {}

    public record ReviewOption(
            String id,
            String text,
            int position,
            @JsonProperty("is_correct") boolean isCorrect
    ) {}
}
