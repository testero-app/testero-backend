package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TopicChaptersResponse(
        @JsonProperty("topic_id") String topicId,
        @JsonProperty("topic_title") String topicTitle,
        List<TopicListResponse.ChapterItem> chapters,
        @JsonProperty("available_questions") AvailableQuestions availableQuestions
) {
    public record AvailableQuestions(
            int base,
            int intermediate,
            int advanced,
            int total
    ) {}
}
