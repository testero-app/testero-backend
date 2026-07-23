package app.testero.service.user;

import app.testero.entity.assessment.AssessmentTemplate;
import app.testero.entity.tag.Tag;
import app.testero.exception.ForbiddenException;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.user.AppUserRoleRepository;
import app.testero.repository.assessment.AssessmentTemplateRepository;
import app.testero.repository.tag.TagRepository;
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
    private final AssessmentTemplateRepository assessmentTemplateRepository;
    private final TagRepository tagRepository;

    public AccessService(AppUserRoleRepository appUserRoleRepository,
                         AssessmentTemplateRepository assessmentTemplateRepository,
                         TagRepository tagRepository) {
        this.appUserRoleRepository = appUserRoleRepository;
        this.assessmentTemplateRepository = assessmentTemplateRepository;
        this.tagRepository = tagRepository;
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

    /**
     * Authorize publishing (and, later, editing) of an assessment template.
     *
     * <ul>
     *   <li>an admin may act on any template, including platform content (owner is null);</li>
     *   <li>a teacher may act only on a template they own;</li>
     *   <li>anyone else is forbidden.</li>
     * </ul>
     *
     * <p>Non-teachers/non-admins (e.g. students) are rejected before the template is looked
     * up, so probing this endpoint cannot reveal which template ids exist.
     *
     * @throws ForbiddenException       if the caller may not act on this template → 403
     * @throws ResourceNotFoundException if the template does not exist → 404
     */
    @Transactional(readOnly = true)
    public void requireCanManageAssessment(UUID userId, UUID templateId) {
        List<String> roles = appUserRoleRepository.findRoleNamesByUserId(userId);
        boolean admin = roles.contains(ROLE_ADMIN);
        boolean teacher = roles.contains(ROLE_TEACHER);

        if (!admin && !teacher) {
            LOG.warn("Forbidden: userId={} is neither TEACHER nor ADMIN", userId);
            throw new ForbiddenException("You are not allowed to perform this action");
        }
        if (admin) {
            return;
        }

        // Teacher: allowed only on a template they own. Platform content (owner null) is
        // admin-only, so a teacher is forbidden there too.
        AssessmentTemplate template = assessmentTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));
        if (!userId.equals(template.getOwnerId())) {
            LOG.warn("Forbidden: teacher userId={} does not own template={}", userId, templateId);
            throw new ForbiddenException("You do not own this assessment");
        }
    }

    /**
     * Authorize managing a tag (rename, delete, attach to / detach from a question).
     *
     * <p>A tag is a teacher's private vocabulary: only its owner may touch it. An admin may
     * touch any. Anyone else — and any teacher who is not the owner — is forbidden.
     *
     * @return the loaded tag, so the caller need not fetch it again
     * @throws ForbiddenException        if the caller may not manage this tag → 403
     * @throws ResourceNotFoundException if the tag does not exist → 404
     */
    @Transactional(readOnly = true)
    public Tag requireCanManageTag(UUID userId, UUID tagId) {
        List<String> roles = appUserRoleRepository.findRoleNamesByUserId(userId);
        boolean admin = roles.contains(ROLE_ADMIN);
        boolean teacher = roles.contains(ROLE_TEACHER);

        if (!admin && !teacher) {
            LOG.warn("Forbidden: userId={} is neither TEACHER nor ADMIN", userId);
            throw new ForbiddenException("You are not allowed to perform this action");
        }

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        if (!admin && !userId.equals(tag.getOwnerId())) {
            LOG.warn("Forbidden: teacher userId={} does not own tag={}", userId, tagId);
            throw new ForbiddenException("You do not own this tag");
        }
        return tag;
    }
}
