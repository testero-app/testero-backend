package app.testero.service;

import app.testero.dto.LoginRequest;
import app.testero.dto.LoginResponse;
import app.testero.dto.SetPasswordRequest;
import app.testero.entity.user.AppUser;
import app.testero.entity.user.StudentProfile;
import app.testero.entity.user.UserClass;
import app.testero.exception.InvalidPasswordException;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AppUserRepository;
import app.testero.repository.StudentProfileRepository;
import app.testero.security.JwtService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AppUserRepository appUserRepository;
    @Mock StudentProfileRepository studentProfileRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock UserService userService;

    @InjectMocks AuthService authService;

    // ── Test data ──────────────────────────────────────────────────

    private static final UUID USER_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");
    private static final String USERNAME = "mario.rossi";
    private static final String EMAIL = "mario@test.com";
    private static final String PASSWORD = "secret123";
    private static final String HASH = "$2a$10$hashedpassword";
    private static final String TOKEN = "jwt.token.here";
    private static final String LIMITED_TOKEN = "jwt.limited.token";

    private AppUser buildUser() {
        AppUser user = new AppUser();
        user.setId(USER_ID);
        user.setName("Mario Rossi");
        user.setUsername(USERNAME);
        user.setEmail(EMAIL);
        user.setPasswordHash(HASH);
        user.setMustChangePassword(false);
        return user;
    }

    private StudentProfile buildProfile(String className) {
        StudentProfile profile = new StudentProfile();
        profile.setUserId(USER_ID);
        if (className != null) {
            UserClass uc = new UserClass();
            uc.setId(UUID.randomUUID());
            uc.setName(className);
            profile.setUserClass(uc);
        }
        return profile;
    }

    // ── Login success ──────────────────────────────────────────────

    @Nested
    @DisplayName("login — success")
    class LoginSuccess {

        @Test
        @DisplayName("returns token and user info with class name")
        void withClassName() {
            AppUser user = buildUser();
            when(appUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);
            when(jwtService.generateToken(USER_ID, USERNAME)).thenReturn(TOKEN);
            when(studentProfileRepository.findByUserId(USER_ID))
                    .thenReturn(Optional.of(buildProfile("5A")));

            LoginResponse response = authService.login(new LoginRequest(USERNAME, PASSWORD));

            assertThat(response.token()).isEqualTo(TOKEN);
            assertThat(response.user().id()).isEqualTo(USER_ID.toString());
            assertThat(response.user().name()).isEqualTo("Mario Rossi");
            assertThat(response.user().username()).isEqualTo(USERNAME);
            assertThat(response.user().className()).isEqualTo("5A");
            assertThat(response.mustChangePassword()).isFalse();
            assertThat(response.passwordExpired()).isFalse();
        }

        @Test
        @DisplayName("returns empty class name when profile has no class")
        void withoutClass() {
            AppUser user = buildUser();
            StudentProfile profile = new StudentProfile();
            profile.setUserId(USER_ID);

            when(appUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);
            when(jwtService.generateToken(USER_ID, USERNAME)).thenReturn(TOKEN);
            when(studentProfileRepository.findByUserId(USER_ID))
                    .thenReturn(Optional.of(profile));

            LoginResponse response = authService.login(new LoginRequest(USERNAME, PASSWORD));

            assertThat(response.user().className()).isEmpty();
        }

        @Test
        @DisplayName("returns empty class name when no student profile exists")
        void withoutProfile() {
            AppUser user = buildUser();
            when(appUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);
            when(jwtService.generateToken(USER_ID, USERNAME)).thenReturn(TOKEN);
            when(studentProfileRepository.findByUserId(USER_ID))
                    .thenReturn(Optional.empty());

            LoginResponse response = authService.login(new LoginRequest(USERNAME, PASSWORD));

            assertThat(response.user().className()).isEmpty();
        }

        @Test
        @DisplayName("calls jwtService.generateToken with correct arguments")
        void verifyTokenGeneration() {
            AppUser user = buildUser();
            when(appUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);
            when(jwtService.generateToken(USER_ID, USERNAME)).thenReturn(TOKEN);
            when(studentProfileRepository.findByUserId(USER_ID))
                    .thenReturn(Optional.empty());

            authService.login(new LoginRequest(USERNAME, PASSWORD));

            verify(jwtService).generateToken(USER_ID, USERNAME);
        }
    }

    // ── Login via email ─────────────────────────────────────────────

    @Nested
    @DisplayName("login — email")
    class LoginViaEmail {

        @Test
        @DisplayName("succeeds with email instead of username")
        void emailSuccess() {
            AppUser user = buildUser();
            when(appUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);
            when(jwtService.generateToken(USER_ID, USERNAME)).thenReturn(TOKEN);
            when(studentProfileRepository.findByUserId(USER_ID))
                    .thenReturn(Optional.empty());

            LoginResponse response = authService.login(new LoginRequest(EMAIL, PASSWORD));

            assertThat(response.token()).isEqualTo(TOKEN);
            assertThat(response.user().username()).isEqualTo(USERNAME);
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when email not found")
        void emailNotFound() {
            when(appUserRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(new LoginRequest("unknown@test.com", PASSWORD)))
                    .isInstanceOf(AuthService.InvalidCredentialsException.class);
        }
    }

    // ── Login failure ──────────────────────────────────────────────

    @Nested
    @DisplayName("login — failure")
    class LoginFailure {

        @Test
        @DisplayName("throws InvalidCredentialsException when user not found")
        void userNotFound() {
            when(appUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(new LoginRequest("unknown", PASSWORD)))
                    .isInstanceOf(AuthService.InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when password is wrong")
        void wrongPassword() {
            AppUser user = buildUser();
            when(appUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong", HASH)).thenReturn(false);

            assertThatThrownBy(() -> authService.login(new LoginRequest(USERNAME, "wrong")))
                    .isInstanceOf(AuthService.InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when password hash is null")
        void nullPasswordHash() {
            AppUser user = buildUser();
            user.setPasswordHash(null);
            when(appUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> authService.login(new LoginRequest(USERNAME, PASSWORD)))
                    .isInstanceOf(AuthService.InvalidCredentialsException.class);
        }
    }

    // ── Login — must change password ───────────────────────────────

    @Nested
    @DisplayName("login — must change password")
    class LoginMustChangePassword {

        @Test
        @DisplayName("returns limited token and mustChangePassword flag when must_change_password is true")
        void mustChangePassword() {
            AppUser user = buildUser();
            user.setMustChangePassword(true);
            when(appUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);
            when(jwtService.generateLimitedToken(USER_ID, USERNAME)).thenReturn(LIMITED_TOKEN);
            when(studentProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            LoginResponse response = authService.login(new LoginRequest(USERNAME, PASSWORD));

            assertThat(response.token()).isEqualTo(LIMITED_TOKEN);
            assertThat(response.mustChangePassword()).isTrue();
            assertThat(response.passwordExpired()).isFalse();
            verify(jwtService).generateLimitedToken(USER_ID, USERNAME);
            verify(jwtService, never()).generateToken(any(), any());
        }

        @Test
        @DisplayName("returns limited token and passwordExpired flag when password is expired")
        void passwordExpired() {
            AppUser user = buildUser();
            user.setPasswordExpiresAt(LocalDateTime.now().minusDays(1));
            when(appUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);
            when(jwtService.generateLimitedToken(USER_ID, USERNAME)).thenReturn(LIMITED_TOKEN);
            when(studentProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            LoginResponse response = authService.login(new LoginRequest(USERNAME, PASSWORD));

            assertThat(response.token()).isEqualTo(LIMITED_TOKEN);
            assertThat(response.mustChangePassword()).isFalse();
            assertThat(response.passwordExpired()).isTrue();
            verify(jwtService).generateLimitedToken(USER_ID, USERNAME);
        }

        @Test
        @DisplayName("returns full token when password_expires_at is in the future")
        void passwordNotExpired() {
            AppUser user = buildUser();
            user.setPasswordExpiresAt(LocalDateTime.now().plusDays(30));
            when(appUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);
            when(jwtService.generateToken(USER_ID, USERNAME)).thenReturn(TOKEN);
            when(studentProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            LoginResponse response = authService.login(new LoginRequest(USERNAME, PASSWORD));

            assertThat(response.token()).isEqualTo(TOKEN);
            assertThat(response.mustChangePassword()).isFalse();
            assertThat(response.passwordExpired()).isFalse();
        }
    }

    // ── Set password ───────────────────────────────────────────────

    @Nested
    @DisplayName("setPassword")
    class SetPassword {

        @Test
        @DisplayName("sets password, clears flags, returns full token")
        void success() {
            AppUser user = buildUser();
            user.setMustChangePassword(true);
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("NewPass1")).thenReturn("$2a$encoded");
            when(jwtService.generateToken(USER_ID, USERNAME)).thenReturn(TOKEN);
            when(studentProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            LoginResponse response = authService.setPassword(
                    USER_ID, new SetPasswordRequest("NewPass1", "NewPass1"));

            assertThat(response.token()).isEqualTo(TOKEN);
            assertThat(response.mustChangePassword()).isFalse();
            assertThat(response.passwordExpired()).isFalse();
            assertThat(user.getPasswordHash()).isEqualTo("$2a$encoded");
            assertThat(user.isMustChangePassword()).isFalse();
            assertThat(user.getPasswordExpiresAt()).isNull();
            verify(userService).validatePasswordStrength("NewPass1");
            verify(appUserRepository).save(user);
        }

        @Test
        @DisplayName("throws when passwords do not match")
        void mismatch() {
            assertThatThrownBy(() -> authService.setPassword(
                    USER_ID, new SetPasswordRequest("NewPass1", "Different1")))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessage("Passwords do not match");
        }

        @Test
        @DisplayName("delegates password strength validation to UserService")
        void weakPassword() {
            doThrow(new InvalidPasswordException("Password must be at least 8 characters with 1 uppercase letter and 1 number"))
                    .when(userService).validatePasswordStrength("weak");

            assertThatThrownBy(() -> authService.setPassword(
                    USER_ID, new SetPasswordRequest("weak", "weak")))
                    .isInstanceOf(InvalidPasswordException.class);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void userNotFound() {
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.setPassword(
                    USER_ID, new SetPasswordRequest("NewPass1", "NewPass1")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
