package app.testero.repository;

import app.testero.entity.tag.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findByTeacherIdOrderByName(UUID teacherId);

    boolean existsByTeacherIdAndName(UUID teacherId, String name);

    /** Tags attached to a question, ordered by name — for the question view. */
    List<Tag> findByIdInOrderByName(List<UUID> ids);
}
