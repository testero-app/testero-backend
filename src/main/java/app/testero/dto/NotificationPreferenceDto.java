package app.testero.dto;

public record NotificationPreferenceDto(
        String event,
        String channel,
        boolean enabled
) {}
