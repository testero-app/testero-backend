package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record SaveAnswerRequest(
        @NotBlank String type,
        String text,
        String motivation,
        @JsonProperty("selected_option_ids") List<String> selectedOptionIds,
        Boolean flagged
) {
    public SaveAnswerRequest {
        if (selectedOptionIds == null) {
            selectedOptionIds = List.of();
        }
    }
}
