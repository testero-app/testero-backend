package app.testero.service;

import app.testero.dto.SubmissionFeedbackResponse.AnswerResult;
import app.testero.dto.SubmissionFeedbackResponse.SubjectScore;
import app.testero.entity.assessment.Subject;
import app.testero.entity.snapshot.QuestionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshotSubject;
import app.testero.entity.submission.Submission;
import app.testero.entity.submission.UserAnswer;
import app.testero.entity.submission.UserAnswerSelectedOption;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.OptionSnapshotRepository;
import app.testero.repository.QuestionSnapshotRepository;
import app.testero.repository.QuestionSnapshotSubjectRepository;
import app.testero.repository.SubjectRepository;
import app.testero.repository.SubmissionRepository;
import app.testero.repository.UserAnswerRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static app.testero.fixture.PythonCertificationFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoringServiceTest {

    @Mock SubmissionRepository submissionRepository;
    @Mock UserAnswerRepository userAnswerRepository;
    @Mock OptionSnapshotRepository optionSnapshotRepository;
    @Mock AssessmentSnapshotRepository assessmentSnapshotRepository;
    @Mock QuestionSnapshotRepository questionSnapshotRepository;
    @Mock QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository;
    @Mock SubjectRepository subjectRepository;

    @InjectMocks ScoringService scoringService;

    // ── Helpers ────────────────────────────────────────────────────

    private static final UUID SUBJECT_A_ID = UUID.fromString("ab000000-0000-0000-0000-000000000001");
    private static final UUID SUBJECT_B_ID = UUID.fromString("ab000000-0000-0000-0000-000000000002");

    private Submission buildSubmission() {
        Submission s = new Submission();
        s.setId(UUID.fromString("ff000000-0000-0000-0000-000000000001"));
        s.setUserId(STUDENT_ID);
        s.setAssessmentSnapshotId(SNAPSHOT_ID);
        return s;
    }

    private QuestionSnapshot buildQs(UUID id, BigDecimal points) {
        QuestionSnapshot qs = new QuestionSnapshot();
        qs.setId(id);
        qs.setAssessmentSnapshotId(SNAPSHOT_ID);
        qs.setType("multiple");
        qs.setText("Question");
        qs.setPosition(1);
        qs.setPoints(points);
        return qs;
    }

    private UserAnswer buildMcAnswer(UUID id, UUID questionSnapshotId) {
        UserAnswer a = new UserAnswer();
        a.setId(id);
        a.setSubmissionId(buildSubmission().getId());
        a.setQuestionSnapshotId(questionSnapshotId);
        a.setType("multiple");
        return a;
    }

    private UserAnswerSelectedOption buildSelectedOption(UUID answerId, UUID optionSnapshotId) {
        UserAnswerSelectedOption aso = new UserAnswerSelectedOption();
        aso.setAnswerId(answerId);
        aso.setOptionSnapshotId(optionSnapshotId);
        return aso;
    }

    private Subject buildSubject(UUID id, String label) {
        Subject s = new Subject();
        s.setId(id);
        s.setLabel(label);
        return s;
    }

    private QuestionSnapshotSubject buildQss(UUID questionSnapshotId, UUID subjectId, BigDecimal weight) {
        QuestionSnapshotSubject qss = new QuestionSnapshotSubject();
        qss.setQuestionSnapshotId(questionSnapshotId);
        qss.setSubjectId(subjectId);
        qss.setWeight(weight);
        return qss;
    }

    private void stubDefaultSnapshot() {
        when(assessmentSnapshotRepository.findById(SNAPSHOT_ID))
                .thenReturn(Optional.of(buildAssessmentSnapshot()));
    }

    // ── scoreSubmission — per-question points ─────────────────────

    @Nested
    @DisplayName("scoreSubmission — per-question points")
    class ScoreSubmission_PerQuestionPoints {

        private static final UUID ANSWER_ID = UUID.fromString("ee000000-0000-0000-0000-000000000001");

        @Test
        @DisplayName("uses per-question points when set")
        void usesPerQuestionPoints() {
            stubDefaultSnapshot();
            QuestionSnapshot qs = buildQs(Q1_ID, new BigDecimal("2.00"));
            when(questionSnapshotRepository.findByIdIn(anyList())).thenReturn(List.of(qs));
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID));
            lenient().when(questionSnapshotSubjectRepository.findByQuestionSnapshotIdIn(anyList()))
                    .thenReturn(List.of());

            UserAnswer answer = buildMcAnswer(ANSWER_ID, Q1_ID);
            when(userAnswerRepository.save(any(UserAnswer.class))).thenAnswer(inv -> inv.getArgument(0));
            when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

            Submission submission = buildSubmission();
            UserAnswerSelectedOption aso = buildSelectedOption(ANSWER_ID, Q1_OPT_C);

            ScoringService.ScoringResult result = scoringService.scoreSubmission(
                    submission, List.of(answer), List.of(aso));

            assertThat(result.answerResults()).hasSize(1);
            AnswerResult ar = result.answerResults().get(0);
            assertThat(ar.isCorrect()).isTrue();
            assertThat(ar.pointsAwarded()).isEqualTo(2.0);
            assertThat(submission.getScore()).isCloseTo(2.0, within(0.001));
        }

        @Test
        @DisplayName("falls back to assessment ptsCorrect when question points is null")
        void fallsBackToPtsCorrect() {
            stubDefaultSnapshot();
            QuestionSnapshot qs = buildQs(Q1_ID, null); // no per-question points
            when(questionSnapshotRepository.findByIdIn(anyList())).thenReturn(List.of(qs));
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID));
            lenient().when(questionSnapshotSubjectRepository.findByQuestionSnapshotIdIn(anyList()))
                    .thenReturn(List.of());

            UserAnswer answer = buildMcAnswer(ANSWER_ID, Q1_ID);
            when(userAnswerRepository.save(any(UserAnswer.class))).thenAnswer(inv -> inv.getArgument(0));
            when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

            Submission submission = buildSubmission();
            UserAnswerSelectedOption aso = buildSelectedOption(ANSWER_ID, Q1_OPT_C);

            ScoringService.ScoringResult result = scoringService.scoreSubmission(
                    submission, List.of(answer), List.of(aso));

            assertThat(result.answerResults()).hasSize(1);
            AnswerResult ar = result.answerResults().get(0);
            assertThat(ar.isCorrect()).isTrue();
            // Should use PTS_CORRECT (1.00) since question.points is null
            assertThat(ar.pointsAwarded()).isEqualTo(1.0);
            assertThat(submission.getScore()).isCloseTo(1.0, within(0.001));
        }

        @Test
        @DisplayName("uses global ptsWrong for wrong answers regardless of per-question points")
        void usesGlobalPtsWrongForWrongAnswers() {
            stubDefaultSnapshot(); // ptsWrong = -0.25
            QuestionSnapshot qs = buildQs(Q1_ID, new BigDecimal("3.00")); // per-question 3.0
            when(questionSnapshotRepository.findByIdIn(anyList())).thenReturn(List.of(qs));
            when(optionSnapshotRepository.findByQuestionSnapshotIdInAndCorrectTrue(anyList()))
                    .thenReturn(correctOptionSnapshotsFor(Q1_ID));
            lenient().when(questionSnapshotSubjectRepository.findByQuestionSnapshotIdIn(anyList()))
                    .thenReturn(List.of());

            UserAnswer answer = buildMcAnswer(ANSWER_ID, Q1_ID);
            when(userAnswerRepository.save(any(UserAnswer.class))).thenAnswer(inv -> inv.getArgument(0));
            when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

            Submission submission = buildSubmission();
            // Select wrong option
            UserAnswerSelectedOption aso = buildSelectedOption(ANSWER_ID, Q1_OPT_A);

            ScoringService.ScoringResult result = scoringService.scoreSubmission(
                    submission, List.of(answer), List.of(aso));

            assertThat(result.answerResults()).hasSize(1);
            AnswerResult ar = result.answerResults().get(0);
            assertThat(ar.isCorrect()).isFalse();
            // Wrong answer uses ptsWrong (-0.25), NOT per-question points
            assertThat(ar.pointsAwarded()).isEqualTo(-0.25);
            assertThat(submission.getScore()).isCloseTo(-0.25, within(0.001));
        }
    }

    // ── computeSubjectScores ──────────────────────────────────────

    @Nested
    @DisplayName("computeSubjectScores")
    class ComputeSubjectScores {

        @Test
        @DisplayName("computes subject breakdown correctly")
        void computesSubjectBreakdown() {
            stubDefaultSnapshot();

            // Q1 → Subject A (weight 1.0), Q2 → Subject B (weight 1.0)
            QuestionSnapshot qs1 = buildQs(Q1_ID, null); // uses ptsCorrect=1.00
            QuestionSnapshot qs2 = buildQs(Q2_ID, null);
            qs2.setPosition(2);
            when(questionSnapshotRepository.findByIdIn(anyList())).thenReturn(List.of(qs1, qs2));

            QuestionSnapshotSubject qss1 = buildQss(Q1_ID, SUBJECT_A_ID, new BigDecimal("1.00"));
            QuestionSnapshotSubject qss2 = buildQss(Q2_ID, SUBJECT_B_ID, new BigDecimal("1.00"));
            when(questionSnapshotSubjectRepository.findByQuestionSnapshotIdIn(anyList()))
                    .thenReturn(List.of(qss1, qss2));

            when(subjectRepository.findByIdIn(anyList())).thenReturn(List.of(
                    buildSubject(SUBJECT_A_ID, "Variables"),
                    buildSubject(SUBJECT_B_ID, "Loops")
            ));

            // Q1 correct (1.0 pts), Q2 wrong (0 pts set manually)
            UserAnswer a1 = buildMcAnswer(UUID.randomUUID(), Q1_ID);
            a1.setIsCorrect(true);
            a1.setPointsAwarded(1.0);
            UserAnswer a2 = buildMcAnswer(UUID.randomUUID(), Q2_ID);
            a2.setIsCorrect(false);
            a2.setPointsAwarded(-0.25);

            List<SubjectScore> scores = scoringService.computeSubjectScores(
                    List.of(a1, a2), SNAPSHOT_ID);

            assertThat(scores).hasSize(2);

            SubjectScore subjectA = scores.stream()
                    .filter(s -> s.label().equals("Variables")).findFirst().orElseThrow();
            assertThat(subjectA.pointsEarned()).isCloseTo(1.0, within(0.001));
            assertThat(subjectA.pointsAvailable()).isCloseTo(1.0, within(0.001));

            SubjectScore subjectB = scores.stream()
                    .filter(s -> s.label().equals("Loops")).findFirst().orElseThrow();
            assertThat(subjectB.pointsEarned()).isCloseTo(-0.25, within(0.001));
            assertThat(subjectB.pointsAvailable()).isCloseTo(1.0, within(0.001));
        }

        @Test
        @DisplayName("subject breakdown distributes by weight for multi-subject questions")
        void distributesWeightForMultiSubjectQuestions() {
            stubDefaultSnapshot();

            // Q1 with points=2.00, linked to 2 subjects: A (0.60) and B (0.40)
            QuestionSnapshot qs = buildQs(Q1_ID, new BigDecimal("2.00"));
            when(questionSnapshotRepository.findByIdIn(anyList())).thenReturn(List.of(qs));

            QuestionSnapshotSubject qssA = buildQss(Q1_ID, SUBJECT_A_ID, new BigDecimal("0.60"));
            QuestionSnapshotSubject qssB = buildQss(Q1_ID, SUBJECT_B_ID, new BigDecimal("0.40"));
            when(questionSnapshotSubjectRepository.findByQuestionSnapshotIdIn(anyList()))
                    .thenReturn(List.of(qssA, qssB));

            when(subjectRepository.findByIdIn(anyList())).thenReturn(List.of(
                    buildSubject(SUBJECT_A_ID, "Variables"),
                    buildSubject(SUBJECT_B_ID, "Loops")
            ));

            UserAnswer a1 = buildMcAnswer(UUID.randomUUID(), Q1_ID);
            a1.setIsCorrect(true);
            a1.setPointsAwarded(2.0);

            List<SubjectScore> scores = scoringService.computeSubjectScores(
                    List.of(a1), SNAPSHOT_ID);

            assertThat(scores).hasSize(2);

            SubjectScore subjectA = scores.stream()
                    .filter(s -> s.label().equals("Variables")).findFirst().orElseThrow();
            // pointsEarned = 2.0 * 0.60 = 1.20, pointsAvailable = 2.0 * 0.60 = 1.20
            assertThat(subjectA.pointsEarned()).isCloseTo(1.2, within(0.001));
            assertThat(subjectA.pointsAvailable()).isCloseTo(1.2, within(0.001));

            SubjectScore subjectB = scores.stream()
                    .filter(s -> s.label().equals("Loops")).findFirst().orElseThrow();
            // pointsEarned = 2.0 * 0.40 = 0.80, pointsAvailable = 2.0 * 0.40 = 0.80
            assertThat(subjectB.pointsEarned()).isCloseTo(0.8, within(0.001));
            assertThat(subjectB.pointsAvailable()).isCloseTo(0.8, within(0.001));
        }

        @Test
        @DisplayName("questions with no subjects excluded from breakdown")
        void questionsWithNoSubjectsExcluded() {
            stubDefaultSnapshot();

            QuestionSnapshot qs = buildQs(Q1_ID, null);
            when(questionSnapshotRepository.findByIdIn(anyList())).thenReturn(List.of(qs));

            // No QuestionSnapshotSubject entries for Q1
            when(questionSnapshotSubjectRepository.findByQuestionSnapshotIdIn(anyList()))
                    .thenReturn(List.of());

            UserAnswer a1 = buildMcAnswer(UUID.randomUUID(), Q1_ID);
            a1.setIsCorrect(true);
            a1.setPointsAwarded(1.0);

            List<SubjectScore> scores = scoringService.computeSubjectScores(
                    List.of(a1), SNAPSHOT_ID);

            assertThat(scores).isEmpty();
        }
    }
}
