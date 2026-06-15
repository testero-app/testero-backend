package app.testero.service;

import app.testero.dto.AssessmentConfigResponse;
import app.testero.dto.AssessmentListResponse;
import app.testero.dto.AssessmentQuestionsResponse;
import app.testero.dto.AssessmentQuestionsResponse.QuestionDto;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.snapshot.OptionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshot;
import app.testero.entity.submission.Submission;
import app.testero.entity.submission.SubmissionStatus;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.AssessmentSubjectRepository;
import app.testero.repository.OptionSnapshotRepository;
import app.testero.repository.QuestionSnapshotRepository;
import app.testero.repository.SubjectRepository;
import app.testero.repository.SubmissionRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static app.testero.fixture.PythonCertificationFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock AssessmentSnapshotRepository snapshotRepository;
    @Mock QuestionSnapshotRepository questionSnapshotRepository;
    @Mock OptionSnapshotRepository optionSnapshotRepository;
    @Mock SubmissionRepository submissionRepository;
    @Mock QuestionPrepService questionPrepService;
    @Mock AssessmentSubjectRepository assessmentSubjectRepository;
    @Mock SubjectRepository subjectRepository;

    @InjectMocks AssessmentService assessmentService;

    // ── Helpers ────────────────────────────────────────────────────

    private static final UUID CLASS_ID = UUID.fromString("bb000000-0000-0000-0000-000000000099");

    private QuestionSnapshot buildQuestionSnapshot(UUID id, String type, int position) {
        QuestionSnapshot q = new QuestionSnapshot();
        q.setId(id);
        q.setAssessmentSnapshotId(SNAPSHOT_ID);
        q.setType(type);
        q.setText("Question " + position);
        q.setPosition(position);
        return q;
    }

    private OptionSnapshot buildOpt(UUID id, UUID questionSnapshotId, String text, int position) {
        return buildOptionSnapshot(id, questionSnapshotId, text, false, position);
    }

    // ── getAvailableAssessments ────────────────────────────────────

    @Nested
    @DisplayName("getAvailableAssessments")
    class GetAvailableAssessments {

        @Test
        @DisplayName("returns NOT_STARTED when no submissions exist")
        void notStarted() {
            AssessmentSnapshot s = buildAssessmentSnapshot();
            when(snapshotRepository.findSnapshotsByClassId(eq(CLASS_ID), any()))
                    .thenReturn(new PageImpl<>(List.of(s)));
            when(submissionRepository.findByUserIdAndAssessmentSnapshotIdIn(
                    STUDENT_ID, List.of(SNAPSHOT_ID)))
                    .thenReturn(List.of());

            AssessmentListResponse response = assessmentService.getAvailableAssessments(CLASS_ID, STUDENT_ID, 0, 20);

            assertThat(response.assessments()).hasSize(1);
            var item = response.assessments().get(0);
            assertThat(item.id()).isEqualTo(SNAPSHOT_ID.toString());
            assertThat(item.title()).isEqualTo(TITLE);
            assertThat(item.timerMinutes()).isEqualTo(TIMER_MINUTES);
            assertThat(item.questionsPerAssessment()).isEqualTo(QUESTIONS_PER_ASSESSMENT);
            assertThat(item.difficulty()).isEqualTo(DIFFICULTY.name());
            assertThat(item.status()).isEqualTo("NOT_STARTED");
            assertThat(item.score()).isNull();
        }

        @Test
        @DisplayName("returns IN_PROGRESS when submission is in progress")
        void inProgress() {
            AssessmentSnapshot s = buildAssessmentSnapshot();
            Submission sub = new Submission();
            sub.setId(UUID.randomUUID());
            sub.setUserId(STUDENT_ID);
            sub.setAssessmentSnapshotId(SNAPSHOT_ID);
            sub.setStatus(SubmissionStatus.IN_PROGRESS);
            sub.setStartedAt(LocalDateTime.now());

            when(snapshotRepository.findSnapshotsByClassId(eq(CLASS_ID), any()))
                    .thenReturn(new PageImpl<>(List.of(s)));
            when(submissionRepository.findByUserIdAndAssessmentSnapshotIdIn(
                    STUDENT_ID, List.of(SNAPSHOT_ID)))
                    .thenReturn(List.of(sub));

            AssessmentListResponse response = assessmentService.getAvailableAssessments(CLASS_ID, STUDENT_ID, 0, 20);

            var item = response.assessments().get(0);
            assertThat(item.status()).isEqualTo("IN_PROGRESS");
            assertThat(item.score()).isNull();
        }

        @Test
        @DisplayName("returns COMPLETED with score when submission is submitted")
        void completed() {
            AssessmentSnapshot s = buildAssessmentSnapshot();
            Submission sub = new Submission();
            sub.setId(UUID.randomUUID());
            sub.setUserId(STUDENT_ID);
            sub.setAssessmentSnapshotId(SNAPSHOT_ID);
            sub.setStatus(SubmissionStatus.SUBMITTED);
            sub.setSubmittedAt(LocalDateTime.now());
            sub.setScore(82.0);

            when(snapshotRepository.findSnapshotsByClassId(eq(CLASS_ID), any()))
                    .thenReturn(new PageImpl<>(List.of(s)));
            when(submissionRepository.findByUserIdAndAssessmentSnapshotIdIn(
                    STUDENT_ID, List.of(SNAPSHOT_ID)))
                    .thenReturn(List.of(sub));

            AssessmentListResponse response = assessmentService.getAvailableAssessments(CLASS_ID, STUDENT_ID, 0, 20);

            var item = response.assessments().get(0);
            assertThat(item.status()).isEqualTo("COMPLETED");
            assertThat(item.score()).isEqualTo(82.0);
        }

        @Test
        @DisplayName("returns empty list when no snapshots exist")
        void emptyList() {
            when(snapshotRepository.findSnapshotsByClassId(eq(CLASS_ID), any()))
                    .thenReturn(new PageImpl<>(List.of()));

            AssessmentListResponse response = assessmentService.getAvailableAssessments(CLASS_ID, STUDENT_ID, 0, 20);

            assertThat(response.assessments()).isEmpty();
        }
    }

    // ── getAssessmentConfig ────────────────────────────────────────

    @Nested
    @DisplayName("getAssessmentConfig")
    class GetAssessmentConfig {

        @Test
        @DisplayName("returns correctly mapped config DTO")
        void returnsMappedConfig() {
            AssessmentSnapshot s = buildAssessmentSnapshot();
            when(snapshotRepository.findById(SNAPSHOT_ID)).thenReturn(Optional.of(s));

            AssessmentConfigResponse response =
                    assessmentService.getAssessmentConfig(SNAPSHOT_ID.toString());

            assertThat(response.assessmentId()).isEqualTo(SNAPSHOT_ID.toString());
            assertThat(response.title()).isEqualTo(TITLE);
            assertThat(response.timerMinutes()).isEqualTo(TIMER_MINUTES);
            assertThat(response.questionsPerAssessment()).isEqualTo(QUESTIONS_PER_ASSESSMENT);
            assertThat(response.scoring().pointsPerCorrect()).isEqualTo(PTS_CORRECT.doubleValue());
            assertThat(response.scoring().pointsPerWrong()).isEqualTo(PTS_WRONG.doubleValue());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not found")
        void notFound() {
            UUID unknownId = UUID.randomUUID();
            when(snapshotRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    assessmentService.getAssessmentConfig(unknownId.toString()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── getAssessmentQuestions ──────────────────────────────────────

    @Nested
    @DisplayName("getAssessmentQuestions")
    class GetAssessmentQuestions {

        @Test
        @DisplayName("fetches questions and options, delegates to QuestionPrepService")
        void fullFlow() {
            AssessmentSnapshot s = buildAssessmentSnapshot();
            QuestionSnapshot q1 = buildQuestionSnapshot(Q1_ID, "multiple", 1);
            QuestionSnapshot q2 = buildQuestionSnapshot(Q2_ID, "multiple", 2);

            OptionSnapshot opt1a = buildOpt(Q1_OPT_A, Q1_ID, "Opt A", 1);
            OptionSnapshot opt1b = buildOpt(Q1_OPT_B, Q1_ID, "Opt B", 2);
            OptionSnapshot opt2a = buildOpt(Q2_OPT_A, Q2_ID, "Opt A", 1);

            when(snapshotRepository.findById(SNAPSHOT_ID)).thenReturn(Optional.of(s));
            when(questionSnapshotRepository.findByAssessmentSnapshotIdOrderByPosition(SNAPSHOT_ID))
                    .thenReturn(List.of(q1, q2));
            when(optionSnapshotRepository.findByQuestionSnapshotIdInOrderByPosition(List.of(Q1_ID, Q2_ID)))
                    .thenReturn(List.of(opt1a, opt1b, opt2a));

            // QuestionPrepService returns its input unchanged for this test
            when(questionPrepService.prepare(anyList(), anyInt()))
                    .thenAnswer(inv -> inv.getArgument(0));

            AssessmentQuestionsResponse response =
                    assessmentService.getAssessmentQuestions(SNAPSHOT_ID.toString());

            assertThat(response.assessmentId()).isEqualTo(SNAPSHOT_ID.toString());
            assertThat(response.title()).isEqualTo(TITLE);
            assertThat(response.questions()).hasSize(2);

            // Verify first question has 2 options
            QuestionDto dto1 = response.questions().get(0);
            assertThat(dto1.id()).isEqualTo(Q1_ID.toString());
            assertThat(dto1.options()).hasSize(2);

            // Verify second question has 1 option
            QuestionDto dto2 = response.questions().get(1);
            assertThat(dto2.id()).isEqualTo(Q2_ID.toString());
            assertThat(dto2.options()).hasSize(1);

            // Verify prep service was called
            verify(questionPrepService).prepare(anyList(), eq(QUESTIONS_PER_ASSESSMENT));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when snapshot not found")
        void notFound() {
            UUID unknownId = UUID.randomUUID();
            when(snapshotRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    assessmentService.getAssessmentQuestions(unknownId.toString()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("returns empty questions when snapshot has none")
        void noQuestions() {
            AssessmentSnapshot s = buildAssessmentSnapshot();
            when(snapshotRepository.findById(SNAPSHOT_ID)).thenReturn(Optional.of(s));
            when(questionSnapshotRepository.findByAssessmentSnapshotIdOrderByPosition(SNAPSHOT_ID))
                    .thenReturn(List.of());

            when(questionPrepService.prepare(anyList(), anyInt()))
                    .thenAnswer(inv -> inv.getArgument(0));

            AssessmentQuestionsResponse response =
                    assessmentService.getAssessmentQuestions(SNAPSHOT_ID.toString());

            assertThat(response.questions()).isEmpty();
            assertThat(response.totalQuestions()).isZero();
        }
    }
}
