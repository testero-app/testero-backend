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
            String name,
            String username,
            @JsonProperty("class_name") String className
    ) {}
}
