package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        String token,
        StudentInfo student
) {
    public record StudentInfo(
            String id,
            String name,
            String username,
            @JsonProperty("class_name") String className
    ) {}
}
