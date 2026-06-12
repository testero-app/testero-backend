package app.testero.event;

import java.time.Instant;
import java.util.UUID;

public record SubmissionStartedEvent(UUID submissionId, Instant autoCloseAt) {
}
