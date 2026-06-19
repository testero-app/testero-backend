package app.testero.repository;

import app.testero.entity.assessment.TopicSubject;
import app.testero.entity.assessment.TopicSubjectId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicSubjectRepository extends JpaRepository<TopicSubject, TopicSubjectId> {
    List<TopicSubject> findByTopicIdOrderByPositionAsc(UUID topicId);
    List<TopicSubject> findByTopicIdInOrderByPositionAsc(List<UUID> topicIds);
}
