package app.testero.repository.assessment;

import app.testero.entity.assessment.Topic;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
    List<Topic> findByEnabledTrueOrderByPositionAsc();
    List<Topic> findAllByOrderByPositionAsc();
}
