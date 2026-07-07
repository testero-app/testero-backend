package app.testero.service;

import app.testero.entity.assessment.AssessmentTemplate;
import app.testero.entity.assessment.OptionTemplate;
import app.testero.entity.assessment.QuestionTemplate;
import app.testero.entity.assessment.QuestionTemplateSubject;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.snapshot.QuestionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshotSubject;
import app.testero.repository.AssessmentSubjectRepository;
import app.testero.repository.AssessmentTemplateRepository;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.AssessmentSnapshotSubjectRepository;
import app.testero.repository.OptionTemplateRepository;
import app.testero.repository.OptionSnapshotRepository;
import app.testero.repository.QuestionTemplateRepository;
import app.testero.repository.QuestionSnapshotRepository;
import app.testero.repository.QuestionTemplateSubjectRepository;
import app.testero.repository.QuestionSnapshotSubjectRepository;
import app.testero.repository.SubjectRepository;
import app.testero.repository.AssessmentTemplateTopicRepository;
import app.testero.repository.AssessmentSnapshotTopicRepository;
import app.testero.repository.TopicRepository;

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

    @Mock AssessmentTemplateRepository assessmentRepository;
    @Mock AssessmentSubjectRepository assessmentSubjectRepository;
    @Mock QuestionTemplateRepository questionTemplateRepository;
    @Mock OptionTemplateRepository optionTemplateRepository;
    @Mock QuestionTemplateSubjectRepository questionTemplateSubjectRepository;
    @Mock AssessmentSnapshotRepository snapshotRepository;
    @Mock AssessmentSnapshotSubjectRepository assessmentSnapshotSubjectRepository;
    @Mock QuestionSnapshotRepository questionSnapshotRepository;
    @Mock OptionSnapshotRepository optionSnapshotRepository;
    @Mock QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository;
    @Mock SubjectRepository subjectRepository;
    @Mock AssessmentTemplateTopicRepository assessmentTemplateTopicRepository;
    @Mock AssessmentSnapshotTopicRepository assessmentSnapshotTopicRepository;
    @Mock TopicRepository topicRepository;

    @InjectMocks SnapshotService snapshotService;

    @Captor ArgumentCaptor<QuestionSnapshotSubject> qssCaptor;

    // ── Helpers ────────────────────────────────────────────────────

    private static final UUID SUBJECT_OOP = UUID.fromString("ee000000-0000-0000-0000-000000000001");
    private static final UUID SUBJECT_FLOW = UUID.fromString("ee000000-0000-0000-0000-000000000002");

    private static QuestionTemplate buildQuestion(UUID id, int position) {
        QuestionTemplate q = new QuestionTemplate();
        q.setId(id);
        q.setAssessmentId(TEST_ID);
        q.setType("multiple");
        q.setText("Question " + position);
        q.setPosition(position);
        return q;
    }

    private static QuestionTemplateSubject buildQuestionTemplateSubject(UUID questionId, UUID subjectId,
                                                        String weight) {
        QuestionTemplateSubject qs = new QuestionTemplateSubject();
        qs.setQuestionTemplateId(questionId);
        qs.setSubjectId(subjectId);
        qs.setWeight(new BigDecimal(weight));
        return qs;
    }

    private void stubPublishSnapshot(List<QuestionTemplate> questions, List<OptionTemplate> options,
                                     List<QuestionTemplateSubject> subjects) {
        when(assessmentRepository.findById(TEST_ID))
                .thenReturn(Optional.of(buildAssessment()));
        when(assessmentSubjectRepository.findByAssessmentId(TEST_ID))
                .thenReturn(List.of());
        when(questionTemplateRepository.findByAssessmentIdOrderByPosition(TEST_ID))
                .thenReturn(questions);

        List<UUID> qIds = questions.stream().map(QuestionTemplate::getId).toList();
        if (!qIds.isEmpty()) {
            when(optionTemplateRepository.findByQuestionTemplateIdInOrderByPosition(qIds))
                    .thenReturn(options);
            when(questionTemplateSubjectRepository.findByQuestionTemplateIdIn(qIds))
                    .thenReturn(subjects);
        }

        lenient().when(subjectRepository.findByIdIn(any()))
                .thenReturn(List.of());
        when(assessmentTemplateTopicRepository.findByAssessmentTemplateId(TEST_ID))
                .thenReturn(List.of());

        when(snapshotRepository.findByAssessmentTemplateIdAndContentHash(eq(TEST_ID), anyString()))
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
    class QuestionTemplateSubjectSnapshotCopy {

        @Test
        @DisplayName("copies question-subject relationships to snapshot")
        void copiesQuestionTemplateSubjects() {
            QuestionTemplate q1 = buildQuestion(Q1_ID, 1);
            QuestionTemplateSubject qs1 = buildQuestionTemplateSubject(Q1_ID, SUBJECT_OOP, "0.60");
            QuestionTemplateSubject qs2 = buildQuestionTemplateSubject(Q1_ID, SUBJECT_FLOW, "0.40");

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
            QuestionTemplate q1 = buildQuestion(Q1_ID, 1);

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
            AssessmentTemplate assessment = buildAssessment();
            QuestionTemplate q1 = buildQuestion(Q1_ID, 1);
            List<QuestionTemplate> questions = List.of(q1);
            Map<UUID, List<OptionTemplate>> options = Map.of();

            String hashWithout = SnapshotService.computeContentHash(
                    assessment, questions, options, Map.of(), List.of());

            QuestionTemplateSubject qs = buildQuestionTemplateSubject(Q1_ID, SUBJECT_OOP, "1.00");
            String hashWith = SnapshotService.computeContentHash(
                    assessment, questions, options, Map.of(Q1_ID, List.of(qs)), List.of());

            assertThat(hashWith).isNotEqualTo(hashWithout);
        }

        @Test
        @DisplayName("hash changes when weight changes")
        void hashChangesWithWeight() {
            AssessmentTemplate assessment = buildAssessment();
            QuestionTemplate q1 = buildQuestion(Q1_ID, 1);
            List<QuestionTemplate> questions = List.of(q1);
            Map<UUID, List<OptionTemplate>> options = Map.of();

            QuestionTemplateSubject qs1 = buildQuestionTemplateSubject(Q1_ID, SUBJECT_OOP, "1.00");
            String hash1 = SnapshotService.computeContentHash(
                    assessment, questions, options, Map.of(Q1_ID, List.of(qs1)), List.of());

            QuestionTemplateSubject qs2 = buildQuestionTemplateSubject(Q1_ID, SUBJECT_OOP, "0.50");
            String hash2 = SnapshotService.computeContentHash(
                    assessment, questions, options, Map.of(Q1_ID, List.of(qs2)), List.of());

            assertThat(hash2).isNotEqualTo(hash1);
        }

        @Test
        @DisplayName("hash is deterministic regardless of subject order")
        void hashDeterministicOrder() {
            AssessmentTemplate assessment = buildAssessment();
            QuestionTemplate q1 = buildQuestion(Q1_ID, 1);
            List<QuestionTemplate> questions = List.of(q1);
            Map<UUID, List<OptionTemplate>> options = Map.of();

            QuestionTemplateSubject qs1 = buildQuestionTemplateSubject(Q1_ID, SUBJECT_OOP, "0.60");
            QuestionTemplateSubject qs2 = buildQuestionTemplateSubject(Q1_ID, SUBJECT_FLOW, "0.40");

            String hash1 = SnapshotService.computeContentHash(
                    assessment, questions, options,
                    Map.of(Q1_ID, List.of(qs1, qs2)), List.of());
            String hash2 = SnapshotService.computeContentHash(
                    assessment, questions, options,
                    Map.of(Q1_ID, List.of(qs2, qs1)), List.of());

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
            when(questionTemplateRepository.findByAssessmentIdOrderByPosition(TEST_ID))
                    .thenReturn(List.of());
            when(snapshotRepository.findByAssessmentTemplateIdAndContentHash(eq(TEST_ID), anyString()))
                    .thenReturn(Optional.of(existing));

            AssessmentSnapshot result = snapshotService.publishSnapshot(TEST_ID);

            assertThat(result.getId()).isEqualTo(SNAPSHOT_ID);
            verify(snapshotRepository, never()).save(any());
        }
    }
}
