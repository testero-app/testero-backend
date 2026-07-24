package app.testero.service.notification;

import app.testero.dto.notification.NotificationItemDto;
import app.testero.entity.notification.Notification;
import app.testero.entity.user.AppUser;
import app.testero.entity.user.NotificationChannel;
import app.testero.entity.user.NotificationPreference;
import app.testero.entity.user.NotificationType;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.user.AppUserRepository;
import app.testero.repository.notification.NotificationPreferenceRepository;
import app.testero.repository.notification.NotificationRepository;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final AppUserRepository appUserRepository;
    private final MessageSource messageSource;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationPreferenceRepository preferenceRepository,
                               AppUserRepository appUserRepository,
                               MessageSource messageSource) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.appUserRepository = appUserRepository;
        this.messageSource = messageSource;
    }

    /**
     * Persists an in-app notification, rendered in the recipient's language.
     * Title and message are resolved from {@code titleKey}/{@code messageKey} against the
     * message bundles at creation time (translate-at-send), so the stored text is already
     * localised. No-op if the user has disabled the IN_APP channel for this event.
     *
     * @param messageArgs positional arguments for the message template (e.g. assessment title, score)
     */
    public void notify(UUID userId, NotificationType event,
                       String titleKey, String messageKey, Object... messageArgs) {
        // Default is true if no preference record exists.
        Optional<NotificationPreference> pref = preferenceRepository
                .findByUserIdAndEventAndChannel(userId, event, NotificationChannel.IN_APP);
        boolean enabled = pref.map(NotificationPreference::isEnabled).orElse(true);

        if (!enabled) {
            return;
        }

        Locale locale = localeFor(userId);
        Notification n = new Notification();
        n.setUserId(userId);
        n.setEvent(event.name());
        n.setTitle(messageSource.getMessage(titleKey, null, locale));
        n.setMessage(messageSource.getMessage(messageKey, messageArgs, locale));
        n.setRead(false);
        notificationRepository.save(n);
    }

    /** The recipient's interface language, defaulting to Italian if the user is missing or unset. */
    private Locale localeFor(UUID userId) {
        String lang = appUserRepository.findById(userId)
                .map(AppUser::getLanguage)
                .orElse("it");
        return Locale.of(lang == null ? "it" : lang);
    }

    @Transactional(readOnly = true)
    public List<NotificationItemDto> getUnread(UUID userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!n.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unread =
                notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationItemDto toDto(Notification n) {
        return new NotificationItemDto(
                n.getId().toString(),
                n.getEvent(),
                n.getTitle(),
                n.getMessage(),
                n.isRead(),
                n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
    }
}
