package app.testero.dto.notification;

public record NotificationPreferenceDto(
        String event,
        String channel,
        boolean enabled
) {}
