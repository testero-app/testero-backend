package app.testero.entity.assessment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "option")
@Getter
@Setter
@NoArgsConstructor
public class Option {

    @Id
    private UUID id;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(nullable = false)
    private String text;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "is_fallback", nullable = false)
    private boolean fallback;

    @Column(nullable = false)
    private int position;
}
