package app.testero.repository;

import app.testero.entity.TeacherClass;
import app.testero.entity.TeacherClassId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TeacherClassRepository extends JpaRepository<TeacherClass, TeacherClassId> {

    List<TeacherClass> findByUserId(UUID userId);

    List<TeacherClass> findByClassId(UUID classId);
}
