package app.testero.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpdateNotificationPreferencesRequest(
        @NotEmpty @Valid List<NotificationPreferenceDto> preferences
) {}
