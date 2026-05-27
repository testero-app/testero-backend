package app.testero.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "test_start")
public class TestStart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getTestId() { return testId; }
    public void setTestId(UUID testId) { this.testId = testId; }
}
