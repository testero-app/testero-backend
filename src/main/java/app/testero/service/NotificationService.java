package app.testero.service;

import app.testero.dto.NotificationItemDto;
import app.testero.entity.notification.Notification;
import app.testero.entity.user.NotificationChannel;
import app.testero.entity.user.NotificationPreference;
import app.testero.entity.user.NotificationType;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.NotificationPreferenceRepository;
import app.testero.repository.NotificationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationPreferenceRepository preferenceRepository) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
    }

    public void notify(UUID userId, NotificationType event, String title, String message) {
        // Check if user has IN_APP enabled for this event.
        // Default is true if no preference record exists.
        Optional<NotificationPreference> pref = preferenceRepository
                .findByUserIdAndEventAndChannel(userId, event, NotificationChannel.IN_APP);
        boolean enabled = pref.map(NotificationPreference::isEnabled).orElse(true);

        if (!enabled) {
            return;
        }

        Notification n = new Notification();
        n.setUserId(userId);
        n.setEvent(event.name());
        n.setTitle(title);
        n.setMessage(message);
        n.setRead(false);
        notificationRepository.save(n);
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
