package app.testero.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Create or rename a tag. */
public record TagRequest(
        @NotBlank @Size(max = 50) String name
) {}
