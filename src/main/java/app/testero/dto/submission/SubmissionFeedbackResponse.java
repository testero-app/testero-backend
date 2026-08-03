package app.testero.dto.submission;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record SubmissionFeedbackResponse(
        String id,
        @JsonProperty("user_id") String userId,
        @JsonProperty("assessment_snapshot_id") String assessmentSnapshotId,
        @JsonProperty("started_at") @Nullable String startedAt,
        @JsonProperty("submitted_at") @Nullable String submittedAt,
        @Nullable Double score,
        // Both null when the assessment snapshot is gone; passed also needs a passing_score.
        @JsonProperty("max_score") @Nullable Double maxScore,
        @Nullable Boolean passed,
        @JsonProperty("passing_score") @Nullable Double passingScore,
        List<AnswerResult> answers,
        @JsonProperty("subject_scores") List<SubjectScore> subjectScores
) {
    public record AnswerResult(
            @JsonProperty("question_snapshot_id") String questionSnapshotId,
            String type,
            // Both null for open questions, which are not auto-graded.
            @JsonProperty("is_correct") @Nullable Boolean isCorrect,
            @JsonProperty("correct_option_snapshot_ids") List<String> correctOptionSnapshotIds,
            @JsonProperty("points_awarded") @Nullable Double pointsAwarded
    ) {}

    public record SubjectScore(
            @JsonProperty("subject_id") String subjectId,
            String label,
            @JsonProperty("points_earned") double pointsEarned,
            @JsonProperty("points_available") double pointsAvailable
    ) {}
}
