package app.testero.repository.user;

import app.testero.entity.user.AppUserRole;
import app.testero.entity.user.AppUserRoleId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppUserRoleRepository extends JpaRepository<AppUserRole, AppUserRoleId> {
    List<AppUserRole> findByUserId(UUID userId);

    /**
     * Role names granted to a user, e.g. {@code ["TEACHER"]}.
     *
     * <p>Roles are not carried in the JWT, so authorization reads them from the database.
     */
    @Query("SELECT r.name FROM AppUserRole ur, AppRole r "
            + "WHERE r.id = ur.roleId AND ur.userId = :userId")
    List<String> findRoleNamesByUserId(@Param("userId") UUID userId);
}
