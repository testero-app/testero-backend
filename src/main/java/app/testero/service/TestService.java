package app.testero.service;

import app.testero.dto.TestConfigResponse;
import app.testero.dto.TestListResponse;
import app.testero.dto.TestQuestionsResponse;
import app.testero.dto.TestQuestionsResponse.OptionDto;
import app.testero.dto.TestQuestionsResponse.QuestionDto;
import app.testero.entity.Option;
import app.testero.entity.Question;
import app.testero.entity.Test;
import app.testero.entity.TestStart;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.OptionRepository;
import app.testero.repository.QuestionRepository;
import app.testero.repository.TestRepository;
import app.testero.repository.TestStartRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TestService {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final TestStartRepository testStartRepository;
    private final QuestionPrepService questionPrepService;

    public TestService(TestRepository testRepository,
                       QuestionRepository questionRepository,
                       OptionRepository optionRepository,
                       TestStartRepository testStartRepository,
                       QuestionPrepService questionPrepService) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.testStartRepository = testStartRepository;
        this.questionPrepService = questionPrepService;
    }

    public TestListResponse getAvailableTests(UUID classId) {
        List<Test> tests = testRepository.findTestsByClassId(classId);

        List<TestListResponse.TestListItem> items = tests.stream()
                .map(t -> new TestListResponse.TestListItem(
                        t.getId().toString(),
                        t.getTitle(),
                        t.getDate().toString(),
                        t.getTimerMinutes(),
                        t.getQuestionsPerTest()
                ))
                .toList();

        return new TestListResponse(items);
    }

    public TestConfigResponse getTestConfig(String testId) {
        Test test = findTestOrThrow(testId);

        return new TestConfigResponse(
                test.getId().toString(),
                test.getTitle(),
                test.getDate().toString(),
                test.getTimerMinutes(),
                test.getTotalPool(),
                test.getQuestionsPerTest(),
                new TestConfigResponse.ScoringRules(
                        test.getPtsCorrect().doubleValue(),
                        test.getPtsWrong().doubleValue()
                )
        );
    }

    public void recordTestStart(String testId) {
        findTestOrThrow(testId);

        TestStart ts = new TestStart();
        ts.setTestId(UUID.fromString(testId));
        testStartRepository.save(ts);
    }

    public TestQuestionsResponse getTestQuestions(String testId) {
        Test test = findTestOrThrow(testId);

        // Fetch all questions for the test, ordered by position
        List<Question> questions = questionRepository.findByTestIdOrderByPosition(test.getId());
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
                test.getQuestionsPerTest()
        );

        return new TestQuestionsResponse(
                test.getId().toString(),
                test.getTitle(),
                test.getDate().toString(),
                test.getTimerMinutes(),
                prepared.size(),
                prepared
        );
    }

    private Test findTestOrThrow(String testId) {
        return testRepository.findById(UUID.fromString(testId))
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));
    }
}
