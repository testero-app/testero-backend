package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        String token,
        UserInfo user,
        @JsonProperty("must_change_password") boolean mustChangePassword,
        @JsonProperty("password_expired") boolean passwordExpired
) {
    public record UserInfo(
            String id,
            @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name") String lastName,
            String username,
            @JsonProperty("class_name") String className
    ) {}
}
