package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TrainingStartRequest(
        @NotNull @JsonProperty("topic_id") String topicId,
        @NotEmpty @JsonProperty("chapter_ids") List<String> chapterIds,
        @NotNull String difficulty,
        @Min(1) @Max(100) @JsonProperty("question_count") int questionCount,
        @JsonProperty("timer_enabled") boolean timerEnabled
) {}
