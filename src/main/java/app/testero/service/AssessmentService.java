package app.testero.service;

import app.testero.dto.AssessmentConfigResponse;
import app.testero.dto.AssessmentListResponse;
import app.testero.dto.AssessmentQuestionsResponse;
import app.testero.dto.AssessmentQuestionsResponse.OptionDto;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final AssessmentStartRepository assessmentStartRepository;
    private final QuestionPrepService questionPrepService;

    public AssessmentService(AssessmentRepository assessmentRepository,
                             QuestionRepository questionRepository,
                             OptionRepository optionRepository,
                             AssessmentStartRepository assessmentStartRepository,
                             QuestionPrepService questionPrepService) {
        this.assessmentRepository = assessmentRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.assessmentStartRepository = assessmentStartRepository;
        this.questionPrepService = questionPrepService;
    }

    public AssessmentListResponse getAvailableAssessments(UUID classId) {
        List<Assessment> assessments = assessmentRepository.findAssessmentsByClassId(classId);

        List<AssessmentListResponse.AssessmentListItem> items = assessments.stream()
                .map(a -> new AssessmentListResponse.AssessmentListItem(
                        a.getId().toString(),
                        a.getTitle(),
                        a.getDate().toString(),
                        a.getTimerMinutes(),
                        a.getQuestionsPerAssessment()
                ))
                .toList();

        return new AssessmentListResponse(items);
    }

    public AssessmentConfigResponse getAssessmentConfig(String assessmentId) {
        Assessment assessment = findAssessmentOrThrow(assessmentId);

        return new AssessmentConfigResponse(
                assessment.getId().toString(),
                assessment.getTitle(),
                assessment.getDate().toString(),
                assessment.getTimerMinutes(),
                assessment.getTotalPool(),
                assessment.getQuestionsPerAssessment(),
                new AssessmentConfigResponse.ScoringRules(
                        assessment.getPtsCorrect().doubleValue(),
                        assessment.getPtsWrong().doubleValue()
                )
        );
    }

    public void recordAssessmentStart(String assessmentId) {
        findAssessmentOrThrow(assessmentId);

        AssessmentStart as = new AssessmentStart();
        as.setAssessmentId(UUID.fromString(assessmentId));
        assessmentStartRepository.save(as);
    }

    public AssessmentQuestionsResponse getAssessmentQuestions(String assessmentId) {
        Assessment assessment = findAssessmentOrThrow(assessmentId);

        // Fetch all questions for the assessment, ordered by position
        List<Question> questions = questionRepository.findByAssessmentIdOrderByPosition(assessment.getId());
        List<UUID> questionIds = questions.stream().map(Question::getId).toList();

        // Fetch all options for these questions in one query
        List<Option> allOptions = questionIds.isEmpty()
                ? List.of()
                : optionRepository.findByQuestionIdInOrderByPosition(questionIds);

        // Group options by question ID
        Map<UUID, List<Option>> optionsByQuestion = allOptions.stream()
                .collect(Collectors.groupingBy(Option::getQuestionId));

        // Map to DTOs (including correct field stripped — we never include it)
        List<QuestionDto> questionDtos = questions.stream()
                .map(q -> {
                    List<Option> opts = optionsByQuestion.getOrDefault(q.getId(), List.of());
                    List<OptionDto> optionDtos = opts.stream()
                            .map(o -> new OptionDto(o.getId().toString(), o.getText(), o.isFallback()))
                            .toList();
                    return new QuestionDto(
                            q.getId().toString(),
                            q.getType(),
                            q.getText(),
                            q.getCode(),
                            "multiple".equals(q.getType()) ? optionDtos : null
                    );
                })
                .toList();

        // Run through preparation pipeline: random subset, shuffle, shuffle options
        List<QuestionDto> prepared = questionPrepService.prepare(
                new ArrayList<>(questionDtos),
                assessment.getQuestionsPerAssessment()
        );

        return new AssessmentQuestionsResponse(
                assessment.getId().toString(),
                assessment.getTitle(),
                assessment.getDate().toString(),
                assessment.getTimerMinutes(),
                prepared.size(),
                prepared
        );
    }

    private Assessment findAssessmentOrThrow(String assessmentId) {
        return assessmentRepository.findById(UUID.fromString(assessmentId))
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));
    }
}
