package app.testero.entity.assessment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "test")
@Getter
@Setter
@NoArgsConstructor
public class Assessment {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "timer_minutes", nullable = false)
    private int timerMinutes;

    @Column(name = "total_pool", nullable = false)
    private int totalPool;

    @Column(name = "questions_per_test", nullable = false)
    private int questionsPerAssessment;

    @Column(name = "pts_correct", nullable = false)
    private BigDecimal ptsCorrect;

    @Column(name = "pts_wrong", nullable = false)
    private BigDecimal ptsWrong;
}
