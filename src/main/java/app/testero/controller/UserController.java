package app.testero.controller;

import app.testero.dto.ChangePasswordRequest;
import app.testero.dto.NotificationPreferenceDto;
import app.testero.dto.UpdateNotificationPreferencesRequest;
import app.testero.dto.UserProfileResponse;
import app.testero.security.UserPrincipal;
import app.testero.service.UserService;
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
