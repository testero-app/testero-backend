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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock NotificationPreferenceRepository preferenceRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock org.springframework.context.MessageSource messageSource;

    @InjectMocks NotificationService notificationService;

    private static final UUID USER_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");
    private static final UUID NOTIF_ID = UUID.fromString("bb000000-0000-0000-0000-000000000001");

    private static final UUID SOURCE_ID = UUID.fromString("dd000000-0000-0000-0000-000000000001");

    private Notification buildNotification(UUID id, UUID userId, boolean read) {
        Notification n = new Notification();
        n.setId(id);
        n.setUserId(userId);
        n.setEvent(NotificationType.EXAM_RESULT.name());
        n.setTitle("Test title");
        n.setMessage("Test message");
        n.setRead(read);
        n.setSourceEventId(SOURCE_ID);
        // createdAt is set by DB, use reflection or setter for test
        try {
            var field = Notification.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(n, LocalDateTime.of(2026, 6, 15, 10, 0));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return n;
    }

    @Nested
    @DisplayName("notify")
    class Notify {

        private AppUser userWithLanguage(String lang) {
            AppUser u = new AppUser();
            u.setId(USER_ID);
            u.setLanguage(lang);
            return u;
        }

        @Test
        @DisplayName("saves notification with text translated into the recipient's language")
        void defaultEnabled() {
            when(preferenceRepository.findByUserIdAndEventAndChannel(
                    USER_ID, NotificationType.EXAM_RESULT, NotificationChannel.IN_APP))
                    .thenReturn(Optional.empty());
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(userWithLanguage("en")));
            when(messageSource.getMessage(eq("title.key"), any(), eq(Locale.of("en"))))
                    .thenReturn("Exam result available");
            when(messageSource.getMessage(eq("msg.key"), any(), eq(Locale.of("en"))))
                    .thenReturn("Python: 18 points");

            notificationService.notify(USER_ID, NotificationType.EXAM_RESULT,
                    "title.key", "msg.key", "Python", 18);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            Notification saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(USER_ID);
            assertThat(saved.getEvent()).isEqualTo("EXAM_RESULT");
            assertThat(saved.getTitle()).isEqualTo("Exam result available");
            assertThat(saved.getMessage()).isEqualTo("Python: 18 points");
            assertThat(saved.isRead()).isFalse();
            assertThat(saved.getSourceEventId()).isNull();
        }

        @Test
        @DisplayName("saves sourceEventId when provided")
        void withSourceEventId() {
            UUID sourceId = UUID.fromString("dd000000-0000-0000-0000-000000000001");
            when(preferenceRepository.findByUserIdAndEventAndChannel(
                    USER_ID, NotificationType.EXAM_RESULT, NotificationChannel.IN_APP))
                    .thenReturn(Optional.empty());
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(userWithLanguage("it")));
            when(messageSource.getMessage(eq("title.key"), any(), eq(Locale.of("it"))))
                    .thenReturn("Risultato disponibile");
            when(messageSource.getMessage(eq("msg.key"), any(), eq(Locale.of("it"))))
                    .thenReturn("Python: 18 punti");

            notificationService.notify(USER_ID, NotificationType.EXAM_RESULT,
                    "title.key", "msg.key", sourceId, "Python", 18);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat(captor.getValue().getSourceEventId()).isEqualTo(sourceId);
        }

        @Test
        @DisplayName("saves notification when preference is explicitly enabled")
        void explicitlyEnabled() {
            NotificationPreference pref = new NotificationPreference();
            pref.setEnabled(true);
            when(preferenceRepository.findByUserIdAndEventAndChannel(
                    USER_ID, NotificationType.EXAM_RESULT, NotificationChannel.IN_APP))
                    .thenReturn(Optional.of(pref));
            lenient().when(appUserRepository.findById(USER_ID))
                    .thenReturn(Optional.of(userWithLanguage("it")));

            notificationService.notify(USER_ID, NotificationType.EXAM_RESULT, "title.key", "msg.key");

            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("skips notification (and translation) when preference is disabled")
        void disabled() {
            NotificationPreference pref = new NotificationPreference();
            pref.setEnabled(false);
            when(preferenceRepository.findByUserIdAndEventAndChannel(
                    USER_ID, NotificationType.EXAM_RESULT, NotificationChannel.IN_APP))
                    .thenReturn(Optional.of(pref));

            notificationService.notify(USER_ID, NotificationType.EXAM_RESULT, "title.key", "msg.key");

            verify(notificationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getUnread")
    class GetUnread {

        @Test
        @DisplayName("returns unread notifications as DTOs")
        void returnsUnread() {
            Notification n = buildNotification(NOTIF_ID, USER_ID, false);
            when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(USER_ID))
                    .thenReturn(List.of(n));

            List<NotificationItemDto> result = notificationService.getUnread(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(NOTIF_ID.toString());
            assertThat(result.get(0).event()).isEqualTo("EXAM_RESULT");
            assertThat(result.get(0).title()).isEqualTo("Test title");
            assertThat(result.get(0).read()).isFalse();
            assertThat(result.get(0).sourceEventId()).isEqualTo(SOURCE_ID.toString());
        }

        @Test
        @DisplayName("returns empty list when no unread notifications")
        void empty() {
            when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(USER_ID))
                    .thenReturn(List.of());

            assertThat(notificationService.getUnread(USER_ID)).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUnreadCount")
    class GetUnreadCount {

        @Test
        @DisplayName("returns count from repository")
        void returnsCount() {
            when(notificationRepository.countByUserIdAndReadFalse(USER_ID)).thenReturn(3L);

            assertThat(notificationService.getUnreadCount(USER_ID)).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {

        @Test
        @DisplayName("marks notification as read when owned by user")
        void success() {
            Notification n = buildNotification(NOTIF_ID, USER_ID, false);
            when(notificationRepository.findById(NOTIF_ID)).thenReturn(Optional.of(n));

            notificationService.markAsRead(NOTIF_ID, USER_ID);

            assertThat(n.isRead()).isTrue();
            verify(notificationRepository).save(n);
        }

        @Test
        @DisplayName("throws when notification not found")
        void notFound() {
            when(notificationRepository.findById(NOTIF_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.markAsRead(NOTIF_ID, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws when notification belongs to different user")
        void wrongUser() {
            UUID otherUser = UUID.fromString("cc000000-0000-0000-0000-000000000001");
            Notification n = buildNotification(NOTIF_ID, otherUser, false);
            when(notificationRepository.findById(NOTIF_ID)).thenReturn(Optional.of(n));

            assertThatThrownBy(() -> notificationService.markAsRead(NOTIF_ID, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("markAllAsRead")
    class MarkAllAsRead {

        @Test
        @DisplayName("marks all unread notifications as read")
        void success() {
            Notification n1 = buildNotification(NOTIF_ID, USER_ID, false);
            UUID id2 = UUID.fromString("bb000000-0000-0000-0000-000000000002");
            Notification n2 = buildNotification(id2, USER_ID, false);
            when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(USER_ID))
                    .thenReturn(List.of(n1, n2));

            notificationService.markAllAsRead(USER_ID);

            assertThat(n1.isRead()).isTrue();
            assertThat(n2.isRead()).isTrue();
            verify(notificationRepository).saveAll(List.of(n1, n2));
        }
    }
}
