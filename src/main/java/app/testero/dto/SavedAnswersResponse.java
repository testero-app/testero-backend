package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SavedAnswersResponse(
        List<SavedAnswer> answers
) {
    public record SavedAnswer(
            @JsonProperty("question_snapshot_id") String questionSnapshotId,
            String type,
            String text,
            String motivation,
            @JsonProperty("selected_option_ids") List<String> selectedOptionIds
    ) {}
}
