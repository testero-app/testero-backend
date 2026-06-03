package app.testero.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "question")
@Getter
@Setter
@NoArgsConstructor
public class Question {

    @Id
    private UUID id;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(columnDefinition = "text")
    private String code;

    @Column(nullable = false)
    private int position;
}
