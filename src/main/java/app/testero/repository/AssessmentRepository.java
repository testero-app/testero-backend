package app.testero.repository;

import app.testero.entity.assessment.Assessment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
}
