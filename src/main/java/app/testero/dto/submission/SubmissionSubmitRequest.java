package app.testero.dto.submission;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SubmissionSubmitRequest(
        @Valid @NotEmpty List<AnswerInput> answers
) {}
