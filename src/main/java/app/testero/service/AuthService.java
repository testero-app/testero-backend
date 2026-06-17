package app.testero.service;

import app.testero.dto.LoginRequest;
import app.testero.dto.LoginResponse;
import app.testero.dto.SetPasswordRequest;
import app.testero.entity.user.AppUser;
import app.testero.entity.user.StudentProfile;
import app.testero.exception.InvalidPasswordException;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AppUserRepository;
import app.testero.repository.StudentProfileRepository;
import app.testero.security.JwtService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(AppUserRepository appUserRepository,
                       StudentProfileRepository studentProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       UserService userService) {
        this.appUserRepository = appUserRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public LoginResponse login(LoginRequest request) {
        String identifier = request.username();
        AppUser user = findByIdentifier(identifier)
                .orElseThrow(() -> {
                    log.warn("Login failed: unknown identifier={}", identifier);
                    return new InvalidCredentialsException();
                });

        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed: wrong password for identifier={}", identifier);
            throw new InvalidCredentialsException();
        }

        boolean mustChange = user.isMustChangePassword();
        boolean expired = user.getPasswordExpiresAt() != null
                && user.getPasswordExpiresAt().isBefore(LocalDateTime.now());

        String token;
        if (mustChange || expired) {
            token = jwtService.generateLimitedToken(user.getId(), user.getUsername());
        } else {
            token = jwtService.generateToken(user.getId(), user.getUsername());
        }

        LoginResponse.UserInfo userInfo = buildUserInfo(user);

        log.info("Login successful: username={}, mustChangePassword={}, passwordExpired={}",
                user.getUsername(), mustChange, expired);
        return new LoginResponse(token, userInfo, mustChange, expired);
    }

    @Transactional
    public LoginResponse setPassword(UUID userId, SetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new InvalidPasswordException("Passwords do not match");
        }
        userService.validatePasswordStrength(request.newPassword());

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        user.setPasswordExpiresAt(null);
        appUserRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getUsername());
        LoginResponse.UserInfo userInfo = buildUserInfo(user);

        log.info("Password set for userId={}", userId);
        return new LoginResponse(token, userInfo, false, false);
    }

    private LoginResponse.UserInfo buildUserInfo(AppUser user) {
        StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
        String className = (profile != null && profile.getUserClass() != null)
                ? profile.getUserClass().getName()
                : "";
        return new LoginResponse.UserInfo(
                user.getId().toString(),
                user.getName(),
                user.getUsername(),
                className
        );
    }

    private Optional<AppUser> findByIdentifier(String identifier) {
        if (identifier.contains("@")) {
            return appUserRepository.findByEmail(identifier);
        }
        return appUserRepository.findByUsername(identifier);
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid credentials");
        }
    }
}
