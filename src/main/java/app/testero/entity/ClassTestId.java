package app.testero.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ClassTestId implements Serializable {

    private UUID classId;
    private UUID testId;

    public ClassTestId() {}

    public ClassTestId(UUID classId, UUID testId) {
        this.classId = classId;
        this.testId = testId;
    }

    public UUID getClassId() { return classId; }
    public void setClassId(UUID classId) { this.classId = classId; }

    public UUID getTestId() { return testId; }
    public void setTestId(UUID testId) { this.testId = testId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassTestId that = (ClassTestId) o;
        return Objects.equals(classId, that.classId) && Objects.equals(testId, that.testId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classId, testId);
    }
}
