package app.testero.repository.assessment;

import app.testero.entity.assessment.ClassAssessmentAssignment;
import app.testero.entity.assessment.ClassAssessmentAssignmentId;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassAssessmentAssignmentRepository
        extends JpaRepository<ClassAssessmentAssignment, ClassAssessmentAssignmentId> {

    /**
     * Assignments whose deadline falls in (now, until] — i.e. approaching but not yet passed.
     * Assignments with no deadline (availableUntil IS NULL) are always open and never remind.
     */
    @Query("SELECT a FROM ClassAssessmentAssignment a "
            + "WHERE a.availableUntil IS NOT NULL "
            + "AND a.availableUntil > :now "
            + "AND a.availableUntil <= :until")
    List<ClassAssessmentAssignment> findWithDeadlineBetween(
            @Param("now") LocalDateTime now, @Param("until") LocalDateTime until);
}
