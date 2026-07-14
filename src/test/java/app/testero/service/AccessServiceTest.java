package app.testero.service;

import app.testero.entity.assessment.AssessmentTemplate;
import app.testero.exception.ForbiddenException;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AppUserRoleRepository;
import app.testero.repository.AssessmentTemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {

    @Mock
    AppUserRoleRepository appUserRoleRepository;

    @Mock
    AssessmentTemplateRepository assessmentTemplateRepository;

    @InjectMocks
    AccessService accessService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID TEMPLATE_ID = UUID.randomUUID();

    private void grant(String... roles) {
        when(appUserRoleRepository.findRoleNamesByUserId(USER_ID)).thenReturn(List.of(roles));
    }

    private void templateOwnedBy(UUID ownerId) {
        AssessmentTemplate template = new AssessmentTemplate();
        template.setId(TEMPLATE_ID);
        template.setOwnerId(ownerId);
        lenient().when(assessmentTemplateRepository.findById(TEMPLATE_ID))
                .thenReturn(Optional.of(template));
    }

    @Test
    @DisplayName("a user holding one of the required roles is allowed through")
    void allowsMatchingRole() {
        grant("TEACHER");

        assertThatCode(() -> accessService.requireAnyRole(
                USER_ID, AccessService.ROLE_TEACHER, AccessService.ROLE_ADMIN))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("any one of several accepted roles is enough")
    void allowsAnyOfSeveralRoles() {
        grant("ADMIN");

        assertThat(accessService.hasAnyRole(
                USER_ID, AccessService.ROLE_TEACHER, AccessService.ROLE_ADMIN)).isTrue();
    }

    @Test
    @DisplayName("a student is rejected with ForbiddenException, not silently allowed")
    void rejectsStudent() {
        grant("STUDENT");

        assertThatThrownBy(() -> accessService.requireAnyRole(
                USER_ID, AccessService.ROLE_TEACHER, AccessService.ROLE_ADMIN))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("a user with no roles at all is rejected")
    void rejectsUserWithoutRoles() {
        grant();

        assertThat(accessService.hasAnyRole(USER_ID, AccessService.ROLE_TEACHER)).isFalse();
        assertThatThrownBy(() -> accessService.requireAnyRole(USER_ID, AccessService.ROLE_TEACHER))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── requireCanManageAssessment ─────────────────────────────────

    @Test
    @DisplayName("a teacher may manage a template they own")
    void teacherManagesOwnTemplate() {
        grant("TEACHER");
        templateOwnedBy(USER_ID);

        assertThatCode(() -> accessService.requireCanManageAssessment(USER_ID, TEMPLATE_ID))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a teacher may not manage a template owned by someone else")
    void teacherCannotManageOthersTemplate() {
        grant("TEACHER");
        templateOwnedBy(UUID.randomUUID());

        assertThatThrownBy(() -> accessService.requireCanManageAssessment(USER_ID, TEMPLATE_ID))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("a teacher may not manage platform content (owner is null)")
    void teacherCannotManagePlatformContent() {
        grant("TEACHER");
        templateOwnedBy(null);

        assertThatThrownBy(() -> accessService.requireCanManageAssessment(USER_ID, TEMPLATE_ID))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("an admin may manage platform content, without an ownership check")
    void adminManagesPlatformContent() {
        grant("ADMIN");
        templateOwnedBy(null);

        assertThatCode(() -> accessService.requireCanManageAssessment(USER_ID, TEMPLATE_ID))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a student is rejected before the template is ever looked up (no id-existence leak)")
    void studentRejectedWithoutTemplateLookup() {
        grant("STUDENT");

        assertThatThrownBy(() -> accessService.requireCanManageAssessment(USER_ID, TEMPLATE_ID))
                .isInstanceOf(ForbiddenException.class);
        // assessmentTemplateRepository is never consulted — a student cannot probe which
        // template ids exist. (No stubbing on it here, and strict mocks would flag an
        // unexpected call.)
    }

    @Test
    @DisplayName("a teacher acting on a non-existent template gets a 404, not a 403")
    void teacherOnMissingTemplateGets404() {
        grant("TEACHER");
        when(assessmentTemplateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accessService.requireCanManageAssessment(USER_ID, TEMPLATE_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
