package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SubmissionFeedbackResponse(
        String id,
        @JsonProperty("student_id") String studentId,
        @JsonProperty("test_id") String testId,
        @JsonProperty("submitted_at") String submittedAt,
        List<AnswerResult> answers
) {
    public record AnswerResult(
            @JsonProperty("question_id") String questionId,
            String type,
            @JsonProperty("is_correct") Boolean isCorrect,
            @JsonProperty("correct_option_ids") List<String> correctOptionIds
    ) {}
}
