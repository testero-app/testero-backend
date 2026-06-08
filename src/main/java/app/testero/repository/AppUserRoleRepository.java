package app.testero.repository;

import app.testero.entity.user.AppUserRole;
import app.testero.entity.user.AppUserRoleId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRoleRepository extends JpaRepository<AppUserRole, AppUserRoleId> {
    List<AppUserRole> findByUserId(UUID userId);
}
