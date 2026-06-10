package app.testero.service;

import app.testero.dto.AnswerInput;
import app.testero.dto.SubmissionFeedbackResponse;
import app.testero.dto.SubmissionFeedbackResponse.AnswerResult;
import app.testero.dto.SubmissionHistoryResponse;
import app.testero.dto.SubmissionHistoryResponse.SubmissionSummary;
import app.testero.dto.SubmissionStartResponse;
import app.testero.dto.SubmissionSubmitRequest;
import app.testero.entity.assessment.Assessment;
import app.testero.entity.assessment.Option;
import app.testero.entity.submission.Submission;
import app.testero.entity.submission.UserAnswer;
import app.testero.exception.IllegalSubmissionStateException;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AssessmentRepository;
import app.testero.repository.OptionRepository;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock SubmissionRepository submissionRepository;
    @Mock UserAnswerRepository userAnswerRepository;
    @Mock UserAnswerSelectedOptionRepository userAnswerSelectedOptionRepository;
    @Mock OptionRepository optionRepository;
    @Mock AssessmentRepository assessmentRepository;

    @InjectMocks SubmissionService submissionService;

    @Captor ArgumentCaptor<Submission> submissionCaptor;

    private Assessment defaultAssessment;

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
        defaultAssessment = buildAssessment();
    }

    // ── Stub helpers ───────────────────────────────────────────────

    private Submission startedSubmission() {
        Submission s = new Submission();
        s.setId(SUBMISSION_ID);
        s.setUserId(STUDENT_ID);
        s.setAssessmentId(TEST_ID);
        s.setStartedAt(LocalDateTime.of(2026, 6, 15, 10, 0));
        return s;
    }

    private void stubSubmitFlow() {
        stubSubmitFlow(defaultAssessment);
    }

    private void stubSubmitFlow(Assessment assessment) {
        when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                .thenReturn(Optional.of(startedSubmission()));

        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

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

        when(assessmentRepository.findById(TEST_ID)).thenReturn(Optional.of(assessment));
    }

    // ── DTO builders ───────────────────────────────────────────────

    private static AnswerInput mcAnswer(UUID questionId, String... selectedOptionIds) {
        return new AnswerInput(questionId.toString(), "multiple", null, null,
                List.of(selectedOptionIds));
    }

    private static AnswerInput openAnswer(UUID questionId, String text) {
        return new AnswerInput(questionId.toString(), "open", text, null, List.of());
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
            when(assessmentRepository.findById(TEST_ID)).thenReturn(Optional.of(defaultAssessment));
            when(submissionRepository.findByAssessmentIdAndUserIdAndSubmittedAtIsNull(TEST_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());
            when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> {
                Submission s = inv.getArgument(0);
                s.setId(SUBMISSION_ID);
                return s;
            });

            SubmissionStartResponse response = submissionService.startSubmission(TEST_ID, STUDENT_ID);

            assertThat(response.submissionId()).isEqualTo(SUBMISSION_ID.toString());
            assertThat(response.startedAt()).isNotNull();

            verify(submissionRepository).save(submissionCaptor.capture());
            Submission saved = submissionCaptor.getValue();
            assertThat(saved.getStartedAt()).isNotNull();
            assertThat(saved.getSubmittedAt()).isNull();
            assertThat(saved.getUserId()).isEqualTo(STUDENT_ID);
            assertThat(saved.getAssessmentId()).isEqualTo(TEST_ID);
        }

        @Test
        @DisplayName("idempotent — returns existing unsubmitted submission")
        void idempotent_returnsExisting() {
            Submission existing = startedSubmission();
            when(assessmentRepository.findById(TEST_ID)).thenReturn(Optional.of(defaultAssessment));
            when(submissionRepository.findByAssessmentIdAndUserIdAndSubmittedAtIsNull(TEST_ID, STUDENT_ID))
                    .thenReturn(Optional.of(existing));

            SubmissionStartResponse response = submissionService.startSubmission(TEST_ID, STUDENT_ID);

            assertThat(response.submissionId()).isEqualTo(SUBMISSION_ID.toString());
            verify(submissionRepository, never()).save(any());
        }

        @Test
        @DisplayName("retake — creates new submission when previous one is completed")
        void retake_createsNewWhenPreviousCompleted() {
            when(assessmentRepository.findById(TEST_ID)).thenReturn(Optional.of(defaultAssessment));
            when(submissionRepository.findByAssessmentIdAndUserIdAndSubmittedAtIsNull(TEST_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());
            when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> {
                Submission s = inv.getArgument(0);
                s.setId(UUID.randomUUID());
                return s;
            });

            SubmissionStartResponse response = submissionService.startSubmission(TEST_ID, STUDENT_ID);

            assertThat(response.submissionId()).isNotNull();
            assertThat(response.startedAt()).isNotNull();
            verify(submissionRepository).save(submissionCaptor.capture());
            Submission saved = submissionCaptor.getValue();
            assertThat(saved.getUserId()).isEqualTo(STUDENT_ID);
            assertThat(saved.getAssessmentId()).isEqualTo(TEST_ID);
        }

        @Test
        @DisplayName("assessment not found → ResourceNotFoundException")
        void assessmentNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(assessmentRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> submissionService.startSubmission(unknownId, STUDENT_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
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
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionsFor(Q1_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_C.toString()));

            assertThat(response.answers()).hasSize(1);
            AnswerResult r = response.answers().get(0);
            assertThat(r.isCorrect()).isTrue();
            assertThat(r.type()).isEqualTo("multiple");
            assertThat(r.correctOptionIds()).containsExactly(Q1_OPT_C.toString());
            assertScore(1.0);
        }

        @Test
        @DisplayName("all wrong (Q1 selects wrong option) → isCorrect=false, score=-0.25")
        void allWrong_singleQuestion() {
            stubSubmitFlow();
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionsFor(Q1_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_A.toString()));

            assertThat(response.answers().get(0).isCorrect()).isFalse();
            assertScore(-0.25);
        }

        @Test
        @DisplayName("unanswered (Q1, no options selected) → isCorrect=null, score=0.0")
        void unanswered_singleQuestion() {
            stubSubmitFlow();
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionsFor(Q1_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID)); // no selected options

            assertThat(response.answers().get(0).isCorrect()).isNull();
            assertScore(0.0);
        }

        @Test
        @DisplayName("mixed: Q1 correct + Q2 wrong + Q3 unanswered → score=0.75")
        void mixedCorrectAndWrong() {
            stubSubmitFlow();
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionsFor(Q1_ID, Q2_ID, Q3_ID));

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
            List<Option> twoCorrect = List.of(
                    buildOption(Q1_OPT_C, Q1_ID, "[::-1]", true, false, 3),
                    buildOption(Q1_OPT_D, Q1_ID, "[-1:]", true, false, 4));
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
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
            List<Option> twoCorrect = List.of(
                    buildOption(Q1_OPT_C, Q1_ID, "[::-1]", true, false, 3),
                    buildOption(Q1_OPT_D, Q1_ID, "[-1:]", true, false, 4));
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
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
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionsFor(Q1_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_C.toString(), Q1_OPT_A.toString()));

            assertThat(response.answers().get(0).isCorrect()).isFalse();
            assertScore(-0.25);
        }

        @Test
        @DisplayName("custom scoring: ptsCorrect=2.0, ptsWrong=0.0")
        void differentScoringConfig() {
            Assessment custom = buildAssessment(new BigDecimal("2.0"), BigDecimal.ZERO);
            stubSubmitFlow(custom);
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionsFor(Q1_ID, Q2_ID));

            submit(
                    mcAnswer(Q1_ID, Q1_OPT_C.toString()),  // correct → +2.0
                    mcAnswer(Q2_ID, Q2_OPT_A.toString())); // wrong   → +0.0

            assertScore(2.0);
        }

        @Test
        @DisplayName("negative ptsWrong accumulates: 2 wrong with ptsWrong=-1.0 → score=-2.0")
        void negativePtsWrong_accumulates() {
            Assessment harsh = buildAssessment(new BigDecimal("3.0"), new BigDecimal("-1.0"));
            stubSubmitFlow(harsh);
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionsFor(Q1_ID, Q2_ID));

            submit(
                    mcAnswer(Q1_ID, Q1_OPT_A.toString()),  // wrong → -1.0
                    mcAnswer(Q2_ID, Q2_OPT_A.toString())); // wrong → -1.0

            assertScore(-2.0);
        }

        @Test
        @DisplayName("fallback 'Nessuna' option — treated like any other option in grading")
        void fallbackOption_treatedNormally() {
            stubSubmitFlow();
            Option fallback = buildFallbackOption(true);
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
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
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionsFor(Q1_ID, Q2_ID, Q3_ID, Q4_ID, Q5_ID));

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
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionsFor(Q1_ID, Q2_ID, Q3_ID, Q4_ID, Q5_ID));

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
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionsFor(Q1_ID, Q2_ID, Q3_ID, Q4_ID, Q5_ID));

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
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionsFor(Q1_ID, Q2_ID, Q3_ID, Q4_ID, Q5_ID));

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
            assertThat(r.correctOptionIds()).isEmpty();
            assertScore(0.0);
            verify(optionRepository, never()).findByQuestionIdInAndCorrectTrue(anyList());
        }

        @Test
        @DisplayName("mix of MC (correct) + open → only MC scored, score=1.0")
        void mixOfMultipleAndOpen_onlyMultipleScored() {
            stubSubmitFlow();
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionsFor(Q1_ID));

            SubmissionFeedbackResponse response = submit(
                    mcAnswer(Q1_ID, Q1_OPT_C.toString()),
                    openAnswer(Q_OPEN_ID, "My explanation"));

            assertThat(response.answers()).hasSize(2);
            assertThat(response.answers().get(0).isCorrect()).isTrue();
            assertThat(response.answers().get(1).isCorrect()).isNull();
            assertScore(1.0);
        }

        @Test
        @DisplayName("only open questions → score=0.0, optionRepository never called")
        void onlyOpenQuestions_scoreRemainsZero() {
            stubSubmitFlow();

            submit(
                    openAnswer(Q_OPEN_ID, "Answer 1"),
                    openAnswer(Q1_ID, "Answer 2"));

            assertScore(0.0);
            verify(optionRepository, never()).findByQuestionIdInAndCorrectTrue(anyList());
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
            completed.setSubmittedAt(LocalDateTime.of(2026, 6, 15, 10, 30));
            when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                    .thenReturn(Optional.of(completed));

            assertThatThrownBy(() -> submit(mcAnswer(Q1_ID, Q1_OPT_C.toString())))
                    .isInstanceOf(IllegalSubmissionStateException.class)
                    .hasMessageContaining("Submission already completed");
        }

        @Test
        @DisplayName("assessment not found during scoring → ResourceNotFoundException")
        void assessmentNotFound_duringScoring() {
            when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                    .thenReturn(Optional.of(startedSubmission()));
            lenient().when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userAnswerRepository.saveAll(anyList())).thenAnswer(inv -> {
                List<UserAnswer> list = inv.getArgument(0);
                list.forEach(a -> a.setId(UUID.randomUUID()));
                return list;
            });
            when(assessmentRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> submit(mcAnswer(Q1_ID, Q1_OPT_C.toString())))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Assessment not found");
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
            s.setAssessmentId(TEST_ID);
            s.setStartedAt(LocalDateTime.of(2026, 6, 15, 10, 0));
            s.setSubmittedAt(LocalDateTime.of(2026, 6, 15, 10, 30));
            s.setScore(2.75);
            return s;
        }

        private UserAnswer storedMcAnswer(UUID questionId, Boolean isCorrect) {
            UserAnswer a = new UserAnswer();
            a.setId(UUID.randomUUID());
            a.setSubmissionId(SUBMISSION_ID);
            a.setQuestionId(questionId);
            a.setType("multiple");
            a.setIsCorrect(isCorrect);
            return a;
        }

        private UserAnswer storedOpenAnswer(UUID questionId) {
            UserAnswer a = new UserAnswer();
            a.setId(UUID.randomUUID());
            a.setSubmissionId(SUBMISSION_ID);
            a.setQuestionId(questionId);
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
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionsFor(Q1_ID));

            SubmissionFeedbackResponse response = submissionService.getSubmission(SUBMISSION_ID, STUDENT_ID);

            assertThat(response.id()).isEqualTo(SUBMISSION_ID.toString());
            assertThat(response.userId()).isEqualTo(STUDENT_ID.toString());
            assertThat(response.assessmentId()).isEqualTo(TEST_ID.toString());
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
        @DisplayName("mixed types → MC gets correctOptionIds, open gets empty list")
        void mixedTypes_correctOptionIdsPopulated() {
            when(submissionRepository.findByIdAndUserId(SUBMISSION_ID, STUDENT_ID))
                    .thenReturn(Optional.of(storedSubmission()));
            UserAnswer mc = storedMcAnswer(Q1_ID, true);
            UserAnswer open = storedOpenAnswer(Q_OPEN_ID);
            when(userAnswerRepository.findBySubmissionId(SUBMISSION_ID))
                    .thenReturn(List.of(mc, open));
            when(optionRepository.findByQuestionIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionsFor(Q1_ID));

            SubmissionFeedbackResponse response = submissionService.getSubmission(SUBMISSION_ID, STUDENT_ID);

            assertThat(response.answers()).hasSize(2);
            AnswerResult mcResult = response.answers().get(0);
            AnswerResult openResult = response.answers().get(1);
            assertThat(mcResult.correctOptionIds()).containsExactly(Q1_OPT_C.toString());
            assertThat(openResult.correctOptionIds()).isEmpty();
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
                    .findByUserIdAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(
                            STUDENT_ID))
                    .thenReturn(List.of());

            SubmissionHistoryResponse response =
                    submissionService.getSubmissionHistory(STUDENT_ID);

            assertThat(response.submissions()).isEmpty();
        }

        @Test
        @DisplayName("returns submission with correct counts")
        void returnsSubmissionWithCorrectCounts() {
            Submission sub = startedSubmission();
            sub.setSubmittedAt(LocalDateTime.of(2026, 6, 15, 10, 25));
            sub.setScore(3.25);

            when(submissionRepository
                    .findByUserIdAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(
                            STUDENT_ID))
                    .thenReturn(List.of(sub));
            when(assessmentRepository.findAllById(List.of(TEST_ID)))
                    .thenReturn(List.of(defaultAssessment));

            UserAnswer correct1 = mcAnswer(SUBMISSION_ID, true);
            UserAnswer correct2 = mcAnswer(SUBMISSION_ID, true);
            UserAnswer wrong1 = mcAnswer(SUBMISSION_ID, false);
            UserAnswer unanswered1 = mcAnswer(SUBMISSION_ID, null);

            when(userAnswerRepository
                    .findBySubmissionIdIn(List.of(SUBMISSION_ID)))
                    .thenReturn(List.of(
                            correct1, correct2, wrong1, unanswered1));

            SubmissionHistoryResponse response =
                    submissionService.getSubmissionHistory(STUDENT_ID);

            assertThat(response.submissions()).hasSize(1);
            SubmissionSummary summary = response.submissions().get(0);
            assertThat(summary.id())
                    .isEqualTo(SUBMISSION_ID.toString());
            assertThat(summary.assessmentTitle())
                    .isEqualTo(defaultAssessment.getTitle());
            assertThat(summary.correctCount()).isEqualTo(2);
            assertThat(summary.wrongCount()).isEqualTo(1);
            assertThat(summary.unansweredCount()).isEqualTo(1);
            assertThat(summary.totalQuestions()).isEqualTo(4);
            assertThat(summary.score()).isEqualTo(3.25);
        }

        private UserAnswer mcAnswer(UUID submissionId,
                                     Boolean isCorrect) {
            UserAnswer a = new UserAnswer();
            a.setId(UUID.randomUUID());
            a.setSubmissionId(submissionId);
            a.setQuestionId(UUID.randomUUID());
            a.setType("multiple");
            a.setIsCorrect(isCorrect);
            return a;
        }
    }
}
