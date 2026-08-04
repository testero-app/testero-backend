package app.testero.repository.assessment;

import app.testero.entity.assessment.AssessmentType;
import app.testero.entity.snapshot.AssessmentSnapshot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentSnapshotRepository extends JpaRepository<AssessmentSnapshot, UUID> {

    Optional<AssessmentSnapshot> findByAssessmentTemplateIdAndContentHash(
            UUID assessmentTemplateId, String contentHash);

    @Query("SELECT s FROM AssessmentSnapshot s JOIN ClassAssessmentAssignment ca "
            + "ON ca.assessmentSnapshotId = s.id "
            + "WHERE ca.classId = :classId "
            + "AND (ca.availableFrom IS NULL OR ca.availableFrom <= CURRENT_TIMESTAMP) "
            + "AND (ca.availableUntil IS NULL OR ca.availableUntil >= CURRENT_TIMESTAMP)")
    List<AssessmentSnapshot> findSnapshotsByClassId(@Param("classId") UUID classId);

    @Query("SELECT s FROM AssessmentSnapshot s JOIN ClassAssessmentAssignment ca "
            + "ON ca.assessmentSnapshotId = s.id "
            + "WHERE ca.classId = :classId "
            + "AND (ca.availableFrom IS NULL OR ca.availableFrom <= CURRENT_TIMESTAMP) "
            + "AND (ca.availableUntil IS NULL OR ca.availableUntil >= CURRENT_TIMESTAMP)")
    Page<AssessmentSnapshot> findSnapshotsByClassId(@Param("classId") UUID classId,
                                                    Pageable pageable);

    /**
     * The pools a class may practise on: what is assigned to it, of a type a student is
     * allowed to meet outside a sitting. Exams are excluded by the caller's type list — a
     * student must not train on the questions of a test they have yet to sit.
     */
    @Query("SELECT s FROM AssessmentSnapshot s JOIN ClassAssessmentAssignment ca "
            + "ON ca.assessmentSnapshotId = s.id "
            + "WHERE ca.classId = :classId "
            + "AND s.type IN :types "
            + "AND (ca.availableFrom IS NULL OR ca.availableFrom <= CURRENT_TIMESTAMP) "
            + "AND (ca.availableUntil IS NULL OR ca.availableUntil >= CURRENT_TIMESTAMP)")
    List<AssessmentSnapshot> findPractisableSnapshots(@Param("classId") UUID classId,
                                                      @Param("types") List<AssessmentType> types);
}
