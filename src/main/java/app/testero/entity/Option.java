package app.testero.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "option")
public class Option {

    @Id
    private UUID id;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(nullable = false)
    private String text;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "is_fallback", nullable = false)
    private boolean fallback;

    @Column(nullable = false)
    private int position;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getQuestionId() { return questionId; }
    public void setQuestionId(UUID questionId) { this.questionId = questionId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public boolean isFallback() { return fallback; }
    public void setFallback(boolean fallback) { this.fallback = fallback; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
