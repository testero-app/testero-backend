package app.testero.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "test")
public class Test {

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
    private int questionsPerTest;

    @Column(name = "pts_correct", nullable = false)
    private BigDecimal ptsCorrect;

    @Column(name = "pts_wrong", nullable = false)
    private BigDecimal ptsWrong;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getTimerMinutes() { return timerMinutes; }
    public void setTimerMinutes(int timerMinutes) { this.timerMinutes = timerMinutes; }

    public int getTotalPool() { return totalPool; }
    public void setTotalPool(int totalPool) { this.totalPool = totalPool; }

    public int getQuestionsPerTest() { return questionsPerTest; }
    public void setQuestionsPerTest(int questionsPerTest) { this.questionsPerTest = questionsPerTest; }

    public BigDecimal getPtsCorrect() { return ptsCorrect; }
    public void setPtsCorrect(BigDecimal ptsCorrect) { this.ptsCorrect = ptsCorrect; }

    public BigDecimal getPtsWrong() { return ptsWrong; }
    public void setPtsWrong(BigDecimal ptsWrong) { this.ptsWrong = ptsWrong; }
}
