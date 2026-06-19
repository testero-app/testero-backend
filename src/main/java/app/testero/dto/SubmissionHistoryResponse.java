package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SubmissionHistoryResponse(
        List<SubmissionSummary> submissions,
        PaginationMetadata pagination
) {
    public record SubmissionSummary(
            String id,
            @JsonProperty("assessment_snapshot_id") String assessmentSnapshotId,
            @JsonProperty("assessment_title") String assessmentTitle,
            String type,
            @JsonProperty("started_at") String startedAt,
            @JsonProperty("submitted_at") String submittedAt,
            Double score,
            @JsonProperty("max_score") Double maxScore,
            Boolean passed,
            @JsonProperty("total_questions") int totalQuestions,
            @JsonProperty("correct_count") int correctCount,
            @JsonProperty("wrong_count") int wrongCount,
            @JsonProperty("unanswered_count") int unansweredCount
    ) {}
}
