package app.testero.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "question")
public class Question {

    @Id
    private UUID id;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(columnDefinition = "text")
    private String code;

    @Column(nullable = false)
    private int position;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTestId() { return testId; }
    public void setTestId(UUID testId) { this.testId = testId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
