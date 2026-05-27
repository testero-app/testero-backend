package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SubmissionCreateRequest(
        @JsonProperty("test_id") @NotBlank String testId,
        @Valid @NotEmpty List<AnswerInput> answers
) {}
