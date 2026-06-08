package app.testero.repository;

import app.testero.entity.user.TeacherClass;
import app.testero.entity.user.TeacherClassId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherClassRepository extends JpaRepository<TeacherClass, TeacherClassId> {

    List<TeacherClass> findByUserId(UUID userId);

    List<TeacherClass> findByClassId(UUID classId);
}
