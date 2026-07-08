package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserProfileResponse(
        String id,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String username,
        String email,
        @JsonProperty("class_name") String className,
        String role
) {}
