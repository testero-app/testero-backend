package app.testero.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "class_test")
@IdClass(ClassTestId.class)
public class ClassTest {

    @Id
    @Column(name = "class_id")
    private UUID classId;

    @Id
    @Column(name = "test_id")
    private UUID testId;

    public UUID getClassId() { return classId; }
    public void setClassId(UUID classId) { this.classId = classId; }

    public UUID getTestId() { return testId; }
    public void setTestId(UUID testId) { this.testId = testId; }
}
