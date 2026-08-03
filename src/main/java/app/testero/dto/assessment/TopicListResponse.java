package app.testero.dto.assessment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record TopicListResponse(List<TopicItem> topics) {

    public record TopicItem(
            String id,
            String title,
            @Nullable String abbreviation,
            @Nullable String description,
            boolean enabled,
            List<ChapterItem> chapters,
            @JsonProperty("total_chapters") int totalChapters,
            @JsonProperty("total_questions") int totalQuestions
    ) {}

    public record ChapterItem(
            String id,
            String label,
            @JsonProperty("question_counts") QuestionCounts questionCounts
    ) {}

    public record QuestionCounts(
            int base,
            int intermediate,
            int advanced
    ) {}
}
