package app.testero.service;

import app.testero.dto.ChangePasswordRequest;
import app.testero.dto.UserProfileResponse;
import app.testero.entity.user.AppRole;
import app.testero.entity.user.AppUser;
import app.testero.entity.user.AppUserRole;
import app.testero.entity.user.StudentProfile;
import app.testero.exception.InvalidPasswordException;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AppRoleRepository;
import app.testero.repository.AppUserRepository;
import app.testero.repository.AppUserRoleRepository;
import app.testero.repository.StudentProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final AppUserRepository appUserRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AppUserRoleRepository appUserRoleRepository;
    private final AppRoleRepository appRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository appUserRepository,
                       StudentProfileRepository studentProfileRepository,
                       AppUserRoleRepository appUserRoleRepository,
                       AppRoleRepository appRoleRepository,
                       PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.appUserRoleRepository = appUserRoleRepository;
        this.appRoleRepository = appRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String className = studentProfileRepository.findByUserId(userId)
                .map(StudentProfile::getUserClass)
                .map(uc -> uc.getName())
                .orElse("");

        String role = resolveRole(userId);

        return new UserProfileResponse(
                user.getId().toString(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                className,
                role
        );
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new InvalidPasswordException("Passwords do not match");
        }

        if (request.newPassword().equals(request.currentPassword())) {
            throw new InvalidPasswordException("New password must be different");
        }

        validatePasswordStrength(request.newPassword());

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        appUserRepository.save(user);

        log.info("Password changed for userId={}", userId);
    }

    private String resolveRole(UUID userId) {
        List<AppUserRole> userRoles = appUserRoleRepository.findByUserId(userId);
        if (userRoles.isEmpty()) {
            return "";
        }
        return appRoleRepository.findById(userRoles.getFirst().getRoleId())
                .map(AppRole::getName)
                .orElse("");
    }

    public void validatePasswordStrength(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidPasswordException(
                    "Password must be at least 8 characters with 1 uppercase letter and 1 number");
        }
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasUppercase || !hasDigit) {
            throw new InvalidPasswordException(
                    "Password must be at least 8 characters with 1 uppercase letter and 1 number");
        }
    }
}
