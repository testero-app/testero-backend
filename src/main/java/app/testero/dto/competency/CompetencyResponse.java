package app.testero.dto.competency;

import java.util.List;

public record CompetencyResponse(List<TopicMastery> topics) {

    public record TopicMastery(
            String id,
            String title,
            int mastery,
            List<TopicMastery> children,
            List<SubjectMastery> subjects
    ) {}

    public record SubjectMastery(
            String id,
            String label,
            int mastery
    ) {}
}
