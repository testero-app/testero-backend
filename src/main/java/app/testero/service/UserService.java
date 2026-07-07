package app.testero.service;

import app.testero.dto.ChangePasswordRequest;
import app.testero.dto.NotificationPreferenceDto;
import app.testero.dto.UserProfileResponse;
import app.testero.entity.user.AppRole;
import app.testero.entity.user.AppUser;
import app.testero.entity.user.NotificationChannel;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    /** Default preferences: event → (channel → enabled). */
    private static final Map<NotificationType, Map<NotificationChannel, Boolean>> DEFAULT_PREFS;

    static {
        DEFAULT_PREFS = new HashMap<>();
        for (NotificationType event : NotificationType.values()) {
            Map<NotificationChannel, Boolean> channels = new HashMap<>();
            channels.put(NotificationChannel.IN_APP, true);
            channels.put(NotificationChannel.EMAIL, false);
            DEFAULT_PREFS.put(event, channels);
        }
    }

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
        List<NotificationPreference> saved =
                notificationPreferenceRepository.findByUserId(userId);

        // Build lookup: event+channel → enabled
        Map<String, Boolean> savedMap = new HashMap<>();
        for (NotificationPreference pref : saved) {
            String key = pref.getEvent().name() + "|" + pref.getChannel().name();
            savedMap.put(key, pref.isEnabled());
        }

        // Merge with defaults
        List<NotificationPreferenceDto> result = new ArrayList<>();
        for (var eventEntry : DEFAULT_PREFS.entrySet()) {
            for (var channelEntry : eventEntry.getValue().entrySet()) {
                String key = eventEntry.getKey().name() + "|"
                        + channelEntry.getKey().name();
                boolean enabled = savedMap.getOrDefault(
                        key, channelEntry.getValue());
                result.add(new NotificationPreferenceDto(
                        eventEntry.getKey().name(),
                        channelEntry.getKey().name(),
                        enabled));
            }
        }
        return result;
    }

    @Transactional
    public List<NotificationPreferenceDto> updateNotificationPreferences(
            UUID userId, List<NotificationPreferenceDto> updates) {
        for (NotificationPreferenceDto dto : updates) {
            NotificationType event = NotificationType.valueOf(dto.event());
            NotificationChannel channel =
                    NotificationChannel.valueOf(dto.channel());
            NotificationPreference pref = notificationPreferenceRepository
                    .findByUserIdAndEventAndChannel(userId, event, channel)
                    .orElseGet(() -> {
                        NotificationPreference p = new NotificationPreference();
                        p.setUserId(userId);
                        p.setEvent(event);
                        p.setChannel(channel);
                        return p;
                    });
            pref.setEnabled(dto.enabled());
            notificationPreferenceRepository.save(pref);
        }
        return getNotificationPreferences(userId);
    }

    private String resolveRole(UUID userId) {
        return appUserRoleRepository.findByUserId(userId).stream()
                .findFirst()
                .flatMap(ur -> appRoleRepository.findById(ur.getRoleId()))
                .map(AppRole::getName)
                .orElse("STUDENT");
    }

    void validatePasswordStrength(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidPasswordException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH
                            + " characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new InvalidPasswordException(
                    "Password must contain at least 1 uppercase letter");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new InvalidPasswordException(
                    "Password must contain at least 1 number");
        }
    }
}
