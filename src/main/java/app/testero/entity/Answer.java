package app.testero.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "answer")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(nullable = false)
    private String type;

    @Column(columnDefinition = "text")
    private String text;

    @Column(columnDefinition = "text")
    private String motivation;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "points_awarded")
    private Double pointsAwarded;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSubmissionId() { return submissionId; }
    public void setSubmissionId(UUID submissionId) { this.submissionId = submissionId; }

    public UUID getQuestionId() { return questionId; }
    public void setQuestionId(UUID questionId) { this.questionId = questionId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getMotivation() { return motivation; }
    public void setMotivation(String motivation) { this.motivation = motivation; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public Double getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(Double pointsAwarded) { this.pointsAwarded = pointsAwarded; }
}
