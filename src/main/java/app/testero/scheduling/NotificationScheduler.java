package app.testero.scheduling;

import app.testero.entity.assessment.ClassAssessmentAssignment;
import app.testero.entity.notification.DeadlineReminderSent;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.user.NotificationType;
import app.testero.entity.user.StudentProfile;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.ClassAssessmentAssignmentRepository;
import app.testero.repository.DeadlineReminderSentRepository;
import app.testero.repository.StudentProfileRepository;
import app.testero.repository.SubmissionRepository;
import app.testero.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sends DEADLINE_REMINDER notifications to students who have an assessment coming due and
 * have not yet engaged with it.
 *
 * <p>Runs hourly. For each assignment whose deadline is within {@link #REMINDER_WINDOW_HOURS},
 * every student in the class who has no submission for that assessment — and has not already
 * been reminded — gets a single in-app notification. The {@code deadline_reminder_sent} marker
 * makes the de-dup per-student and restart-safe. Delivery is in-app only; honouring an EMAIL
 * channel is separate work (email infrastructure).
 */
@Component
public class NotificationScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationScheduler.class);

    /** How far ahead of a deadline to remind. */
    static final int REMINDER_WINDOW_HOURS = 24;

    private final ClassAssessmentAssignmentRepository assignmentRepository;
    private final AssessmentSnapshotRepository snapshotRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final SubmissionRepository submissionRepository;
    private final DeadlineReminderSentRepository reminderSentRepository;
    private final NotificationService notificationService;

    public NotificationScheduler(ClassAssessmentAssignmentRepository assignmentRepository,
                                 AssessmentSnapshotRepository snapshotRepository,
                                 StudentProfileRepository studentProfileRepository,
                                 SubmissionRepository submissionRepository,
                                 DeadlineReminderSentRepository reminderSentRepository,
                                 NotificationService notificationService) {
        this.assignmentRepository = assignmentRepository;
        this.snapshotRepository = snapshotRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.submissionRepository = submissionRepository;
        this.reminderSentRepository = reminderSentRepository;
        this.notificationService = notificationService;
    }

    /** Top of every hour. */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendDeadlineReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime until = now.plusHours(REMINDER_WINDOW_HOURS);

        List<ClassAssessmentAssignment> dueSoon = assignmentRepository.findWithDeadlineBetween(now, until);
        int sent = 0;
        for (ClassAssessmentAssignment assignment : dueSoon) {
            sent += remindForAssignment(assignment);
        }
        if (sent > 0) {
            LOG.info("Deadline reminders: sent {} notification(s) across {} assignment(s)",
                    sent, dueSoon.size());
        }
    }

    private int remindForAssignment(ClassAssessmentAssignment assignment) {
        UUID snapshotId = assignment.getAssessmentSnapshotId();
        AssessmentSnapshot snapshot = snapshotRepository.findById(snapshotId).orElse(null);
        if (snapshot == null) {
            LOG.warn("Assignment references missing snapshot {}, skipping", snapshotId);
            return 0;
        }

        int sent = 0;
        for (StudentProfile student : studentProfileRepository.findByClassId(assignment.getClassId())) {
            UUID userId = student.getUserId();

            if (submissionRepository.existsByAssessmentSnapshotIdAndUserId(snapshotId, userId)) {
                continue; // already engaged with it
            }
            if (reminderSentRepository.existsByAssessmentSnapshotIdAndUserId(snapshotId, userId)) {
                continue; // already reminded
            }

            notificationService.notify(userId, NotificationType.DEADLINE_REMINDER,
                    "notification.deadline.title", "notification.deadline.message",
                    snapshot.getTitle());
            reminderSentRepository.save(new DeadlineReminderSent(snapshotId, userId));
            sent++;
        }
        return sent;
    }
}
