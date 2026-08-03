package app.testero.dto.training;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record TrainingStartResponse(
        @JsonProperty("submission_id") String submissionId,
        @JsonProperty("assessment_snapshot_id") String assessmentSnapshotId,
        // null when the student started the session with the timer off.
        @JsonProperty("timer_minutes") @Nullable Integer timerMinutes,
        @JsonProperty("total_questions") int totalQuestions
) {}
