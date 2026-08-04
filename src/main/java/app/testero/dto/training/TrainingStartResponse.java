package app.testero.dto.training;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * A free training session has no assessment snapshot of its own — its paper spans the pools
 * the class may practise on — so the client works from the submission id alone and fetches
 * the questions at {@code GET /submissions/{id}/questions}.
 */
public record TrainingStartResponse(
        @JsonProperty("submission_id") String submissionId,
        // null when the student started the session with the timer off.
        @JsonProperty("timer_minutes") @Nullable Integer timerMinutes,
        @JsonProperty("total_questions") int totalQuestions
) {}
