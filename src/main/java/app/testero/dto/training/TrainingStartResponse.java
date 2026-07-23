package app.testero.dto.training;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TrainingStartResponse(
        @JsonProperty("submission_id") String submissionId,
        @JsonProperty("assessment_snapshot_id") String assessmentSnapshotId,
        @JsonProperty("timer_minutes") Integer timerMinutes,
        @JsonProperty("total_questions") int totalQuestions
) {}
