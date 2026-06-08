package app.testero.entity.submission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_answer")
@Getter
@Setter
@NoArgsConstructor
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(nullable = false)
    private String type;

    @Column(columnDefinition = "text")
    private String text;

    @Column(columnDefinition = "text")
    private String motivation;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "points_awarded")
    private Double pointsAwarded;
}
