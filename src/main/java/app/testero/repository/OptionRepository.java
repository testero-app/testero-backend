package app.testero.repository;

import app.testero.entity.assessment.Option;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionRepository extends JpaRepository<Option, UUID> {

    List<Option> findByQuestionIdInOrderByPosition(List<UUID> questionIds);

    List<Option> findByQuestionIdInAndCorrectTrue(List<UUID> questionIds);
}
