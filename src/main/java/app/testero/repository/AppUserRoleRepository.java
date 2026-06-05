package app.testero.repository;

import app.testero.entity.AppUserRole;
import app.testero.entity.AppUserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AppUserRoleRepository extends JpaRepository<AppUserRole, AppUserRoleId> {
    List<AppUserRole> findByUserId(UUID userId);
}
