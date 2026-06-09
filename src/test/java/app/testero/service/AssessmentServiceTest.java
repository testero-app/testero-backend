package app.testero.service;

import app.testero.dto.AssessmentConfigResponse;
import app.testero.dto.AssessmentListResponse;
import app.testero.dto.AssessmentQuestionsResponse;
import app.testero.dto.AssessmentQuestionsResponse.QuestionDto;
import app.testero.entity.assessment.Assessment;
import app.testero.entity.assessment.AssessmentStart;
import app.testero.entity.assessment.Option;
import app.testero.entity.assessment.Question;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AssessmentRepository;
import app.testero.repository.AssessmentStartRepository;
import app.testero.repository.OptionRepository;
import app.testero.repository.QuestionRepository;

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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import app.testero.fixture.PythonCertificationFixture;

import static app.testero.fixture.PythonCertificationFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock AssessmentRepository assessmentRepository;
    @Mock QuestionRepository questionRepository;
    @Mock OptionRepository optionRepository;
    @Mock AssessmentStartRepository assessmentStartRepository;
    @Mock QuestionPrepService questionPrepService;

    @InjectMocks AssessmentService assessmentService;

    @Captor ArgumentCaptor<AssessmentStart> startCaptor;

    // ── Helpers ────────────────────────────────────────────────────

    private static final UUID CLASS_ID = UUID.fromString("bb000000-0000-0000-0000-000000000099");

    private Question buildQuestion(UUID id, String type, int position) {
        Question q = new Question();
        q.setId(id);
        q.setAssessmentId(TEST_ID);
        q.setType(type);
        q.setText("Question " + position);
        q.setPosition(position);
        return q;
    }

    private Option buildOpt(UUID id, UUID questionId, String text, int position) {
        return buildOption(id, questionId, text, false, false, position);
    }

    // ── getAvailableAssessments ────────────────────────────────────

    @Nested
    @DisplayName("getAvailableAssessments")
    class GetAvailableAssessments {

        @Test
        @DisplayName("returns mapped list from repository")
        void returnsMappedList() {
            Assessment a = buildAssessment();
            when(assessmentRepository.findAssessmentsByClassId(CLASS_ID))
                    .thenReturn(List.of(a));

            AssessmentListResponse response = assessmentService.getAvailableAssessments(CLASS_ID);

            assertThat(response.assessments()).hasSize(1);
            var item = response.assessments().get(0);
            assertThat(item.id()).isEqualTo(TEST_ID.toString());
            assertThat(item.title()).isEqualTo(TITLE);
            assertThat(item.date()).isEqualTo(PythonCertificationFixture.DATE.toString());
            assertThat(item.timerMinutes()).isEqualTo(TIMER_MINUTES);
            assertThat(item.questionsPerAssessment()).isEqualTo(QUESTIONS_PER_ASSESSMENT);
        }

        @Test
        @DisplayName("returns empty list when no assessments exist")
        void emptyList() {
            when(assessmentRepository.findAssessmentsByClassId(CLASS_ID))
                    .thenReturn(List.of());

            AssessmentListResponse response = assessmentService.getAvailableAssessments(CLASS_ID);

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
            Assessment a = buildAssessment();
            when(assessmentRepository.findById(TEST_ID)).thenReturn(Optional.of(a));

            AssessmentConfigResponse response =
                    assessmentService.getAssessmentConfig(TEST_ID.toString());

            assertThat(response.assessmentId()).isEqualTo(TEST_ID.toString());
            assertThat(response.title()).isEqualTo(TITLE);
            assertThat(response.timerMinutes()).isEqualTo(TIMER_MINUTES);
            assertThat(response.totalPool()).isEqualTo(TOTAL_POOL);
            assertThat(response.questionsPerAssessment()).isEqualTo(QUESTIONS_PER_ASSESSMENT);
            assertThat(response.scoring().pointsPerCorrect()).isEqualTo(PTS_CORRECT.doubleValue());
            assertThat(response.scoring().pointsPerWrong()).isEqualTo(PTS_WRONG.doubleValue());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not found")
        void notFound() {
            UUID unknownId = UUID.randomUUID();
            when(assessmentRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    assessmentService.getAssessmentConfig(unknownId.toString()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── recordAssessmentStart ──────────────────────────────────────

    @Nested
    @DisplayName("recordAssessmentStart")
    class RecordAssessmentStart {

        @Test
        @DisplayName("saves AssessmentStart entity with correct assessment ID")
        void savesEntity() {
            when(assessmentRepository.findById(TEST_ID))
                    .thenReturn(Optional.of(buildAssessment()));

            assessmentService.recordAssessmentStart(TEST_ID.toString());

            verify(assessmentStartRepository).save(startCaptor.capture());
            assertThat(startCaptor.getValue().getAssessmentId()).isEqualTo(TEST_ID);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when assessment not found")
        void notFound() {
            UUID unknownId = UUID.randomUUID();
            when(assessmentRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    assessmentService.recordAssessmentStart(unknownId.toString()))
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
            Assessment a = buildAssessment();
            Question q1 = buildQuestion(Q1_ID, "multiple", 1);
            Question q2 = buildQuestion(Q2_ID, "multiple", 2);

            Option opt1a = buildOpt(Q1_OPT_A, Q1_ID, "Opt A", 1);
            Option opt1b = buildOpt(Q1_OPT_B, Q1_ID, "Opt B", 2);
            Option opt2a = buildOpt(Q2_OPT_A, Q2_ID, "Opt A", 1);

            when(assessmentRepository.findById(TEST_ID)).thenReturn(Optional.of(a));
            when(questionRepository.findByAssessmentIdOrderByPosition(TEST_ID))
                    .thenReturn(List.of(q1, q2));
            when(optionRepository.findByQuestionIdInOrderByPosition(List.of(Q1_ID, Q2_ID)))
                    .thenReturn(List.of(opt1a, opt1b, opt2a));

            // QuestionPrepService returns its input unchanged for this test
            when(questionPrepService.prepare(anyList(), anyInt()))
                    .thenAnswer(inv -> inv.getArgument(0));

            AssessmentQuestionsResponse response =
                    assessmentService.getAssessmentQuestions(TEST_ID.toString());

            assertThat(response.assessmentId()).isEqualTo(TEST_ID.toString());
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
        @DisplayName("throws ResourceNotFoundException when assessment not found")
        void notFound() {
            UUID unknownId = UUID.randomUUID();
            when(assessmentRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    assessmentService.getAssessmentQuestions(unknownId.toString()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("returns empty questions when assessment has none")
        void noQuestions() {
            Assessment a = buildAssessment();
            when(assessmentRepository.findById(TEST_ID)).thenReturn(Optional.of(a));
            when(questionRepository.findByAssessmentIdOrderByPosition(TEST_ID))
                    .thenReturn(List.of());

            when(questionPrepService.prepare(anyList(), anyInt()))
                    .thenAnswer(inv -> inv.getArgument(0));

            AssessmentQuestionsResponse response =
                    assessmentService.getAssessmentQuestions(TEST_ID.toString());

            assertThat(response.questions()).isEmpty();
            assertThat(response.totalQuestions()).isZero();
        }
    }
}
