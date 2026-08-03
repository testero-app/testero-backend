package app.testero.dto.submission;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record SavedAnswersResponse(
        List<SavedAnswer> answers
) {
    public record SavedAnswer(
            @JsonProperty("question_snapshot_id") String questionSnapshotId,
            String type,
            @Nullable String text,
            @Nullable String motivation,
            @JsonProperty("selected_option_ids") List<String> selectedOptionIds,
            boolean flagged
    ) {}
}
