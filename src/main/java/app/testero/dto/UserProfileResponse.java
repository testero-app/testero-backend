package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserProfileResponse(
        String id,
        String name,
        String username,
        String email,
        @JsonProperty("class_name") String className,
        String role
) {}
