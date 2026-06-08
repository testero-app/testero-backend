package app.testero.service;

import app.testero.dto.AnswerInput;
import app.testero.dto.SubmissionCreateRequest;
import app.testero.dto.SubmissionFeedbackResponse;
import app.testero.dto.SubmissionFeedbackResponse.AnswerResult;
import app.testero.entity.assessment.Assessment;
import app.testero.entity.assessment.Option;
import app.testero.entity.submission.Submission;
import app.testero.entity.submission.UserAnswer;
import app.testero.entity.submission.UserAnswerSelectedOption;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AssessmentRepository;
import app.testero.repository.OptionRepository;
import app.testero.repository.SubmissionRepository;
import app.testero.repository.UserAnswerRepository;
import app.testero.repository.UserAnswerSelectedOptionRepository;
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
    private final UserAnswerRepository userAnswerRepository;
    private final UserAnswerSelectedOptionRepository userAnswerSelectedOptionRepository;
    private final OptionRepository optionRepository;
    private final AssessmentRepository assessmentRepository;

    public SubmissionService(SubmissionRepository submissionRepository,
                             UserAnswerRepository userAnswerRepository,
                             UserAnswerSelectedOptionRepository userAnswerSelectedOptionRepository,
                             OptionRepository optionRepository,
                             AssessmentRepository assessmentRepository) {
        this.submissionRepository = submissionRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.userAnswerSelectedOptionRepository = userAnswerSelectedOptionRepository;
        this.optionRepository = optionRepository;
        this.assessmentRepository = assessmentRepository;
    }

    @Transactional
    public SubmissionFeedbackResponse createSubmission(UUID userId, SubmissionCreateRequest request) {
        UUID assessmentUuid = UUID.fromString(request.assessmentId());

        // 1. Create submission record
        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setAssessmentId(assessmentUuid);
        submission.setStartedAt(request.startedAt() != null
                ? LocalDateTime.parse(request.startedAt())
                : null);
        submission.setSubmittedAt(LocalDateTime.now());
        submission = submissionRepository.save(submission);

        // 2. Create answer records
        List<UserAnswer> answers = new ArrayList<>();
        for (AnswerInput input : request.answers()) {
            UserAnswer answer = new UserAnswer();
            answer.setSubmissionId(submission.getId());
            answer.setQuestionId(UUID.fromString(input.questionId()));
            answer.setType(input.type());
            answer.setText(input.text() != null ? input.text() : "");
            answer.setMotivation(input.motivation() != null ? input.motivation() : "");
            answers.add(answer);
        }
        answers = userAnswerRepository.saveAll(answers);

        // 3. Create answer_selected_option records
        // Build a map from questionId to UserAnswer for lookup
        Map<UUID, UserAnswer> answerByQuestion = new HashMap<>();
        for (UserAnswer a : answers) {
            answerByQuestion.put(a.getQuestionId(), a);
        }

        List<UserAnswerSelectedOption> selectedOptions = new ArrayList<>();
        for (AnswerInput input : request.answers()) {
            if (input.selectedOptionIds() != null && !input.selectedOptionIds().isEmpty()) {
                UserAnswer answer = answerByQuestion.get(UUID.fromString(input.questionId()));
                for (String optionId : input.selectedOptionIds()) {
                    UserAnswerSelectedOption aso = new UserAnswerSelectedOption();
                    aso.setAnswerId(answer.getId());
                    aso.setOptionId(UUID.fromString(optionId));
                    selectedOptions.add(aso);
                }
            }
        }
        if (!selectedOptions.isEmpty()) {
            userAnswerSelectedOptionRepository.saveAll(selectedOptions);
        }

        // 4. Fetch assessment scoring rules
        Assessment assessment = assessmentRepository.findById(assessmentUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));
        double ptsCorrect = assessment.getPtsCorrect().doubleValue();
        double ptsWrong = assessment.getPtsWrong().doubleValue();

        // 5. Fetch correct options for all MC questions
        List<UUID> mcQuestionIds = answers.stream()
                .filter(a -> "multiple".equals(a.getType()))
                .map(UserAnswer::getQuestionId)
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
        for (UserAnswerSelectedOption aso : selectedOptions) {
            selectedByAnswer.computeIfAbsent(aso.getAnswerId(), k -> new HashSet<>()).add(aso.getOptionId());
        }

        // 6. Score each answer
        double totalScore = 0.0;
        List<AnswerResult> answerResults = new ArrayList<>();

        for (UserAnswer answer : answers) {
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

                userAnswerRepository.save(answer);

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
                submission.getUserId().toString(),
                submission.getAssessmentId().toString(),
                submission.getStartedAt() != null ? submission.getStartedAt().toString() : null,
                submission.getSubmittedAt().toString(),
                answerResults
        );
    }

    public SubmissionFeedbackResponse getSubmission(UUID submissionId, UUID userId) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        List<UserAnswer> answers = userAnswerRepository.findBySubmissionId(submissionId);

        // Fetch correct options for MC questions
        List<UUID> mcQuestionIds = answers.stream()
                .filter(a -> "multiple".equals(a.getType()))
                .map(UserAnswer::getQuestionId)
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
                submission.getUserId().toString(),
                submission.getAssessmentId().toString(),
                submission.getStartedAt() != null ? submission.getStartedAt().toString() : null,
                submission.getSubmittedAt().toString(),
                answerResults
        );
    }
}
