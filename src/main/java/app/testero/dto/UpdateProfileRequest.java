package app.testero.dto;

import jakarta.validation.constraints.Pattern;

/**
 * Both fields are optional — a client may update just one. {@code language},
 * when present, must be a supported interface language.
 */
public record UpdateProfileRequest(
        String email,
        @Pattern(regexp = "it|en", message = "Unsupported language")
        String language
) {}
