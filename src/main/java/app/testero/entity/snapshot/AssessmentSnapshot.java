package app.testero.entity.snapshot;

import app.testero.entity.assessment.AssessmentType;
import app.testero.entity.assessment.Difficulty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "assessment_snapshot")
@Getter
@Setter
@NoArgsConstructor
public class AssessmentSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "assessment_id")
    private UUID assessmentId;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false)
    private String title;

    @Column(name = "timer_minutes", nullable = false)
    private int timerMinutes;

    @Column(name = "questions_per_assessment", nullable = false)
    private int questionsPerAssessment;

    @Column(name = "pts_correct", nullable = false)
    private BigDecimal ptsCorrect;

    @Column(name = "pts_wrong", nullable = false)
    private BigDecimal ptsWrong;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentType type = AssessmentType.CERTIFICATION;

    @Column(name = "passing_score")
    private BigDecimal passingScore;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
