package app.testero.controller;

import app.testero.dto.NotificationItemDto;
import app.testero.security.UserPrincipal;
import app.testero.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/unread")
    public List<NotificationItemDto> getUnread(
            @AuthenticationPrincipal UserPrincipal principal) {
        return notificationService.getUnread(principal.userId());
    }

    @GetMapping("/count")
    public Map<String, Long> getCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        return Map.of("count", notificationService.getUnreadCount(principal.userId()));
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable UUID id,
                           @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAsRead(id, principal.userId());
    }

    @PutMapping("/read-all")
    public void markAllAsRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.userId());
    }
}
