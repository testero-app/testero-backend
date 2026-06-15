package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank @JsonProperty("current_password") String currentPassword,
        @NotBlank @JsonProperty("new_password") String newPassword,
        @NotBlank @JsonProperty("confirm_password") String confirmPassword
) {}
