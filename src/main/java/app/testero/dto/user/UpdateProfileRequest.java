package app.testero.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

/**
 * Both fields are optional — a client may update just one. {@code language},
 * when present, must be a supported interface language.
 */
public record UpdateProfileRequest(
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String email,
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Pattern(regexp = "it|en", message = "Unsupported language")
        String language
) {}
