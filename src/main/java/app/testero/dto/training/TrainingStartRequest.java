package app.testero.dto.training;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * What the student wants to practise on. Both filters are optional and only narrow the draw:
 * with no chapters the whole topic is used, with no topic at all the questions come from
 * everything the student's class may practise on.
 */
public record TrainingStartRequest(
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @JsonProperty("topic_id") String topicId,

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @JsonProperty("chapter_ids") List<String> chapterIds,

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String difficulty,

        @Min(1) @Max(100) @JsonProperty("question_count") int questionCount,

        @JsonProperty("timer_enabled") boolean timerEnabled
) {}
