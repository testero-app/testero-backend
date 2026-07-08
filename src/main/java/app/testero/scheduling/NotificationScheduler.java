package app.testero.scheduling;

import org.springframework.stereotype.Component;

/**
 * TODO: Implement DEADLINE_REMINDER notifications.
 *
 * Planned logic:
 * - Run on a cron schedule (e.g. every hour or daily).
 * - Query class_assessment_assignment for snapshots whose deadline is approaching
 *   (e.g. within 24 hours).
 * - For each student in the class who has NOT yet submitted, send a
 *   NotificationType.DEADLINE_REMINDER in-app notification via NotificationService.notify().
 * - Track which reminders have already been sent to avoid duplicates (e.g. via a
 *   "last_reminder_sent" column or by checking existing notifications).
 */
@Component
public class NotificationScheduler {
    // Implementation deferred until teacher UI is available.
}
