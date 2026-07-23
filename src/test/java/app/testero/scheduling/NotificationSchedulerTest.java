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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock ClassAssessmentAssignmentRepository assignmentRepository;
    @Mock AssessmentSnapshotRepository snapshotRepository;
    @Mock StudentProfileRepository studentProfileRepository;
    @Mock SubmissionRepository submissionRepository;
    @Mock DeadlineReminderSentRepository reminderSentRepository;
    @Mock NotificationService notificationService;

    @InjectMocks NotificationScheduler scheduler;

    private static final UUID CLASS_ID = UUID.fromString("c1000000-0000-0000-0000-000000000001");
    private static final UUID SNAPSHOT_ID = UUID.fromString("50000000-0000-0000-0000-000000000001");
    private static final UUID STUDENT_A = UUID.fromString("a0000000-0000-0000-0000-000000000001");
    private static final UUID STUDENT_B = UUID.fromString("b0000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        ClassAssessmentAssignment assignment = new ClassAssessmentAssignment();
        assignment.setClassId(CLASS_ID);
        assignment.setAssessmentSnapshotId(SNAPSHOT_ID);
        lenient().when(assignmentRepository.findWithDeadlineBetween(any(), any()))
                .thenReturn(List.of(assignment));

        AssessmentSnapshot snapshot = new AssessmentSnapshot();
        snapshot.setId(SNAPSHOT_ID);
        snapshot.setTitle("Verifica di Matematica");
        lenient().when(snapshotRepository.findById(SNAPSHOT_ID)).thenReturn(Optional.of(snapshot));
    }

    private StudentProfile student(UUID userId) {
        StudentProfile p = new StudentProfile();
        p.setUserId(userId);
        p.setClassId(CLASS_ID);
        return p;
    }

    @Test
    @DisplayName("reminds a student who has not submitted and records the marker")
    void remindsNonSubmitter() {
        when(studentProfileRepository.findByClassId(CLASS_ID)).thenReturn(List.of(student(STUDENT_A)));
        when(submissionRepository.existsByAssessmentSnapshotIdAndUserId(SNAPSHOT_ID, STUDENT_A)).thenReturn(false);
        when(reminderSentRepository.existsByAssessmentSnapshotIdAndUserId(SNAPSHOT_ID, STUDENT_A)).thenReturn(false);

        scheduler.sendDeadlineReminders();

        verify(notificationService).notify(eq(STUDENT_A), eq(NotificationType.DEADLINE_REMINDER), any(), any(), any());
        verify(reminderSentRepository).save(any(DeadlineReminderSent.class));
    }

    @Test
    @DisplayName("does not remind a student who has already submitted")
    void skipsSubmitter() {
        when(studentProfileRepository.findByClassId(CLASS_ID)).thenReturn(List.of(student(STUDENT_A)));
        when(submissionRepository.existsByAssessmentSnapshotIdAndUserId(SNAPSHOT_ID, STUDENT_A)).thenReturn(true);

        scheduler.sendDeadlineReminders();

        verify(notificationService, never()).notify(any(), any(), any(), any(), any());
        verify(reminderSentRepository, never()).save(any());
    }

    @Test
    @DisplayName("does not remind a student twice")
    void skipsAlreadyReminded() {
        when(studentProfileRepository.findByClassId(CLASS_ID)).thenReturn(List.of(student(STUDENT_A)));
        when(submissionRepository.existsByAssessmentSnapshotIdAndUserId(SNAPSHOT_ID, STUDENT_A)).thenReturn(false);
        when(reminderSentRepository.existsByAssessmentSnapshotIdAndUserId(SNAPSHOT_ID, STUDENT_A)).thenReturn(true);

        scheduler.sendDeadlineReminders();

        verify(notificationService, never()).notify(any(), any(), any(), any(), any());
        verify(reminderSentRepository, never()).save(any());
    }

    @Test
    @DisplayName("reminds only the non-submitter in a mixed class")
    void mixedClass() {
        when(studentProfileRepository.findByClassId(CLASS_ID))
                .thenReturn(List.of(student(STUDENT_A), student(STUDENT_B)));
        // A already submitted, B has not
        when(submissionRepository.existsByAssessmentSnapshotIdAndUserId(SNAPSHOT_ID, STUDENT_A)).thenReturn(true);
        when(submissionRepository.existsByAssessmentSnapshotIdAndUserId(SNAPSHOT_ID, STUDENT_B)).thenReturn(false);
        when(reminderSentRepository.existsByAssessmentSnapshotIdAndUserId(SNAPSHOT_ID, STUDENT_B)).thenReturn(false);

        scheduler.sendDeadlineReminders();

        verify(notificationService, never()).notify(eq(STUDENT_A), any(), any(), any(), any());
        verify(notificationService, times(1)).notify(eq(STUDENT_B), eq(NotificationType.DEADLINE_REMINDER), any(), any(), any());
    }

    @Test
    @DisplayName("skips an assignment whose snapshot is missing, without failing")
    void missingSnapshotSkipped() {
        when(snapshotRepository.findById(SNAPSHOT_ID)).thenReturn(Optional.empty());

        scheduler.sendDeadlineReminders();

        verify(notificationService, never()).notify(any(), any(), any(), any(), any());
        verify(studentProfileRepository, never()).findByClassId(any());
    }
}
