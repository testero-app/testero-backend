package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SubmissionStartResponse(
        @JsonProperty("submission_id") String submissionId,
        @JsonProperty("started_at") String startedAt
) {}
