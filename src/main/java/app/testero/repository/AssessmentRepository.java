package app.testero.repository;

import app.testero.entity.assessment.Assessment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

    @Query("SELECT a FROM Assessment a JOIN ClassAssessment ca ON ca.assessmentId = a.id "
            + "WHERE ca.classId = :classId "
            + "AND ca.activatedAt IS NOT NULL "
            + "AND ca.deactivatedAt IS NULL")
    List<Assessment> findAssessmentsByClassId(@Param("classId") UUID classId);
}
