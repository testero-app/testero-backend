package app.testero.service;

import app.testero.dto.ChangePasswordRequest;
import app.testero.dto.NotificationPreferenceDto;
import app.testero.dto.UserProfileResponse;
import app.testero.entity.user.AppRole;
import app.testero.entity.user.AppUser;
import app.testero.entity.user.AppUserRole;
import app.testero.entity.user.NotificationPreference;
import app.testero.entity.user.NotificationType;
import app.testero.entity.user.StudentProfile;
import app.testero.exception.InvalidPasswordException;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AppRoleRepository;
import app.testero.repository.AppUserRepository;
import app.testero.repository.AppUserRoleRepository;
import app.testero.repository.NotificationPreferenceRepository;
import app.testero.repository.StudentProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private static final Map<NotificationType, Boolean> DEFAULT_PREFS = Map.of(
            NotificationType.EXAM_RESULT, true,
            NotificationType.DEADLINE_REMINDER, true,
            NotificationType.PRODUCT_NEWS, false
    );

    private final AppUserRepository appUserRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AppUserRoleRepository appUserRoleRepository;
    private final AppRoleRepository appRoleRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository appUserRepository,
                       StudentProfileRepository studentProfileRepository,
                       AppUserRoleRepository appUserRoleRepository,
                       AppRoleRepository appRoleRepository,
                       NotificationPreferenceRepository notificationPreferenceRepository,
                       PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.appUserRoleRepository = appUserRoleRepository;
        this.appRoleRepository = appRoleRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
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

    @Transactional(readOnly = true)
    public List<NotificationPreferenceDto> getNotificationPreferences(UUID userId) {
        List<NotificationPreference> saved = notificationPreferenceRepository.findByUserId(userId);
        Map<NotificationType, Boolean> merged = new EnumMap<>(DEFAULT_PREFS);
        for (NotificationPreference pref : saved) {
            merged.put(pref.getType(), pref.isEnabled());
        }
        return merged.entrySet().stream()
                .map(e -> new NotificationPreferenceDto(e.getKey().name(), e.getValue()))
                .toList();
    }

    @Transactional
    public List<NotificationPreferenceDto> updateNotificationPreferences(UUID userId,
                                                                         List<NotificationPreferenceDto> updates) {
        for (NotificationPreferenceDto dto : updates) {
            NotificationType type = NotificationType.valueOf(dto.type());
            NotificationPreference pref = notificationPreferenceRepository
                    .findByUserIdAndType(userId, type)
                    .orElseGet(() -> {
                        NotificationPreference p = new NotificationPreference();
                        p.setUserId(userId);
                        p.setType(type);
                        return p;
                    });
            pref.setEnabled(dto.enabled());
            notificationPreferenceRepository.save(pref);
        }
        return getNotificationPreferences(userId);
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
