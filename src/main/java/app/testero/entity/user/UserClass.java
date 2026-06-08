package app.testero.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_class")
@Getter
@Setter
@NoArgsConstructor
public class UserClass {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;
}
