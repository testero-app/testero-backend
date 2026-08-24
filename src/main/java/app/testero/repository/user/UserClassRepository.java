package app.testero.repository.user;

import app.testero.entity.user.UserClass;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserClassRepository extends JpaRepository<UserClass, UUID> {
}
