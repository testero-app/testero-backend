package app.testero.repository;

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

    Optional<AssessmentSnapshot> findByAssessmentTemplateIdAndContentHash(UUID assessmentTemplateId, String contentHash);

    @Query("SELECT s FROM AssessmentSnapshot s JOIN ClassAssessment ca "
            + "ON ca.assessmentSnapshotId = s.id "
            + "WHERE ca.classId = :classId "
            + "AND (ca.availableFrom IS NULL OR ca.availableFrom <= CURRENT_TIMESTAMP) "
            + "AND (ca.availableUntil IS NULL OR ca.availableUntil >= CURRENT_TIMESTAMP)")
    List<AssessmentSnapshot> findSnapshotsByClassId(@Param("classId") UUID classId);

    @Query("SELECT s FROM AssessmentSnapshot s JOIN ClassAssessment ca "
            + "ON ca.assessmentSnapshotId = s.id "
            + "WHERE ca.classId = :classId "
            + "AND (ca.availableFrom IS NULL OR ca.availableFrom <= CURRENT_TIMESTAMP) "
            + "AND (ca.availableUntil IS NULL OR ca.availableUntil >= CURRENT_TIMESTAMP)")
    Page<AssessmentSnapshot> findSnapshotsByClassId(@Param("classId") UUID classId,
                                                    Pageable pageable);
}
