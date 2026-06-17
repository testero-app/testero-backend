package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record SetPasswordRequest(
        @NotBlank @JsonProperty("new_password") String newPassword,
        @NotBlank @JsonProperty("confirm_password") String confirmPassword
) {}
