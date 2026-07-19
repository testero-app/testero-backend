package app.testero.entity.tag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A tag in a teacher's private vocabulary for organising their question bank.
 *
 * <p>Owned by one teacher via {@code ownerId} (an {@code app_user}, gated to teachers by role):
 * tags are never shared globally. Two teachers may each own a tag with the same name, but a
 * single teacher cannot (UNIQUE owner_id, name). Mirrors {@code assessment_template.owner_id}.
 */
@Entity
@Table(name = "tag")
@Getter
@Setter
@NoArgsConstructor
public class Tag {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
