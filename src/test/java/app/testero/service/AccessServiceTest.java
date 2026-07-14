package app.testero.service;

import app.testero.exception.ForbiddenException;
import app.testero.repository.AppUserRoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {

    @Mock
    AppUserRoleRepository appUserRoleRepository;

    @InjectMocks
    AccessService accessService;

    private static final UUID USER_ID = UUID.randomUUID();

    private void grant(String... roles) {
        when(appUserRoleRepository.findRoleNamesByUserId(USER_ID)).thenReturn(List.of(roles));
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
}
