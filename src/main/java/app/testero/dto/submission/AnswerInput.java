package app.testero.dto.submission;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AnswerInput(
        @JsonProperty("question_id") @NotBlank String questionId,
        @NotBlank String type,
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) String text,
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) String motivation,
        @JsonProperty("selected_option_ids")
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) List<String> selectedOptionIds
) {
    public AnswerInput {
        if (selectedOptionIds == null) {
            selectedOptionIds = List.of();
        }
    }
}
