package app.testero.service;

import app.testero.entity.assessment.Assessment;
import app.testero.entity.assessment.Option;
import app.testero.entity.assessment.Question;
import app.testero.entity.assessment.QuestionSubject;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.snapshot.QuestionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshotSubject;
import app.testero.repository.AssessmentRepository;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.OptionRepository;
import app.testero.repository.OptionSnapshotRepository;
import app.testero.repository.QuestionRepository;
import app.testero.repository.QuestionSnapshotRepository;
import app.testero.repository.QuestionSubjectRepository;
import app.testero.repository.QuestionSnapshotSubjectRepository;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static app.testero.fixture.PythonCertificationFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnapshotServiceTest {

    @Mock AssessmentRepository assessmentRepository;
    @Mock QuestionRepository questionRepository;
    @Mock OptionRepository optionRepository;
    @Mock QuestionSubjectRepository questionSubjectRepository;
    @Mock AssessmentSnapshotRepository snapshotRepository;
    @Mock QuestionSnapshotRepository questionSnapshotRepository;
    @Mock OptionSnapshotRepository optionSnapshotRepository;
    @Mock QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository;

    @InjectMocks SnapshotService snapshotService;

    @Captor ArgumentCaptor<QuestionSnapshotSubject> qssCaptor;

    // ── Helpers ────────────────────────────────────────────────────

    private static final UUID SUBJECT_OOP = UUID.fromString("ee000000-0000-0000-0000-000000000001");
    private static final UUID SUBJECT_FLOW = UUID.fromString("ee000000-0000-0000-0000-000000000002");

    private static Question buildQuestion(UUID id, int position) {
        Question q = new Question();
        q.setId(id);
        q.setAssessmentId(TEST_ID);
        q.setType("multiple");
        q.setText("Question " + position);
        q.setPosition(position);
        return q;
    }

    private static QuestionSubject buildQuestionSubject(UUID questionId, UUID subjectId,
                                                        String weight) {
        QuestionSubject qs = new QuestionSubject();
        qs.setQuestionId(questionId);
        qs.setSubjectId(subjectId);
        qs.setWeight(new BigDecimal(weight));
        return qs;
    }

    private void stubPublishSnapshot(List<Question> questions, List<Option> options,
                                     List<QuestionSubject> subjects) {
        when(assessmentRepository.findById(TEST_ID))
                .thenReturn(Optional.of(buildAssessment()));
        when(questionRepository.findByAssessmentIdOrderByPosition(TEST_ID))
                .thenReturn(questions);

        List<UUID> qIds = questions.stream().map(Question::getId).toList();
        if (!qIds.isEmpty()) {
            when(optionRepository.findByQuestionIdInOrderByPosition(qIds))
                    .thenReturn(options);
            when(questionSubjectRepository.findByQuestionIdIn(qIds))
                    .thenReturn(subjects);
        }

        when(snapshotRepository.findByAssessmentIdAndContentHash(eq(TEST_ID), anyString()))
                .thenReturn(Optional.empty());
        when(snapshotRepository.findTopByAssessmentIdOrderByVersionDesc(TEST_ID))
                .thenReturn(Optional.empty());

        when(snapshotRepository.save(any(AssessmentSnapshot.class)))
                .thenAnswer(inv -> {
                    AssessmentSnapshot s = inv.getArgument(0);
                    s.setId(SNAPSHOT_ID);
                    return s;
                });

        UUID qsId = UUID.fromString("ff000000-0000-0000-0000-000000000001");
        when(questionSnapshotRepository.save(any(QuestionSnapshot.class)))
                .thenAnswer(inv -> {
                    QuestionSnapshot qs = inv.getArgument(0);
                    qs.setId(qsId);
                    return qs;
                });
    }

    // ── Question-Subject snapshot copy ─────────────────────────────

    @Nested
    @DisplayName("question-subject snapshot copy")
    class QuestionSubjectSnapshotCopy {

        @Test
        @DisplayName("copies question-subject relationships to snapshot")
        void copiesQuestionSubjects() {
            Question q1 = buildQuestion(Q1_ID, 1);
            QuestionSubject qs1 = buildQuestionSubject(Q1_ID, SUBJECT_OOP, "0.60");
            QuestionSubject qs2 = buildQuestionSubject(Q1_ID, SUBJECT_FLOW, "0.40");

            stubPublishSnapshot(List.of(q1), List.of(), List.of(qs1, qs2));

            snapshotService.publishSnapshot(TEST_ID);

            verify(questionSnapshotSubjectRepository, times(2)).save(qssCaptor.capture());
            List<QuestionSnapshotSubject> saved = qssCaptor.getAllValues();

            assertThat(saved).hasSize(2);
            assertThat(saved).extracting(QuestionSnapshotSubject::getSubjectId)
                    .containsExactlyInAnyOrder(SUBJECT_OOP, SUBJECT_FLOW);
            assertThat(saved).extracting(QuestionSnapshotSubject::getWeight)
                    .containsExactlyInAnyOrder(
                            new BigDecimal("0.60"), new BigDecimal("0.40"));
        }

        @Test
        @DisplayName("handles questions with no subjects")
        void noSubjects() {
            Question q1 = buildQuestion(Q1_ID, 1);

            stubPublishSnapshot(List.of(q1), List.of(), List.of());

            snapshotService.publishSnapshot(TEST_ID);

            verify(questionSnapshotSubjectRepository, never()).save(any());
        }
    }

    // ── Content hash ──────────────────────────────────────────────

    @Nested
    @DisplayName("content hash")
    class ContentHash {

        @Test
        @DisplayName("hash changes when subject association is added")
        void hashChangesWithSubject() {
            Assessment assessment = buildAssessment();
            Question q1 = buildQuestion(Q1_ID, 1);
            List<Question> questions = List.of(q1);
            Map<UUID, List<Option>> options = Map.of();

            String hashWithout = SnapshotService.computeContentHash(
                    assessment, questions, options, Map.of());

            QuestionSubject qs = buildQuestionSubject(Q1_ID, SUBJECT_OOP, "1.00");
            String hashWith = SnapshotService.computeContentHash(
                    assessment, questions, options, Map.of(Q1_ID, List.of(qs)));

            assertThat(hashWith).isNotEqualTo(hashWithout);
        }

        @Test
        @DisplayName("hash changes when weight changes")
        void hashChangesWithWeight() {
            Assessment assessment = buildAssessment();
            Question q1 = buildQuestion(Q1_ID, 1);
            List<Question> questions = List.of(q1);
            Map<UUID, List<Option>> options = Map.of();

            QuestionSubject qs1 = buildQuestionSubject(Q1_ID, SUBJECT_OOP, "1.00");
            String hash1 = SnapshotService.computeContentHash(
                    assessment, questions, options, Map.of(Q1_ID, List.of(qs1)));

            QuestionSubject qs2 = buildQuestionSubject(Q1_ID, SUBJECT_OOP, "0.50");
            String hash2 = SnapshotService.computeContentHash(
                    assessment, questions, options, Map.of(Q1_ID, List.of(qs2)));

            assertThat(hash2).isNotEqualTo(hash1);
        }

        @Test
        @DisplayName("hash is deterministic regardless of subject order")
        void hashDeterministicOrder() {
            Assessment assessment = buildAssessment();
            Question q1 = buildQuestion(Q1_ID, 1);
            List<Question> questions = List.of(q1);
            Map<UUID, List<Option>> options = Map.of();

            QuestionSubject qs1 = buildQuestionSubject(Q1_ID, SUBJECT_OOP, "0.60");
            QuestionSubject qs2 = buildQuestionSubject(Q1_ID, SUBJECT_FLOW, "0.40");

            String hash1 = SnapshotService.computeContentHash(
                    assessment, questions, options,
                    Map.of(Q1_ID, List.of(qs1, qs2)));
            String hash2 = SnapshotService.computeContentHash(
                    assessment, questions, options,
                    Map.of(Q1_ID, List.of(qs2, qs1)));

            assertThat(hash1).isEqualTo(hash2);
        }
    }

    // ── Idempotency ───────────────────────────────────────────────

    @Nested
    @DisplayName("idempotency")
    class Idempotency {

        @Test
        @DisplayName("returns existing snapshot when content hash matches")
        void returnsExistingSnapshot() {
            AssessmentSnapshot existing = buildAssessmentSnapshot();

            when(assessmentRepository.findById(TEST_ID))
                    .thenReturn(Optional.of(buildAssessment()));
            when(questionRepository.findByAssessmentIdOrderByPosition(TEST_ID))
                    .thenReturn(List.of());
            when(snapshotRepository.findByAssessmentIdAndContentHash(eq(TEST_ID), anyString()))
                    .thenReturn(Optional.of(existing));

            AssessmentSnapshot result = snapshotService.publishSnapshot(TEST_ID);

            assertThat(result.getId()).isEqualTo(SNAPSHOT_ID);
            verify(snapshotRepository, never()).save(any());
        }
    }
}
