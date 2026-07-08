package app.testero.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NotificationItemDto(
        String id,
        String event,
        String title,
        String message,
        boolean read,
        @JsonProperty("created_at") String createdAt) {}
