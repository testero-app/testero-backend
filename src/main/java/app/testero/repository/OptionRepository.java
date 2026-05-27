package app.testero.repository;

import app.testero.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OptionRepository extends JpaRepository<Option, UUID> {

    List<Option> findByQuestionIdInOrderByPosition(List<UUID> questionIds);

    List<Option> findByQuestionIdInAndCorrectTrue(List<UUID> questionIds);
}
