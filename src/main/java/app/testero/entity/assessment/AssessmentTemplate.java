package app.testero.entity.assessment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "assessment_template")
@Getter
@Setter
@NoArgsConstructor
public class AssessmentTemplate {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "available_from")
    private LocalDateTime availableFrom;

    @Column(name = "available_until")
    private LocalDateTime availableUntil;

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
    private AssessmentType type = AssessmentType.CERT_SIMULATION;

    @Column(name = "passing_score")
    private BigDecimal passingScore;

    @Column(name = "pts_unanswered", nullable = false)
    private BigDecimal ptsUnanswered;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "shuffle_questions", nullable = false)
    private boolean shuffleQuestions;

    @Column(name = "shuffle_options", nullable = false)
    private boolean shuffleOptions;

    @Column(name = "assessment_description", columnDefinition = "text")
    private String assessmentDescription;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
