package app.testero.repository;

import app.testero.entity.AnswerSelectedOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AnswerSelectedOptionRepository extends JpaRepository<AnswerSelectedOption, UUID> {

    List<AnswerSelectedOption> findByAnswerIdIn(List<UUID> answerIds);
}
