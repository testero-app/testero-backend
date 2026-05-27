package app.testero.repository;

import app.testero.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface TestRepository extends JpaRepository<Test, UUID> {

    @Query("SELECT t FROM Test t JOIN ClassTest ct ON ct.testId = t.id WHERE ct.classId = :classId")
    List<Test> findTestsByClassId(@Param("classId") UUID classId);
}
