package app.testero.repository;

import app.testero.entity.submission.UserAnswerSelectedOption;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAnswerSelectedOptionRepository extends JpaRepository<UserAnswerSelectedOption, UUID> {

    List<UserAnswerSelectedOption> findByAnswerIdIn(List<UUID> answerIds);

    void deleteByAnswerId(UUID answerId);

    void deleteByAnswerIdIn(List<UUID> answerIds);
}
