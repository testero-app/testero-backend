package app.testero.service;

import app.testero.exception.ForbiddenException;
import app.testero.repository.AppUserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Role checks for actions that not every authenticated user may perform.
 *
 * <p>Roles live in {@code app_user_role} but are <em>not</em> carried in the JWT, and
 * {@code JwtAuthFilter} builds its {@code Authentication} with no authorities at all. That
 * means Spring's {@code hasRole(...)} would silently evaluate to false for everyone, so
 * authorization is done explicitly here, reading the roles from the database.
 *
 * <p>Reading from the database rather than from the token also means existing tokens keep
 * working: no claim has to be added, nothing has to be re-issued.
 */
@Service
public class AccessService {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_TEACHER = "TEACHER";

    private static final Logger LOG = LoggerFactory.getLogger(AccessService.class);

    private final AppUserRoleRepository appUserRoleRepository;

    public AccessService(AppUserRoleRepository appUserRoleRepository) {
        this.appUserRoleRepository = appUserRoleRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasAnyRole(UUID userId, String... roles) {
        List<String> granted = appUserRoleRepository.findRoleNamesByUserId(userId);
        return List.of(roles).stream().anyMatch(granted::contains);
    }

    /**
     * @throws ForbiddenException if the user holds none of the given roles → 403
     */
    @Transactional(readOnly = true)
    public void requireAnyRole(UUID userId, String... roles) {
        if (!hasAnyRole(userId, roles)) {
            LOG.warn("Forbidden: userId={} lacks any of {}", userId, List.of(roles));
            throw new ForbiddenException("You are not allowed to perform this action");
        }
    }
}
