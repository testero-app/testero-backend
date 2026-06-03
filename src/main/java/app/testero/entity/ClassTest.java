package app.testero.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "class_test")
@IdClass(ClassTestId.class)
@Getter
@Setter
@NoArgsConstructor
public class ClassTest {

    @Id
    @Column(name = "class_id")
    private UUID classId;

    @Id
    @Column(name = "test_id")
    private UUID testId;
}
