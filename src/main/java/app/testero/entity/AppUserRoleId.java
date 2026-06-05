package app.testero.entity;

import java.io.Serializable;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AppUserRoleId implements Serializable {
    private UUID userId;
    private UUID roleId;
}
