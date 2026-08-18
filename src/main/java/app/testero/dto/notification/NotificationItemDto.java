package app.testero.dto.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record NotificationItemDto(
        String id,
        String event,
        String title,
        String message,
        boolean read,
        @JsonProperty("created_at") @Nullable String createdAt,
        @JsonProperty("source_event_id") @Nullable String sourceEventId) {}
