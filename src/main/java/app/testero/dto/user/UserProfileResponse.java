package app.testero.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record UserProfileResponse(
        String id,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String username,
        @Nullable String email,
        @JsonProperty("class_name") String className,
        String role,
        String language
) {}
