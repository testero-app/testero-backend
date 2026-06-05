package app.testero.repository;

import app.testero.entity.TestSubject;
import app.testero.entity.TestSubjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TestSubjectRepository extends JpaRepository<TestSubject, TestSubjectId> {

    List<TestSubject> findByTestId(UUID testId);

    List<TestSubject> findBySubjectId(UUID subjectId);
}
