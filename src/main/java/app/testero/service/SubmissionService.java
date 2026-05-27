package app.testero.service;

import app.testero.dto.AnswerInput;
import app.testero.dto.SubmissionCreateRequest;
import app.testero.dto.SubmissionFeedbackResponse;
import app.testero.dto.SubmissionFeedbackResponse.AnswerResult;
import app.testero.entity.Answer;
import app.testero.entity.AnswerSelectedOption;
import app.testero.entity.Option;
import app.testero.entity.Submission;
import app.testero.entity.Test;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AnswerRepository;
import app.testero.repository.AnswerSelectedOptionRepository;
import app.testero.repository.OptionRepository;
import app.testero.repository.SubmissionRepository;
import app.testero.repository.TestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AnswerRepository answerRepository;
    private final AnswerSelectedOptionRepository answerSelectedOptionRepository;
    private final OptionRepository optionRepository;
    private final TestRepository testRepository;

    public SubmissionService(SubmissionRepository submissionRepository,
                             AnswerRepository answerRepository,
                             AnswerSelectedOptionRepository answerSelectedOptionRepository,
                             OptionRepository optionRepository,
                             TestRepository testRepository) {
        this.submissionRepository = submissionRepository;
        this.answerRepository = answerRepository;
        this.answerSelectedOptionRepository = answerSelectedOptionRepository;
        this.optionRepository = optionRepository;
        this.testRepository = testRepository;
    }

    @Transactional
    public SubmissionFeedbackResponse createSubmission(UUID studentId, SubmissionCreateRequest request) {
        UUID testUuid = UUID.fromString(request.testId());

        // 1. Create submission record
        Submission submission = new Submission();
        submission.setStudentId(studentId);
        submission.setTestId(testUuid);
        submission.setSubmittedAt(LocalDateTime.now());
        submission = submissionRepository.save(submission);

        // 2. Create answer records
        List<Answer> answers = new ArrayList<>();
        for (AnswerInput input : request.answers()) {
            Answer answer = new Answer();
            answer.setSubmissionId(submission.getId());
            answer.setQuestionId(UUID.fromString(input.questionId()));
            answer.setType(input.type());
            answer.setText(input.text() != null ? input.text() : "");
            answer.setMotivation(input.motivation() != null ? input.motivation() : "");
            answers.add(answer);
        }
        answers = answerRepository.saveAll(answers);

        // 3. Create answer_selected_option records
        // Build a map from questionId to Answer for lookup
        Map<UUID, Answer> answerByQuestion = new HashMap<>();
        for (Answer a : answers) {
            answerByQuestion.put(a.getQuestionId(), a);
        }

        List<AnswerSelectedOption> selectedOptions = new ArrayList<>();
        for (AnswerInput input : request.answers()) {
            if (input.selectedOptionIds() != null && !input.selectedOptionIds().isEmpty()) {
                Answer answer = answerByQuestion.get(UUID.fromString(input.questionId()));
                for (String optionId : input.selectedOptionIds()) {
                    AnswerSelectedOption aso = new AnswerSelectedOption();
                    aso.setAnswerId(answer.getId());
                    aso.setOptionId(UUID.fromString(optionId));
                    selectedOptions.add(aso);
                }
            }
        }
        if (!selectedOptions.isEmpty()) {
            answerSelectedOptionRepository.saveAll(selectedOptions);
        }

        // 4. Fetch test scoring rules
        Test test = testRepository.findById(testUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));
        double ptsCorrect = test.getPtsCorrect().doubleValue();
        double ptsWrong = test.getPtsWrong().doubleValue();

        // 5. Fetch correct options for all MC questions
        List<UUID> mcQuestionIds = answers.stream()
                .filter(a -> "multiple".equals(a.getType()))
                .map(Answer::getQuestionId)
                .toList();

        Map<UUID, Set<UUID>> correctMap = new HashMap<>();
        if (!mcQuestionIds.isEmpty()) {
            List<Option> correctOptions = optionRepository.findByQuestionIdInAndCorrectTrue(mcQuestionIds);
            for (Option opt : correctOptions) {
                correctMap.computeIfAbsent(opt.getQuestionId(), k -> new HashSet<>()).add(opt.getId());
            }
        }

        // Build selected options map: answerId -> set of selected option IDs
        Map<UUID, Set<UUID>> selectedByAnswer = new HashMap<>();
        for (AnswerSelectedOption aso : selectedOptions) {
            selectedByAnswer.computeIfAbsent(aso.getAnswerId(), k -> new HashSet<>()).add(aso.getOptionId());
        }

        // 6. Score each answer
        double totalScore = 0.0;
        List<AnswerResult> answerResults = new ArrayList<>();

        for (Answer answer : answers) {
            if ("multiple".equals(answer.getType())) {
                Set<UUID> correctIds = correctMap.getOrDefault(answer.getQuestionId(), Set.of());
                Set<UUID> selectedIds = selectedByAnswer.getOrDefault(answer.getId(), Set.of());

                if (selectedIds.isEmpty()) {
                    // Unanswered
                    answer.setIsCorrect(null);
                    answer.setPointsAwarded(0.0);
                } else {
                    boolean isCorrect = selectedIds.equals(correctIds);
                    double points = isCorrect ? ptsCorrect : ptsWrong;
                    answer.setIsCorrect(isCorrect);
                    answer.setPointsAwarded(points);
                    totalScore += points;
                }

                answerRepository.save(answer);

                answerResults.add(new AnswerResult(
                        answer.getQuestionId().toString(),
                        "multiple",
                        answer.getIsCorrect(),
                        correctIds.stream().map(UUID::toString).toList()
                ));
            } else {
                // Open question: pending manual grading
                answerResults.add(new AnswerResult(
                        answer.getQuestionId().toString(),
                        "open",
                        null,
                        List.of()
                ));
            }
        }

        // 7. Update submission score
        submission.setScore(totalScore);
        submissionRepository.save(submission);

        // 8. Return feedback (without score/points)
        return new SubmissionFeedbackResponse(
                submission.getId().toString(),
                submission.getStudentId().toString(),
                submission.getTestId().toString(),
                submission.getSubmittedAt().toString(),
                answerResults
        );
    }

    public SubmissionFeedbackResponse getSubmission(UUID submissionId, UUID studentId) {
        Submission submission = submissionRepository.findByIdAndStudentId(submissionId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        List<Answer> answers = answerRepository.findBySubmissionId(submissionId);

        // Fetch correct options for MC questions
        List<UUID> mcQuestionIds = answers.stream()
                .filter(a -> "multiple".equals(a.getType()))
                .map(Answer::getQuestionId)
                .toList();

        Map<UUID, List<String>> correctMap = new HashMap<>();
        if (!mcQuestionIds.isEmpty()) {
            List<Option> correctOptions = optionRepository.findByQuestionIdInAndCorrectTrue(mcQuestionIds);
            for (Option opt : correctOptions) {
                correctMap.computeIfAbsent(opt.getQuestionId(), k -> new ArrayList<>()).add(opt.getId().toString());
            }
        }

        List<AnswerResult> answerResults = answers.stream()
                .map(a -> new AnswerResult(
                        a.getQuestionId().toString(),
                        a.getType(),
                        a.getIsCorrect(),
                        correctMap.getOrDefault(a.getQuestionId(), List.of())
                ))
                .toList();

        return new SubmissionFeedbackResponse(
                submission.getId().toString(),
                submission.getStudentId().toString(),
                submission.getTestId().toString(),
                submission.getSubmittedAt().toString(),
                answerResults
        );
    }
}
