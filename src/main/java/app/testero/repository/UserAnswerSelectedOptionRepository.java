package app.testero.repository;

import app.testero.entity.UserAnswerSelectedOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface UserAnswerSelectedOptionRepository extends JpaRepository<UserAnswerSelectedOption, UUID> {

    List<UserAnswerSelectedOption> findByAnswerIdIn(List<UUID> answerIds);
}
