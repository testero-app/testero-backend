package app.testero.service;

import app.testero.dto.ChangePasswordRequest;
import app.testero.dto.UserProfileResponse;
import app.testero.entity.user.AppRole;
import app.testero.entity.user.AppUser;
import app.testero.entity.user.AppUserRole;
import app.testero.entity.user.StudentProfile;
import app.testero.entity.user.UserClass;
import app.testero.exception.InvalidPasswordException;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AppRoleRepository;
import app.testero.repository.AppUserRepository;
import app.testero.repository.AppUserRoleRepository;
import app.testero.repository.StudentProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock AppUserRepository appUserRepository;
    @Mock StudentProfileRepository studentProfileRepository;
    @Mock AppUserRoleRepository appUserRoleRepository;
    @Mock AppRoleRepository appRoleRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    private static final UUID USER_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");
    private static final UUID ROLE_ID = UUID.fromString("bb000000-0000-0000-0000-000000000001");

    private AppUser buildUser() {
        AppUser user = new AppUser();
        user.setId(USER_ID);
        user.setName("Mario Rossi");
        user.setUsername("mario");
        user.setEmail("mario@test.com");
        user.setPasswordHash("hashed");
        return user;
    }

    @Nested
    @DisplayName("getProfile")
    class GetProfile {

        @Test
        @DisplayName("returns full profile with class and role")
        void fullProfile() {
            AppUser user = buildUser();
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            UserClass uc = new UserClass();
            uc.setName("5A");
            StudentProfile profile = new StudentProfile();
            profile.setUserClass(uc);
            when(studentProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));

            AppUserRole userRole = new AppUserRole();
            userRole.setRoleId(ROLE_ID);
            when(appUserRoleRepository.findByUserId(USER_ID)).thenReturn(List.of(userRole));
            AppRole role = new AppRole();
            role.setName("STUDENT");
            when(appRoleRepository.findById(ROLE_ID)).thenReturn(Optional.of(role));

            UserProfileResponse response = userService.getProfile(USER_ID);

            assertThat(response.id()).isEqualTo(USER_ID.toString());
            assertThat(response.name()).isEqualTo("Mario Rossi");
            assertThat(response.username()).isEqualTo("mario");
            assertThat(response.email()).isEqualTo("mario@test.com");
            assertThat(response.className()).isEqualTo("5A");
            assertThat(response.role()).isEqualTo("STUDENT");
        }

        @Test
        @DisplayName("returns profile without class for teacher")
        void teacherNoProfile() {
            AppUser user = buildUser();
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(studentProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            AppUserRole userRole = new AppUserRole();
            userRole.setRoleId(ROLE_ID);
            when(appUserRoleRepository.findByUserId(USER_ID)).thenReturn(List.of(userRole));
            AppRole role = new AppRole();
            role.setName("TEACHER");
            when(appRoleRepository.findById(ROLE_ID)).thenReturn(Optional.of(role));

            UserProfileResponse response = userService.getProfile(USER_ID);

            assertThat(response.className()).isEmpty();
            assertThat(response.role()).isEqualTo("TEACHER");
        }

        @Test
        @DisplayName("throws 404 when user not found")
        void userNotFound() {
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getProfile(USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("success — password updated")
        void success() {
            AppUser user = buildUser();
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldPass1", "hashed")).thenReturn(true);
            when(passwordEncoder.encode("NewPass1!")).thenReturn("newHash");

            var request = new ChangePasswordRequest("oldPass1", "NewPass1!", "NewPass1!");
            userService.changePassword(USER_ID, request);

            assertThat(user.getPasswordHash()).isEqualTo("newHash");
            verify(appUserRepository).save(user);
        }

        @Test
        @DisplayName("fails when current password is wrong")
        void wrongCurrentPassword() {
            AppUser user = buildUser();
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

            var request = new ChangePasswordRequest("wrong", "NewPass1!", "NewPass1!");

            assertThatThrownBy(() -> userService.changePassword(USER_ID, request))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessage("Current password is incorrect");
            verify(appUserRepository, never()).save(any());
        }

        @Test
        @DisplayName("fails when passwords don't match")
        void passwordsMismatch() {
            AppUser user = buildUser();
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldPass1", "hashed")).thenReturn(true);

            var request = new ChangePasswordRequest("oldPass1", "NewPass1!", "Different1!");

            assertThatThrownBy(() -> userService.changePassword(USER_ID, request))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessage("Passwords do not match");
        }

        @Test
        @DisplayName("fails when new password equals current")
        void sameAsOld() {
            AppUser user = buildUser();
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldPass1", "hashed")).thenReturn(true);

            var request = new ChangePasswordRequest("oldPass1", "oldPass1", "oldPass1");

            assertThatThrownBy(() -> userService.changePassword(USER_ID, request))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessage("New password must be different");
        }

        @Test
        @DisplayName("fails when password too short")
        void tooShort() {
            AppUser user = buildUser();
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldPass1", "hashed")).thenReturn(true);

            var request = new ChangePasswordRequest("oldPass1", "Sh1", "Sh1");

            assertThatThrownBy(() -> userService.changePassword(USER_ID, request))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("at least 8 characters");
        }

        @Test
        @DisplayName("fails when password has no uppercase")
        void noUppercase() {
            AppUser user = buildUser();
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldPass1", "hashed")).thenReturn(true);

            var request = new ChangePasswordRequest("oldPass1", "lowercase1", "lowercase1");

            assertThatThrownBy(() -> userService.changePassword(USER_ID, request))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("1 uppercase");
        }

        @Test
        @DisplayName("fails when password has no digit")
        void noDigit() {
            AppUser user = buildUser();
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldPass1", "hashed")).thenReturn(true);

            var request = new ChangePasswordRequest("oldPass1", "NoDigitHere", "NoDigitHere");

            assertThatThrownBy(() -> userService.changePassword(USER_ID, request))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("1 number");
            verify(passwordEncoder, never()).encode(anyString());
        }
    }
}
