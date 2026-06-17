package app.testero.controller;

import app.testero.dto.LoginRequest;
import app.testero.dto.LoginResponse;
import app.testero.dto.SetPasswordRequest;
import app.testero.security.UserPrincipal;
import app.testero.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and obtain JWT token")
    @SecurityRequirement(name = "")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/set-password")
    @Operation(summary = "Set new password (first access or expired password)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<LoginResponse> setPassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SetPasswordRequest request) {
        LoginResponse response = authService.setPassword(principal.userId(), request);
        return ResponseEntity.ok(response);
    }
}
