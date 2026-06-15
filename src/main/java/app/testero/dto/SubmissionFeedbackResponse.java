package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SubmissionFeedbackResponse(
        String id,
        @JsonProperty("user_id") String userId,
        @JsonProperty("assessment_snapshot_id") String assessmentSnapshotId,
        @JsonProperty("started_at") String startedAt,
        @JsonProperty("submitted_at") String submittedAt,
        Double score,
        @JsonProperty("max_score") Double maxScore,
        Boolean passed,
        @JsonProperty("passing_score") Double passingScore,
        List<AnswerResult> answers,
        @JsonProperty("subject_scores") List<SubjectScore> subjectScores
) {
    public record AnswerResult(
            @JsonProperty("question_snapshot_id") String questionSnapshotId,
            String type,
            @JsonProperty("is_correct") Boolean isCorrect,
            @JsonProperty("correct_option_snapshot_ids") List<String> correctOptionSnapshotIds,
            @JsonProperty("points_awarded") Double pointsAwarded
    ) {}

    public record SubjectScore(
            @JsonProperty("subject_id") String subjectId,
            String label,
            @JsonProperty("points_earned") double pointsEarned,
            @JsonProperty("points_available") double pointsAvailable
    ) {}
}
