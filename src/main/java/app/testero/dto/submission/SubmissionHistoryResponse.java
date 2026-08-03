package app.testero.dto.submission;
import app.testero.dto.common.PaginationMetadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record SubmissionHistoryResponse(
        List<SubmissionSummary> submissions,
        PaginationMetadata pagination
) {
    public record SubmissionSummary(
            String id,
            @JsonProperty("assessment_snapshot_id") String assessmentSnapshotId,
            @JsonProperty("assessment_title") String assessmentTitle,
            String type,
            @JsonProperty("started_at") @Nullable String startedAt,
            @JsonProperty("submitted_at") @Nullable String submittedAt,
            @Nullable Double score,
            @JsonProperty("max_score") @Nullable Double maxScore,
            @Nullable Boolean passed,
            @JsonProperty("total_questions") int totalQuestions,
            @JsonProperty("correct_count") int correctCount,
            @JsonProperty("wrong_count") int wrongCount,
            @JsonProperty("unanswered_count") int unansweredCount,
            @JsonProperty("subject_scores")
            List<SubmissionFeedbackResponse.SubjectScore> subjectScores
    ) {}
}
