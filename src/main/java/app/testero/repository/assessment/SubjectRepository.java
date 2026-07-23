package app.testero.repository.assessment;

import app.testero.entity.assessment.Subject;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    List<Subject> findByIdIn(List<UUID> ids);
}
