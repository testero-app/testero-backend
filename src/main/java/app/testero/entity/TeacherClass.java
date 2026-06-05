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
@Table(name = "teacher_class")
@IdClass(TeacherClassId.class)
@Getter
@Setter
@NoArgsConstructor
public class TeacherClass {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "class_id")
    private UUID classId;
}
