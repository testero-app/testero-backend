package app.testero.controller.user;

import app.testero.dto.user.ChangePasswordRequest;
import app.testero.dto.notification.NotificationPreferenceDto;
import app.testero.dto.notification.UpdateNotificationPreferencesRequest;
import app.testero.dto.user.UpdateProfileRequest;
import app.testero.dto.user.UserProfileResponse;
import app.testero.security.UserPrincipal;
import app.testero.service.user.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.userId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(principal.userId(), request));
    }

    @GetMapping("/me/notifications")
    public ResponseEntity<List<NotificationPreferenceDto>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getNotificationPreferences(principal.userId()));
    }

    @PutMapping("/me/notifications")
    public ResponseEntity<List<NotificationPreferenceDto>> updateNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateNotificationPreferencesRequest request) {
        return ResponseEntity.ok(
                userService.updateNotificationPreferences(principal.userId(), request.preferences()));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.userId(), request);
        return ResponseEntity.noContent().build();
    }
}
