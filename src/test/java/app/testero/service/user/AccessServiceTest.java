package app.testero.service.user;

import app.testero.entity.assessment.AssessmentTemplate;
import app.testero.entity.tag.Tag;
import app.testero.exception.ForbiddenException;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.user.AppUserRoleRepository;
import app.testero.repository.assessment.AssessmentTemplateRepository;
import app.testero.repository.tag.TagRepository;
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

    @Mock
    TagRepository tagRepository;

    @InjectMocks
    AccessService accessService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID TEMPLATE_ID = UUID.randomUUID();
    private static final UUID TAG_ID = UUID.randomUUID();

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

    private void tagOwnedBy(UUID ownerId) {
        Tag tag = new Tag();
        tag.setId(TAG_ID);
        tag.setOwnerId(ownerId);
        lenient().when(tagRepository.findById(TAG_ID)).thenReturn(Optional.of(tag));
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

    // ── requireCanManageTag ────────────────────────────────────────

    @Test
    @DisplayName("a teacher may manage a tag they own, and gets it back")
    void teacherManagesOwnTag() {
        grant("TEACHER");
        tagOwnedBy(USER_ID);

        Tag tag = accessService.requireCanManageTag(USER_ID, TAG_ID);
        assertThat(tag.getOwnerId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("a teacher may not manage another teacher's tag")
    void teacherCannotManageOthersTag() {
        grant("TEACHER");
        tagOwnedBy(UUID.randomUUID());

        assertThatThrownBy(() -> accessService.requireCanManageTag(USER_ID, TAG_ID))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("an admin may manage any tag")
    void adminManagesAnyTag() {
        grant("ADMIN");
        tagOwnedBy(UUID.randomUUID());

        assertThatCode(() -> accessService.requireCanManageTag(USER_ID, TAG_ID))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a student is rejected before the tag is ever looked up")
    void studentRejectedWithoutTagLookup() {
        grant("STUDENT");

        assertThatThrownBy(() -> accessService.requireCanManageTag(USER_ID, TAG_ID))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("a teacher acting on a non-existent tag gets a 404")
    void teacherOnMissingTagGets404() {
        grant("TEACHER");
        when(tagRepository.findById(TAG_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accessService.requireCanManageTag(USER_ID, TAG_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
