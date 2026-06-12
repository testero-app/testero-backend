package app.testero.scheduling;

import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.submission.Submission;
import app.testero.entity.submission.SubmissionStatus;
import app.testero.event.SubmissionCompletedEvent;
import app.testero.event.SubmissionStartedEvent;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.SubmissionRepository;
import app.testero.service.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class SubmissionAutoCloseScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubmissionAutoCloseScheduler.class);

    private final SubmissionService submissionService;
    private final SubmissionRepository submissionRepository;
    private final AssessmentSnapshotRepository assessmentSnapshotRepository;
    private final TaskScheduler taskScheduler;
    private final ConcurrentHashMap<UUID, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public SubmissionAutoCloseScheduler(SubmissionService submissionService,
                                         SubmissionRepository submissionRepository,
                                         AssessmentSnapshotRepository assessmentSnapshotRepository,
                                         TaskScheduler taskScheduler) {
        this.submissionService = submissionService;
        this.submissionRepository = submissionRepository;
        this.assessmentSnapshotRepository = assessmentSnapshotRepository;
        this.taskScheduler = taskScheduler;
    }

    @EventListener
    public void onSubmissionStarted(SubmissionStartedEvent event) {
        scheduleAutoClose(event.submissionId(), event.autoCloseAt());
    }

    @EventListener
    public void onSubmissionCompleted(SubmissionCompletedEvent event) {
        cancelAutoClose(event.submissionId());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void recoverOpenSubmissions() {
        List<Submission> openSubmissions = submissionRepository.findByStatus(SubmissionStatus.IN_PROGRESS);
        for (Submission submission : openSubmissions) {
            AssessmentSnapshot snapshot = assessmentSnapshotRepository
                    .findById(submission.getAssessmentSnapshotId())
                    .orElse(null);
            if (snapshot == null) {
                log.warn("Snapshot not found for submission {}, closing immediately", submission.getId());
                submissionService.autoCloseSubmission(submission.getId());
                continue;
            }

            Instant autoCloseAt = submission.getStartedAt()
                    .plusMinutes(snapshot.getTimerMinutes())
                    .plusSeconds(1)
                    .atZone(ZoneId.systemDefault())
                    .toInstant();

            if (autoCloseAt.isBefore(Instant.now())) {
                log.info("Submission {} expired during downtime, closing immediately", submission.getId());
                submissionService.autoCloseSubmission(submission.getId());
            } else {
                log.info("Rescheduling auto-close for submission {} at {}", submission.getId(), autoCloseAt);
                scheduleAutoClose(submission.getId(), autoCloseAt);
            }
        }
        if (!openSubmissions.isEmpty()) {
            log.info("Recovery complete: processed {} open submission(s)", openSubmissions.size());
        }
    }

    @Scheduled(cron = "0 1 0 * * *")
    public void nightlyCleanup() {
        List<Submission> openSubmissions = submissionRepository.findByStatus(SubmissionStatus.IN_PROGRESS);
        int closed = 0;
        for (Submission submission : openSubmissions) {
            AssessmentSnapshot snapshot = assessmentSnapshotRepository
                    .findById(submission.getAssessmentSnapshotId())
                    .orElse(null);
            if (snapshot == null) {
                submissionService.autoCloseSubmission(submission.getId());
                closed++;
                continue;
            }

            Instant deadline = submission.getStartedAt()
                    .plusMinutes(snapshot.getTimerMinutes())
                    .plusSeconds(1)
                    .atZone(ZoneId.systemDefault())
                    .toInstant();

            if (deadline.isBefore(Instant.now())) {
                submissionService.autoCloseSubmission(submission.getId());
                closed++;
            }
        }
        if (closed > 0) {
            log.info("Nightly cleanup: closed {} orphaned submission(s)", closed);
        }
    }

    private void scheduleAutoClose(UUID submissionId, Instant fireAt) {
        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            scheduledTasks.remove(submissionId);
            log.info("Auto-closing submission {}", submissionId);
            submissionService.autoCloseSubmission(submissionId);
        }, fireAt);
        scheduledTasks.put(submissionId, future);
    }

    private void cancelAutoClose(UUID submissionId) {
        ScheduledFuture<?> future = scheduledTasks.remove(submissionId);
        if (future != null) {
            future.cancel(false);
        }
    }
}
