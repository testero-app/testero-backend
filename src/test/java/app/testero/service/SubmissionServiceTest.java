package app.testero.service;

import app.testero.dto.AnswerInput;
import app.testero.dto.SaveAnswerRequest;
import app.testero.dto.SubmissionFeedbackResponse;
import app.testero.dto.SubmissionFeedbackResponse.AnswerResult;
import app.testero.dto.SubmissionHistoryResponse;
import app.testero.dto.SubmissionHistoryResponse.SubmissionSummary;
import app.testero.dto.SubmissionStartResponse;
import app.testero.dto.SubmissionSubmitRequest;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.snapshot.OptionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshot;
import app.testero.entity.submission.Submission;
import app.testero.entity.submission.SubmissionStatus;
import app.testero.entity.submission.UserAnswer;
import app.testero.entity.submission.UserAnswerSelectedOption;
import app.testero.exception.IllegalSubmissionStateException;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.OptionSnapshotRepository;
import app.testero.repository.QuestionSnapshotRepository;
import app.testero.repository.SubmissionRepository;
import app.testero.repository.UserAnswerRepository;
import app.testero.repository.UserAnswerSelectedOptionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static app.testero.fixture.PythonCertificationFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock SubmissionRepository submissionRepository;
    @Mock UserAnswerRepository userAnswerRepository;
    @Mock UserAnswerSelectedOptionRepository userAnswerSelectedOptionRepository;
    @Mock OptionSnapshotRepository optionSnapshotRepository;
    @Mock AssessmentSnapshotRepository assessmentSnapshotRepository;
    @Mock QuestionSnapshotRepository questionSnapshotRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    ScoringService scoringService;
    SubmissionService submissionService;

    @Captor ArgumentCaptor<Submission> submissionCaptor;

    private AssessmentSnapshot defaultSnapshot;

    private static final UUID SUBMISSION_ID = UUID.fromString("ff000000-0000-0000-0000-000000000001");

    // Deterministic answer IDs assigned by the saveAll mock
    private final UUID ANSWER_1_ID = UUID.fromString("ee000000-0000-0000-0000-000000000001");
    private final UUID ANSWER_2_ID = UUID.fromString("ee000000-0000-0000-0000-000000000002");
    private final UUID ANSWER_3_ID = UUID.fromString("ee000000-0000-0000-0000-000000000003");
    private final UUID ANSWER_4_ID = UUID.fromString("ee000000-0000-0000-0000-000000000004");
    private final UUID ANSWER_5_ID = UUID.fromString("ee000000-0000-0000-0000-000000000005");
    private final List<UUID> ANSWER_IDS = List.of(ANSWER_1_ID, ANSWER_2_ID, ANSWER_3_ID, ANSWER_4_ID, ANSWER_5_ID);

    @BeforeEach
    void setUp() {
        defaultSnapshot = buildAssessmentSnapshot();
        scoringService = new ScoringService(
                submissionRepository, userAnswerRepository,
                optionSnapshotRepository, assessmentSnapshotRepository);
        submissionService = new SubmissionService(
                submissionRepository, userAnswerRepository, userAnswerSelectedOptionRepository,
                optionSnapshotRepository, assessmentSnapshotRepository, questionSnapshotRepository,
                scoringService, eventPublisher);
    }

    // ── Stub helpers ───────────────────────────────────────────────

    private Submission startedSubmission() {
        Submission s = new Submission();
        s.setId(SUBMISSION_ID);
        s.setUserId(STUDENT_ID);
        s.setAssessmentSnapshotId(SNAPSHOT_ID);
        s.setStatus(SubmissionStatus.IN_PROGRESS);
        s.setStartedAt(LocalDateTime.of(2026, 6, 15, 10, 0));
        return s;
    }

    private void stubSubmitFlow() {
        stubSubmitFlow(defaultSnapshot);
    }

    private void stubSubmitFlow(AssessmentSnapshot snapshot) {
        when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                .thenReturn(Optional.of(startedSubmission()));

        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

        // Stub for deleting pre-existing incremental answers (empty by default)
        lenient().when(userAnswerRepository.findBySubmissionId(SUBMISSION_ID))
                .thenReturn(List.of());

        AtomicInteger idx = new AtomicInteger(0);
        when(userAnswerRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<UserAnswer> list = inv.getArgument(0);
            for (UserAnswer a : list) {
                a.setId(ANSWER_IDS.get(idx.getAndIncrement()));
            }
            return list;
        });

        lenient().when(userAnswerRepository.save(any(UserAnswer.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(userAnswerSelectedOptionRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        when(assessmentSnapshotRepository.findById(SNAPSHOT_ID)).thenReturn(Optional.of(snapshot));
    }

    // ── DTO builders ───────────────────────────────────────────────

    private static AnswerInput mcAnswer(UUID questionSnapshotId, String... selectedOptionIds) {
        return new AnswerInput(questionSnapshotId.toString(), "multiple", null, null,
                List.of(selectedOptionIds));
    }

    private static AnswerInput openAnswer(UUID questionSnapshotId, String text) {
        return new AnswerInput(questionSnapshotId.toString(), "open", text, null, List.of());
    }

    private static SubmissionSubmitRequest buildRequest(AnswerInput... answers) {
        return new SubmissionSubmitRequest(List.of(answers));
    }

    private SubmissionFeedbackResponse submit(AnswerInput... answers) {
        return submissionService.submitAnswers(SUBMISSION_ID, STUDENT_ID, buildRequest(answers));
    }

    private void assertScore(double expected) {
        verify(submissionRepository, atLeast(1)).save(submissionCaptor.capture());
        List<Submission> captured = submissionCaptor.getAllValues();
        Submission last = captured.get(captured.size() - 1);
        assertThat(last.getScore()).isCloseTo(expected, within(0.001));
    }

    // ════════════════════════════════════════════════════════════════
    // startSubmission
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("startSubmission")
    class StartSubmissionTests {

        @Test
        @DisplayName("creates new submission with server-side startedAt")
        void createsNewSubmission() {
            when(assessmentSnapshotRepository.findById(SNAPSHOT_ID)).thenReturn(Optional.of(defaultSnapshot));
            when(submissionRepository.findByAssessmentSnapshotIdAndUserIdAndStatus(
                    SNAPSHOT_ID, STUDENT_ID, SubmissionStatus.IN_PROGRESS))
                    .thenReturn(Optional.empty());
            when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> {
                Submission s = inv.getArgument(0);
                s.setId(SUBMISSION_ID);
                return s;
            });

            SubmissionStartResponse response = submissionService.startSubmission(SNAPSHOT_ID, STUDENT_ID);

            assertThat(response.submissionId()).isEqualTo(SUBMISSION_ID.toString());
            assertThat(response.startedAt()).isNotNull();

            verify(submissionRepository).save(submissionCaptor.capture());
            Submission saved = submissionCaptor.getValue();
            assertThat(saved.getStartedAt()).isNotNull();
            assertThat(saved.getSubmittedAt()).isNull();
            assertThat(saved.getStatus()).isEqualTo(SubmissionStatus.IN_PROGRESS);
            assertThat(saved.getUserId()).isEqualTo(STUDENT_ID);
            assertThat(saved.getAssessmentSnapshotId()).isEqualTo(SNAPSHOT_ID);

            verify(eventPublisher).publishEvent(any(app.testero.event.SubmissionStartedEvent.class));
        }

        @Test
        @DisplayName("idempotent — returns existing unsubmitted submission")
        void idempotent_returnsExisting() {
            Submission existing = startedSubmission();
            when(assessmentSnapshotRepository.findById(SNAPSHOT_ID)).thenReturn(Optional.of(defaultSnapshot));
            when(submissionRepository.findByAssessmentSnapshotIdAndUserIdAndStatus(
                    SNAPSHOT_ID, STUDENT_ID, SubmissionStatus.IN_PROGRESS))
                    .thenReturn(Optional.of(existing));

            SubmissionStartResponse response = submissionService.startSubmission(SNAPSHOT_ID, STUDENT_ID);

            assertThat(response.submissionId()).isEqualTo(SUBMISSION_ID.toString());
            verify(submissionRepository, never()).save(any());
        }

        @Test
        @DisplayName("retake — creates new submission when previous one is completed")
        void retake_createsNewWhenPreviousCompleted() {
            when(assessmentSnapshotRepository.findById(SNAPSHOT_ID)).thenReturn(Optional.of(defaultSnapshot));
            when(submissionRepository.findByAssessmentSnapshotIdAndUserIdAndStatus(
                    SNAPSHOT_ID, STUDENT_ID, SubmissionStatus.IN_PROGRESS))
                    .thenReturn(Optional.empty());
            when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> {
                Submission s = inv.getArgument(0);
                s.setId(UUID.randomUUID());
                return s;
            });

            SubmissionStartResponse response = submissionService.startSubmission(SNAPSHOT_ID, STUDENT_ID);

            assertThat(response.submissionId()).isNotNull();
            assertThat(response.startedAt()).isNotNull();
            verify(submissionRepository).save(submissionCaptor.capture());
            Submission saved = submissionCaptor.getValue();
            assertThat(saved.getUserId()).isEqualTo(STUDENT_ID);
            assertThat(saved.getAssessmentSnapshotId()).isEqualTo(SNAPSHOT_ID);
        }

        @Test
        @DisplayName("assessment snapshot not found → ResourceNotFoundException")
        void assessmentNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(assessmentSnapshotRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> submissionService.startSubmission(unknownId, STUDENT_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // saveAnswer
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("saveAnswer")
    class SaveAnswerTests {

        private QuestionSnapshot buildQuestionSnapshot() {
            QuestionSnapshot qs = new QuestionSnapshot();
            qs.setId(Q1_ID);
            qs.setAssessmentSnapshotId(SNAPSHOT_ID);
            qs.setType("multiple");
            qs.setText("Question 1");
            qs.setPosition(1);
            return qs;
        }

        @Test
        @DisplayName("creates new answer for a question not yet answered")
        void createsNewAnswer() {
            when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                    .thenReturn(Optional.of(startedSubmission()));
            when(questionSnapshotRepository.findById(Q1_ID))
                    .thenReturn(Optional.of(buildQuestionSnapshot()));
            when(userAnswerRepository.findBySubmissionIdAndQuestionSnapshotId(SUBMISSION_ID, Q1_ID))
                    .thenReturn(Optional.empty());
            when(userAnswerRepository.save(any(UserAnswer.class))).thenAnswer(inv -> {
                UserAnswer a = inv.getArgument(0);
                a.setId(ANSWER_1_ID);
                return a;
            });

            SaveAnswerRequest request = new SaveAnswerRequest(
                    "multiple", null, null, List.of(Q1_OPT_C.toString()));

            submissionService.saveAnswer(SUBMISSION_ID, Q1_ID, STUDENT_ID, request);

            verify(userAnswerRepository).save(any(UserAnswer.class));
            verify(userAnswerSelectedOptionRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("updates existing answer (upsert)")
        void updatesExistingAnswer() {
            when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                    .thenReturn(Optional.of(startedSubmission()));
            when(questionSnapshotRepository.findById(Q1_ID))
                    .thenReturn(Optional.of(buildQuestionSnapshot()));
            UserAnswer existing = new UserAnswer();
            existing.setId(ANSWER_1_ID);
            existing.setSubmissionId(SUBMISSION_ID);
            existing.setQuestionSnapshotId(Q1_ID);
            existing.setType("multiple");
            when(userAnswerRepository.findBySubmissionIdAndQuestionSnapshotId(SUBMISSION_ID, Q1_ID))
                    .thenReturn(Optional.of(existing));
            when(userAnswerRepository.save(any(UserAnswer.class))).thenAnswer(inv -> inv.getArgument(0));

            SaveAnswerRequest request = new SaveAnswerRequest(
                    "multiple", null, null, List.of(Q1_OPT_A.toString()));

            submissionService.saveAnswer(SUBMISSION_ID, Q1_ID, STUDENT_ID, request);

            verify(userAnswerSelectedOptionRepository).deleteByAnswerId(ANSWER_1_ID);
            verify(userAnswerRepository).save(any(UserAnswer.class));
        }

        @Test
        @DisplayName("rejects if submission is not in progress")
        void rejectsIfNotInProgress() {
            Submission completed = startedSubmission();
            completed.setStatus(SubmissionStatus.SUBMITTED);
            completed.setSubmittedAt(LocalDateTime.of(2026, 6, 15, 10, 30));
            when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                    .thenReturn(Optional.of(completed));

            SaveAnswerRequest request = new SaveAnswerRequest("multiple", null, null, List.of());

            assertThatThrownBy(() -> submissionService.saveAnswer(SUBMISSION_ID, Q1_ID, STUDENT_ID, request))
                    .isInstanceOf(IllegalSubmissionStateException.class);
        }

        @Test
        @DisplayName("rejects if question not in assessment snapshot")
        void rejectsIfQuestionNotInSnapshot() {
            when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                    .thenReturn(Optional.of(startedSubmission()));
            // Question exists but belongs to a different snapshot
            QuestionSnapshot wrongSnapshot = buildQuestionSnapshot();
            wrongSnapshot.setAssessmentSnapshotId(UUID.randomUUID());
            when(questionSnapshotRepository.findById(Q1_ID))
                    .thenReturn(Optional.of(wrongSnapshot));

            SaveAnswerRequest request = new SaveAnswerRequest("multiple", null, null, List.of());

            assertThatThrownBy(() -> submissionService.saveAnswer(SUBMISSION_ID, Q1_ID, STUDENT_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("saves empty answer (student navigated away without answering)")
        void savesEmptyAnswer() {
            when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                    .thenReturn(Optional.of(startedSubmission()));
            when(questionSnapshotRepository.findById(Q1_ID))
                    .thenReturn(Optional.of(buildQuestionSnapshot()));
            when(userAnswerRepository.findBySubmissionIdAndQuestionSnapshotId(SUBMISSION_ID, Q1_ID))
                    .thenReturn(Optional.empty());
            when(userAnswerRepository.save(any(UserAnswer.class))).thenAnswer(inv -> {
                UserAnswer a = inv.getArgument(0);
                a.setId(ANSWER_1_ID);
                return a;
            });

            SaveAnswerRequest request = new SaveAnswerRequest("multiple", "", "", List.of());

            submissionService.saveAnswer(SUBMISSION_ID, Q1_ID, STUDENT_ID, request);

            verify(userAnswerRepository).save(any(UserAnswer.class));
            verify(userAnswerSelectedOptionRepository, never()).saveAll(anyList());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // autoCloseSubmission
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("autoCloseSubmission")
    class AutoCloseTests {

        @Test
        @DisplayName("closes in-progress submission and scores partial answers")
        void closesInProgressSubmission() {
            Submission submission = startedSubmission();
            when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(submission));
            when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

            UserAnswer mc = new UserAnswer();
            mc.setId(ANSWER_1_ID);
            mc.setSubmissionId(SUBMISSION_ID);
            mc.setQuestionSnapshotId(Q1_ID);
            mc.setType("multiple");
            when(userAnswerRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(List.of(mc));

            UserAnswerSelectedOption aso = new UserAnswerSelectedOption();
            aso.setAnswerId(ANSWER_1_ID);
            aso.setOptionSnapshotId(Q1_OPT_C);
            when(userAnswerSelectedOptionRepository.findByAnswerIdIn(List.of(ANSWER_1_ID)))
                    .thenReturn(List.of(aso));

            when(assessmentSnapshotRepository.findById(SNAPSHOT_ID)).thenReturn(Optional.of(defaultSnapshot));
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID));
            when(userAnswerRepository.save(any(UserAnswer.class))).thenAnswer(inv -> inv.getArgument(0));

            submissionService.autoCloseSubmission(SUBMISSION_ID);

            verify(submissionRepository).save(submissionCaptor.capture());
            Submission saved = submissionCaptor.getValue();
            assertThat(saved.getStatus()).isEqualTo(SubmissionStatus.AUTO_CLOSED);
            assertThat(saved.getSubmittedAt()).isNotNull();
            assertThat(saved.getScore()).isCloseTo(1.0, within(0.001));
        }

        @Test
        @DisplayName("no-op if submission is already submitted")
        void noOpIfAlreadySubmitted() {
            Submission completed = startedSubmission();
            completed.setStatus(SubmissionStatus.SUBMITTED);
            completed.setSubmittedAt(LocalDateTime.of(2026, 6, 15, 10, 30));
            when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(completed));

            submissionService.autoCloseSubmission(SUBMISSION_ID);

            verify(submissionRepository, never()).save(any());
        }

        @Test
        @DisplayName("closes with zero score when no answers saved")
        void closesWithZeroScoreWhenNoAnswers() {
            Submission submission = startedSubmission();
            when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(submission));
            when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userAnswerRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(List.of());

            submissionService.autoCloseSubmission(SUBMISSION_ID);

            verify(submissionRepository).save(submissionCaptor.capture());
            Submission saved = submissionCaptor.getValue();
            assertThat(saved.getStatus()).isEqualTo(SubmissionStatus.AUTO_CLOSED);
            assertThat(saved.getScore()).isEqualTo(0.0);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // submitAnswers — Multiple Choice
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("submitAnswers — multiple choice scoring")
    class SubmitAnswers_MultipleChoice {

        @Test
        @DisplayName("all correct (Q1) → isCorrect=true, score=1.0")
        void allCorrect_singleQuestion() {
            stubSubmitFlow();
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_C.toString()));

            assertThat(response.answers()).hasSize(1);
            AnswerResult r = response.answers().get(0);
            assertThat(r.isCorrect()).isTrue();
            assertThat(r.type()).isEqualTo("multiple");
            assertThat(r.correctOptionSnapshotIds()).containsExactly(Q1_OPT_C.toString());
            assertScore(1.0);
        }

        @Test
        @DisplayName("submit after incremental save → upserts existing answers without constraint violation")
        void submitAfterIncrementalSave() {
            // Simulate pre-existing answer saved incrementally during the test
            UserAnswer existingAnswer = new UserAnswer();
            existingAnswer.setId(ANSWER_1_ID);
            existingAnswer.setSubmissionId(SUBMISSION_ID);
            existingAnswer.setQuestionSnapshotId(Q1_ID);
            existingAnswer.setType("multiple");
            existingAnswer.setText("");
            existingAnswer.setMotivation("");

            // Override the default stub: return pre-existing answers
            stubSubmitFlow();
            when(userAnswerRepository.findBySubmissionId(SUBMISSION_ID))
                    .thenReturn(List.of(existingAnswer));
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_C.toString()));

            assertThat(response.answers()).hasSize(1);
            assertThat(response.answers().get(0).isCorrect()).isTrue();
            assertScore(1.0);

            // Verify no new UserAnswer was created — the existing one was reused
            verify(userAnswerRepository).saveAll(argThat((List<UserAnswer> list) ->
                    list.size() == 1 && list.get(0).getId().equals(ANSWER_1_ID)));
        }

        @Test
        @DisplayName("all wrong (Q1 selects wrong option) → isCorrect=false, score=-0.25")
        void allWrong_singleQuestion() {
            stubSubmitFlow();
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_A.toString()));

            assertThat(response.answers().get(0).isCorrect()).isFalse();
            assertScore(-0.25);
        }

        @Test
        @DisplayName("unanswered (Q1, no options selected) → isCorrect=null, score=0.0")
        void unanswered_singleQuestion() {
            stubSubmitFlow();
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID)); // no selected options

            assertThat(response.answers().get(0).isCorrect()).isNull();
            assertScore(0.0);
        }

        @Test
        @DisplayName("mixed: Q1 correct + Q2 wrong + Q3 unanswered → score=0.75")
        void mixedCorrectAndWrong() {
            stubSubmitFlow();
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID, Q2_ID, Q3_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_C.toString()),  // correct → +1.0
                    mcAnswer(Q2_ID, Q2_OPT_A.toString()),  // wrong   → -0.25
                    mcAnswer(Q3_ID));                        // unanswered → 0.0

            assertThat(response.answers()).hasSize(3);
            assertThat(response.answers().get(0).isCorrect()).isTrue();
            assertThat(response.answers().get(1).isCorrect()).isFalse();
            assertThat(response.answers().get(2).isCorrect()).isNull();
            assertScore(0.75);
        }

        @Test
        @DisplayName("multiple correct options — all selected → correct")
        void multipleCorrectOptions_allSelected() {
            stubSubmitFlow();
            List<OptionSnapshot> twoCorrect = List.of(
                    buildOptionSnapshot(Q1_OPT_C, Q1_ID, "[::-1]", true, 3),
                    buildOptionSnapshot(Q1_OPT_D, Q1_ID, "[-1:]", true, 4));
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(twoCorrect);

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_C.toString(), Q1_OPT_D.toString()));

            assertThat(response.answers().get(0).isCorrect()).isTrue();
            assertScore(1.0);
        }

        @Test
        @DisplayName("multiple correct options — partial selection → wrong")
        void multipleCorrectOptions_partialSelection() {
            stubSubmitFlow();
            List<OptionSnapshot> twoCorrect = List.of(
                    buildOptionSnapshot(Q1_OPT_C, Q1_ID, "[::-1]", true, 3),
                    buildOptionSnapshot(Q1_OPT_D, Q1_ID, "[-1:]", true, 4));
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(twoCorrect);

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_C.toString())); // only 1 of 2

            assertThat(response.answers().get(0).isCorrect()).isFalse();
            assertScore(-0.25);
        }

        @Test
        @DisplayName("over-selection — correct + extra wrong selected → full penalty")
        void overSelection_correctPlusExtra_isWrong() {
            stubSubmitFlow();
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_C.toString(), Q1_OPT_A.toString()));

            assertThat(response.answers().get(0).isCorrect()).isFalse();
            assertScore(-0.25);
        }

        @Test
        @DisplayName("custom scoring: ptsCorrect=2.0, ptsWrong=0.0")
        void differentScoringConfig() {
            AssessmentSnapshot custom = buildAssessmentSnapshot(new BigDecimal("2.0"), BigDecimal.ZERO);
            stubSubmitFlow(custom);
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID, Q2_ID));

            submit(
                    mcAnswer(Q1_ID, Q1_OPT_C.toString()),  // correct → +2.0
                    mcAnswer(Q2_ID, Q2_OPT_A.toString())); // wrong   → +0.0

            assertScore(2.0);
        }

        @Test
        @DisplayName("negative ptsWrong accumulates: 2 wrong with ptsWrong=-1.0 → score=-2.0")
        void negativePtsWrong_accumulates() {
            AssessmentSnapshot harsh = buildAssessmentSnapshot(new BigDecimal("3.0"), new BigDecimal("-1.0"));
            stubSubmitFlow(harsh);
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID, Q2_ID));

            submit(
                    mcAnswer(Q1_ID, Q1_OPT_A.toString()),  // wrong → -1.0
                    mcAnswer(Q2_ID, Q2_OPT_A.toString())); // wrong → -1.0

            assertScore(-2.0);
        }

        @Test
        @DisplayName("fallback 'Nessuna' option — treated like any other option in grading")
        void fallbackOption_treatedNormally() {
            stubSubmitFlow();
            OptionSnapshot fallback = buildFallbackOptionSnapshot(true);
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(List.of(fallback));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_FALLBACK.toString()));

            assertThat(response.answers().get(0).isCorrect()).isTrue();
            assertScore(1.0);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // submitAnswers — Full Exam Scenarios (happy path)
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("submitAnswers — full exam scenarios")
    class SubmitAnswers_FullExam {

        @Test
        @DisplayName("perfect score — 5/5 correct → score=5.0")
        void allCorrect_fullExam() {
            stubSubmitFlow();
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID, Q2_ID, Q3_ID, Q4_ID, Q5_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_C.toString()),
                    mcAnswer(Q2_ID, Q2_OPT_D.toString()),
                    mcAnswer(Q3_ID, Q3_OPT_B.toString()),
                    mcAnswer(Q4_ID, Q4_OPT_A.toString()),
                    mcAnswer(Q5_ID, Q5_OPT_A.toString()));

            assertThat(response.answers()).hasSize(5);
            assertThat(response.answers()).allSatisfy(r -> {
                assertThat(r.isCorrect()).isTrue();
                assertThat(r.type()).isEqualTo("multiple");
            });
            assertScore(5.0);
        }

        @Test
        @DisplayName("total disaster — 5/5 wrong → score=-1.25")
        void allWrong_fullExam() {
            stubSubmitFlow();
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID, Q2_ID, Q3_ID, Q4_ID, Q5_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_A.toString()),
                    mcAnswer(Q2_ID, Q2_OPT_A.toString()),
                    mcAnswer(Q3_ID, Q3_OPT_A.toString()),
                    mcAnswer(Q4_ID, Q4_OPT_B.toString()),
                    mcAnswer(Q5_ID, Q5_OPT_B.toString()));

            assertThat(response.answers()).hasSize(5);
            assertThat(response.answers()).allSatisfy(r ->
                    assertThat(r.isCorrect()).isFalse());
            assertScore(-1.25);
        }

        @Test
        @DisplayName("blank exam — 5/5 unanswered → score=0.0")
        void allUnanswered_fullExam() {
            stubSubmitFlow();
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID, Q2_ID, Q3_ID, Q4_ID, Q5_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID),
                    mcAnswer(Q2_ID),
                    mcAnswer(Q3_ID),
                    mcAnswer(Q4_ID),
                    mcAnswer(Q5_ID));

            assertThat(response.answers()).hasSize(5);
            assertThat(response.answers()).allSatisfy(r ->
                    assertThat(r.isCorrect()).isNull());
            assertScore(0.0);
        }

        @Test
        @DisplayName("80% correct, 20% wrong — 4 correct + 1 wrong → score=3.75")
        void eightyPercentCorrect_fullExam() {
            stubSubmitFlow();
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID, Q2_ID, Q3_ID, Q4_ID, Q5_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_C.toString()),
                    mcAnswer(Q2_ID, Q2_OPT_D.toString()),
                    mcAnswer(Q3_ID, Q3_OPT_B.toString()),
                    mcAnswer(Q4_ID, Q4_OPT_A.toString()),
                    mcAnswer(Q5_ID, Q5_OPT_B.toString()));

            assertThat(response.answers()).hasSize(5);
            long correctCount = response.answers().stream()
                    .filter(r -> Boolean.TRUE.equals(r.isCorrect())).count();
            long wrongCount = response.answers().stream()
                    .filter(r -> Boolean.FALSE.equals(r.isCorrect())).count();
            assertThat(correctCount).isEqualTo(4);
            assertThat(wrongCount).isEqualTo(1);
            assertScore(3.75);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // submitAnswers — Open & Mixed
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("submitAnswers — open and mixed question types")
    class SubmitAnswers_OpenAndMixed {

        @Test
        @DisplayName("open question → isCorrect=null, no score impact")
        void openQuestion_noScoreImpact() {
            stubSubmitFlow();

            SubmissionFeedbackResponse response = submit(
                    openAnswer(Q_OPEN_ID, "Python uses # for comments"));

            assertThat(response.answers()).hasSize(1);
            AnswerResult r = response.answers().get(0);
            assertThat(r.type()).isEqualTo("open");
            assertThat(r.isCorrect()).isNull();
            assertThat(r.correctOptionSnapshotIds()).isEmpty();
            assertScore(0.0);
            verify(optionSnapshotRepository, never()).findByQuestionSnapshotIdInAndCorrectTrue(anyList());
        }

        @Test
        @DisplayName("mix of MC (correct) + open → only MC scored, score=1.0")
        void mixOfMultipleAndOpen_onlyMultipleScored() {
            stubSubmitFlow();
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_C.toString()),
                    openAnswer(Q_OPEN_ID, "My explanation"));

            assertThat(response.answers()).hasSize(2);
            assertThat(response.answers().get(0).isCorrect()).isTrue();
            assertThat(response.answers().get(1).isCorrect()).isNull();
            assertScore(1.0);
        }

        @Test
        @DisplayName("only open questions → score=0.0, optionSnapshotRepository never called")
        void onlyOpenQuestions_scoreRemainsZero() {
            stubSubmitFlow();

            submit(
                    openAnswer(Q_OPEN_ID, "Answer 1"),
                    openAnswer(Q1_ID, "Answer 2"));

            assertScore(0.0);
            verify(optionSnapshotRepository, never()).findByQuestionSnapshotIdInAndCorrectTrue(anyList());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // submitAnswers — Error cases
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("submitAnswers — error cases")
    class SubmitAnswers_ErrorCases {

        @Test
        @DisplayName("submission not found → ResourceNotFoundException")
        void submissionNotFound() {
            when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> submit(mcAnswer(Q1_ID, Q1_OPT_C.toString())))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Submission not found");
        }

        @Test
        @DisplayName("already submitted → IllegalSubmissionStateException")
        void alreadySubmitted() {
            Submission completed = startedSubmission();
            completed.setStatus(SubmissionStatus.SUBMITTED);
            completed.setSubmittedAt(LocalDateTime.of(2026, 6, 15, 10, 30));
            when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                    .thenReturn(Optional.of(completed));

            assertThatThrownBy(() -> submit(mcAnswer(Q1_ID, Q1_OPT_C.toString())))
                    .isInstanceOf(IllegalSubmissionStateException.class)
                    .hasMessageContaining("Submission already completed");
        }

        @Test
        @DisplayName("assessment snapshot not found during scoring → ResourceNotFoundException")
        void assessmentNotFound_duringScoring() {
            Submission sub = startedSubmission();
            when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                    .thenReturn(Optional.of(sub));
            lenient().when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userAnswerRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(List.of());
            when(userAnswerRepository.saveAll(anyList())).thenAnswer(inv -> {
                List<UserAnswer> list = inv.getArgument(0);
                list.forEach(a -> a.setId(UUID.randomUUID()));
                return list;
            });
            when(assessmentSnapshotRepository.findById(SNAPSHOT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> submit(mcAnswer(Q1_ID, Q1_OPT_C.toString())))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Assessment snapshot not found");
        }
    }

    // ════════════════════════════════════════════════════════════════
    // submitAnswers — submittedAt set server-side
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("submitAnswers — server-side timestamps")
    class SubmitAnswers_Timestamps {

        @Test
        @DisplayName("submittedAt is set by the server, not the client")
        void submittedAtSetServerSide() {
            stubSubmitFlow();

            SubmissionFeedbackResponse response = submit(
                    openAnswer(Q_OPEN_ID, "Answer"));

            assertThat(response.submittedAt()).isNotNull();
            verify(submissionRepository, atLeast(1)).save(submissionCaptor.capture());
            Submission saved = submissionCaptor.getAllValues().get(0);
            assertThat(saved.getSubmittedAt()).isNotNull();
            assertThat(saved.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // getSubmission
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getSubmission")
    class GetSubmissionTests {

        private Submission storedSubmission() {
            Submission s = new Submission();
            s.setId(SUBMISSION_ID);
            s.setUserId(STUDENT_ID);
            s.setAssessmentSnapshotId(SNAPSHOT_ID);
            s.setStatus(SubmissionStatus.SUBMITTED);
            s.setStartedAt(LocalDateTime.of(2026, 6, 15, 10, 0));
            s.setSubmittedAt(LocalDateTime.of(2026, 6, 15, 10, 30));
            s.setScore(2.75);
            return s;
        }

        private UserAnswer storedMcAnswer(UUID questionSnapshotId, Boolean isCorrect) {
            UserAnswer a = new UserAnswer();
            a.setId(UUID.randomUUID());
            a.setSubmissionId(SUBMISSION_ID);
            a.setQuestionSnapshotId(questionSnapshotId);
            a.setType("multiple");
            a.setIsCorrect(isCorrect);
            return a;
        }

        private UserAnswer storedOpenAnswer(UUID questionSnapshotId) {
            UserAnswer a = new UserAnswer();
            a.setId(UUID.randomUUID());
            a.setSubmissionId(SUBMISSION_ID);
            a.setQuestionSnapshotId(questionSnapshotId);
            a.setType("open");
            a.setText("My answer");
            a.setIsCorrect(null);
            return a;
        }

        @Test
        @DisplayName("found → returns correctly mapped response")
        void found_returnsResponse() {
            when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                    .thenReturn(Optional.of(storedSubmission()));
            UserAnswer mc = storedMcAnswer(Q1_ID, true);
            when(userAnswerRepository.findBySubmissionId(SUBMISSION_ID))
                    .thenReturn(List.of(mc));
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID));

            SubmissionFeedbackResponse response = submissionService.getSubmission(SUBMISSION_ID, STUDENT_ID);

            assertThat(response.id()).isEqualTo(SUBMISSION_ID.toString());
            assertThat(response.userId()).isEqualTo(STUDENT_ID.toString());
            assertThat(response.assessmentSnapshotId()).isEqualTo(SNAPSHOT_ID.toString());
            assertThat(response.startedAt()).isNotNull();
            assertThat(response.submittedAt()).isNotNull();
            assertThat(response.answers()).hasSize(1);
            assertThat(response.answers().get(0).isCorrect()).isTrue();
        }

        @Test
        @DisplayName("not found → ResourceNotFoundException")
        void notFound_throwsResourceNotFoundException() {
            when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> submissionService.getSubmission(SUBMISSION_ID, STUDENT_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Submission not found");
        }

        @Test
        @DisplayName("mixed types → MC gets correctOptionSnapshotIds, open gets empty list")
        void mixedTypes_correctOptionIdsPopulated() {
            when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                    .thenReturn(Optional.of(storedSubmission()));
            UserAnswer mc = storedMcAnswer(Q1_ID, true);
            UserAnswer open = storedOpenAnswer(Q_OPEN_ID);
            when(userAnswerRepository.findBySubmissionId(SUBMISSION_ID))
                    .thenReturn(List.of(mc, open));
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID));

            SubmissionFeedbackResponse response = submissionService.getSubmission(SUBMISSION_ID, STUDENT_ID);

            assertThat(response.answers()).hasSize(2);
            AnswerResult mcResult = response.answers().get(0);
            AnswerResult openResult = response.answers().get(1);
            assertThat(mcResult.correctOptionSnapshotIds()).containsExactly(Q1_OPT_C.toString());
            assertThat(openResult.correctOptionSnapshotIds()).isEmpty();
        }
    }

    // ════════════════════════════════════════════════════════════════
    // getSubmissionHistory
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getSubmissionHistory")
    class GetSubmissionHistoryTests {

        @Test
        @DisplayName("returns empty list when no completed submissions")
        void returnsEmptyListWhenNoSubmissions() {
            when(submissionRepository
                    .findByUserIdAndStatusInOrderBySubmittedAtDesc(
                            eq(STUDENT_ID),
                            eq(List.of(SubmissionStatus.SUBMITTED, SubmissionStatus.AUTO_CLOSED)),
                            any()))
                    .thenReturn(new PageImpl<>(List.of()));

            SubmissionHistoryResponse response =
                    submissionService.getSubmissionHistory(STUDENT_ID, 0, 20);

            assertThat(response.submissions()).isEmpty();
        }

        @Test
        @DisplayName("returns submission with correct counts")
        void returnsSubmissionWithCorrectCounts() {
            Submission sub = startedSubmission();
            sub.setStatus(SubmissionStatus.SUBMITTED);
            sub.setSubmittedAt(LocalDateTime.of(2026, 6, 15, 10, 25));
            sub.setScore(3.25);

            when(submissionRepository
                    .findByUserIdAndStatusInOrderBySubmittedAtDesc(
                            eq(STUDENT_ID),
                            eq(List.of(SubmissionStatus.SUBMITTED, SubmissionStatus.AUTO_CLOSED)),
                            any()))
                    .thenReturn(new PageImpl<>(List.of(sub)));
            when(assessmentSnapshotRepository.findAllById(List.of(SNAPSHOT_ID)))
                    .thenReturn(List.of(defaultSnapshot));

            UserAnswer correct1 = mcAnswer(SUBMISSION_ID, true);
            UserAnswer correct2 = mcAnswer(SUBMISSION_ID, true);
            UserAnswer wrong1 = mcAnswer(SUBMISSION_ID, false);
            UserAnswer unanswered1 = mcAnswer(SUBMISSION_ID, null);

            when(userAnswerRepository
                    .findBySubmissionIdIn(List.of(SUBMISSION_ID)))
                    .thenReturn(List.of(
                            correct1, correct2, wrong1, unanswered1));

            SubmissionHistoryResponse response =
                    submissionService.getSubmissionHistory(STUDENT_ID, 0, 20);

            assertThat(response.submissions()).hasSize(1);
            SubmissionSummary summary = response.submissions().get(0);
            assertThat(summary.id())
                    .isEqualTo(SUBMISSION_ID.toString());
            assertThat(summary.assessmentSnapshotId())
                    .isEqualTo(SNAPSHOT_ID.toString());
            assertThat(summary.assessmentTitle())
                    .isEqualTo(defaultSnapshot.getTitle());
            assertThat(summary.correctCount()).isEqualTo(2);
            assertThat(summary.wrongCount()).isEqualTo(1);
            assertThat(summary.unansweredCount()).isEqualTo(1);
            assertThat(summary.totalQuestions()).isEqualTo(4);
            assertThat(summary.score()).isEqualTo(3.25);
            assertThat(summary.maxScore()).isEqualTo(5.0);
        }

        private UserAnswer mcAnswer(UUID submissionId,
                                     Boolean isCorrect) {
            UserAnswer a = new UserAnswer();
            a.setId(UUID.randomUUID());
            a.setSubmissionId(submissionId);
            a.setQuestionSnapshotId(UUID.randomUUID());
            a.setType("multiple");
            a.setIsCorrect(isCorrect);
            return a;
        }
    }
}
