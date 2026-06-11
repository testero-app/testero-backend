package app.testero.service;

import app.testero.dto.AnswerInput;
import app.testero.dto.SubmissionFeedbackResponse;
import app.testero.dto.SubmissionFeedbackResponse.AnswerResult;
import app.testero.dto.SubmissionHistoryResponse;
import app.testero.dto.SubmissionHistoryResponse.SubmissionSummary;
import app.testero.dto.SubmissionReviewResponse;
import app.testero.dto.SubmissionReviewResponse.ReviewOption;
import app.testero.dto.SubmissionReviewResponse.ReviewQuestion;
import app.testero.dto.SubmissionStartResponse;
import app.testero.dto.SubmissionSubmitRequest;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.snapshot.OptionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshot;
import app.testero.entity.submission.Submission;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserAnswerSelectedOptionRepository userAnswerSelectedOptionRepository;
    private final OptionSnapshotRepository optionSnapshotRepository;
    private final AssessmentSnapshotRepository assessmentSnapshotRepository;
    private final QuestionSnapshotRepository questionSnapshotRepository;

    public SubmissionService(SubmissionRepository submissionRepository,
                             UserAnswerRepository userAnswerRepository,
                             UserAnswerSelectedOptionRepository userAnswerSelectedOptionRepository,
                             OptionSnapshotRepository optionSnapshotRepository,
                             AssessmentSnapshotRepository assessmentSnapshotRepository,
                             QuestionSnapshotRepository questionSnapshotRepository) {
        this.submissionRepository = submissionRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.userAnswerSelectedOptionRepository = userAnswerSelectedOptionRepository;
        this.optionSnapshotRepository = optionSnapshotRepository;
        this.assessmentSnapshotRepository = assessmentSnapshotRepository;
        this.questionSnapshotRepository = questionSnapshotRepository;
    }

    @Transactional
    public SubmissionStartResponse startSubmission(UUID assessmentSnapshotId, UUID userId) {
        assessmentSnapshotRepository.findById(assessmentSnapshotId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment snapshot not found"));

        // Idempotent: if the user has an in-progress submission for this snapshot, return it.
        Optional<Submission> existing = submissionRepository
                .findByAssessmentSnapshotIdAndUserIdAndSubmittedAtIsNull(assessmentSnapshotId, userId);
        if (existing.isPresent()) {
            Submission s = existing.get();
            return new SubmissionStartResponse(
                    s.getId().toString(),
                    s.getStartedAt().toString()
            );
        }

        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setAssessmentSnapshotId(assessmentSnapshotId);
        submission.setStartedAt(LocalDateTime.now());
        submission = submissionRepository.save(submission);

        return new SubmissionStartResponse(
                submission.getId().toString(),
                submission.getStartedAt().toString()
        );
    }

    @Transactional
    public SubmissionFeedbackResponse submitAnswers(UUID submissionId, UUID userId,
                                                     SubmissionSubmitRequest request) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (submission.getSubmittedAt() != null) {
            throw new IllegalSubmissionStateException("Submission already completed");
        }

        UUID snapshotId = submission.getAssessmentSnapshotId();

        // 1. Set submitted timestamp
        submission.setSubmittedAt(LocalDateTime.now());

        // 2. Create answer records (referencing question snapshots)
        List<UserAnswer> answers = new ArrayList<>();
        for (AnswerInput input : request.answers()) {
            UserAnswer answer = new UserAnswer();
            answer.setSubmissionId(submission.getId());
            answer.setQuestionSnapshotId(UUID.fromString(input.questionId()));
            answer.setType(input.type());
            answer.setText(input.text() != null ? input.text() : "");
            answer.setMotivation(input.motivation() != null ? input.motivation() : "");
            answers.add(answer);
        }
        answers = userAnswerRepository.saveAll(answers);

        // 3. Create answer_selected_option records (referencing option snapshots)
        Map<UUID, UserAnswer> answerByQuestion = new HashMap<>();
        for (UserAnswer a : answers) {
            answerByQuestion.put(a.getQuestionSnapshotId(), a);
        }

        List<UserAnswerSelectedOption> selectedOptions = new ArrayList<>();
        for (AnswerInput input : request.answers()) {
            if (input.selectedOptionIds() != null && !input.selectedOptionIds().isEmpty()) {
                UserAnswer answer = answerByQuestion.get(UUID.fromString(input.questionId()));
                for (String optionId : input.selectedOptionIds()) {
                    UserAnswerSelectedOption aso = new UserAnswerSelectedOption();
                    aso.setAnswerId(answer.getId());
                    aso.setOptionSnapshotId(UUID.fromString(optionId));
                    selectedOptions.add(aso);
                }
            }
        }
        if (!selectedOptions.isEmpty()) {
            userAnswerSelectedOptionRepository.saveAll(selectedOptions);
        }

        // 4. Fetch snapshot scoring rules
        AssessmentSnapshot snapshot = assessmentSnapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment snapshot not found"));
        double ptsCorrect = snapshot.getPtsCorrect().doubleValue();
        double ptsWrong = snapshot.getPtsWrong().doubleValue();

        // 5. Fetch correct options from option_snapshot for all MC questions
        List<UUID> mcQuestionSnapshotIds = answers.stream()
                .filter(a -> "multiple".equals(a.getType()))
                .map(UserAnswer::getQuestionSnapshotId)
                .toList();

        Map<UUID, Set<UUID>> correctMap = new HashMap<>();
        if (!mcQuestionSnapshotIds.isEmpty()) {
            List<OptionSnapshot> correctOptions = optionSnapshotRepository
                    .findByQuestionSnapshotIdInAndCorrectTrue(mcQuestionSnapshotIds);
            for (OptionSnapshot opt : correctOptions) {
                correctMap.computeIfAbsent(opt.getQuestionSnapshotId(), k -> new HashSet<>())
                        .add(opt.getId());
            }
        }

        // Build selected options map: answerId -> set of selected option snapshot IDs
        Map<UUID, Set<UUID>> selectedByAnswer = new HashMap<>();
        for (UserAnswerSelectedOption aso : selectedOptions) {
            selectedByAnswer.computeIfAbsent(aso.getAnswerId(), k -> new HashSet<>())
                    .add(aso.getOptionSnapshotId());
        }

        // 6. Score each answer
        double totalScore = 0.0;
        List<AnswerResult> answerResults = new ArrayList<>();

        for (UserAnswer answer : answers) {
            if ("multiple".equals(answer.getType())) {
                Set<UUID> correctIds = correctMap.getOrDefault(answer.getQuestionSnapshotId(), Set.of());
                Set<UUID> selectedIds = selectedByAnswer.getOrDefault(answer.getId(), Set.of());

                if (selectedIds.isEmpty()) {
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
                        answer.getQuestionSnapshotId().toString(),
                        "multiple",
                        answer.getIsCorrect(),
                        correctIds.stream().map(UUID::toString).toList()
                ));
            } else {
                answerResults.add(new AnswerResult(
                        answer.getQuestionSnapshotId().toString(),
                        "open",
                        null,
                        List.of()
                ));
            }
        }

        // 7. Update submission score
        submission.setScore(totalScore);
        submissionRepository.save(submission);

        // 8. Return feedback
        return new SubmissionFeedbackResponse(
                submission.getId().toString(),
                submission.getUserId().toString(),
                submission.getAssessmentSnapshotId().toString(),
                submission.getStartedAt() != null ? submission.getStartedAt().toString() : null,
                submission.getSubmittedAt().toString(),
                answerResults
        );
    }

    public SubmissionFeedbackResponse getSubmission(UUID submissionId, UUID userId) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        List<UserAnswer> answers = userAnswerRepository.findBySubmissionId(submissionId);

        List<UUID> mcQuestionSnapshotIds = answers.stream()
                .filter(a -> "multiple".equals(a.getType()))
                .map(UserAnswer::getQuestionSnapshotId)
                .toList();

        Map<UUID, List<String>> correctMap = new HashMap<>();
        if (!mcQuestionSnapshotIds.isEmpty()) {
            List<OptionSnapshot> correctOptions = optionSnapshotRepository
                    .findByQuestionSnapshotIdInAndCorrectTrue(mcQuestionSnapshotIds);
            for (OptionSnapshot opt : correctOptions) {
                correctMap.computeIfAbsent(opt.getQuestionSnapshotId(), k -> new ArrayList<>())
                        .add(opt.getId().toString());
            }
        }

        List<AnswerResult> answerResults = answers.stream()
                .map(a -> new AnswerResult(
                        a.getQuestionSnapshotId().toString(),
                        a.getType(),
                        a.getIsCorrect(),
                        correctMap.getOrDefault(a.getQuestionSnapshotId(), List.of())
                ))
                .toList();

        return new SubmissionFeedbackResponse(
                submission.getId().toString(),
                submission.getUserId().toString(),
                submission.getAssessmentSnapshotId().toString(),
                submission.getStartedAt() != null ? submission.getStartedAt().toString() : null,
                submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString() : null,
                answerResults
        );
    }

    @Transactional(readOnly = true)
    public SubmissionHistoryResponse getSubmissionHistory(UUID userId) {
        List<Submission> submissions = submissionRepository
                .findByUserIdAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(userId);

        if (submissions.isEmpty()) {
            return new SubmissionHistoryResponse(List.of());
        }

        // Batch-fetch snapshot titles
        List<UUID> snapshotIds = submissions.stream()
                .map(Submission::getAssessmentSnapshotId)
                .distinct()
                .toList();
        Map<UUID, AssessmentSnapshot> snapshotMap = assessmentSnapshotRepository
                .findAllById(snapshotIds).stream()
                .collect(Collectors.toMap(AssessmentSnapshot::getId, s -> s));

        // Batch-fetch answers for correct/wrong counts
        List<UUID> submissionIds = submissions.stream()
                .map(Submission::getId)
                .toList();
        Map<UUID, List<UserAnswer>> answersBySubmission =
                userAnswerRepository.findBySubmissionIdIn(submissionIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                UserAnswer::getSubmissionId));

        List<SubmissionSummary> summaries = submissions.stream()
                .map(s -> {
                    AssessmentSnapshot snapshot = snapshotMap
                            .get(s.getAssessmentSnapshotId());
                    String title = snapshot != null
                            ? snapshot.getTitle() : "Unknown";

                    List<UserAnswer> answers = answersBySubmission
                            .getOrDefault(s.getId(), List.of());
                    List<UserAnswer> mcAnswers = answers.stream()
                            .filter(a -> "multiple".equals(a.getType()))
                            .toList();

                    int correct = (int) mcAnswers.stream()
                            .filter(a -> Boolean.TRUE.equals(
                                    a.getIsCorrect()))
                            .count();
                    int wrong = (int) mcAnswers.stream()
                            .filter(a -> Boolean.FALSE.equals(
                                    a.getIsCorrect()))
                            .count();
                    int unanswered = mcAnswers.size() - correct - wrong;

                    return new SubmissionSummary(
                            s.getId().toString(),
                            s.getAssessmentSnapshotId().toString(),
                            title,
                            s.getStartedAt() != null
                                    ? s.getStartedAt().toString()
                                    : null,
                            s.getSubmittedAt().toString(),
                            s.getScore(),
                            mcAnswers.size(),
                            correct,
                            wrong,
                            unanswered
                    );
                })
                .toList();

        return new SubmissionHistoryResponse(summaries);
    }

    @Transactional(readOnly = true)
    public SubmissionReviewResponse getSubmissionReview(UUID submissionId, UUID userId) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (submission.getSubmittedAt() == null) {
            throw new IllegalSubmissionStateException("Submission not yet completed");
        }

        AssessmentSnapshot snapshot = assessmentSnapshotRepository
                .findById(submission.getAssessmentSnapshotId())
                .orElseThrow(() -> new ResourceNotFoundException("Assessment snapshot not found"));

        List<QuestionSnapshot> questions = questionSnapshotRepository
                .findByAssessmentSnapshotIdOrderByPosition(snapshot.getId());

        List<UUID> questionIds = questions.stream()
                .map(QuestionSnapshot::getId)
                .toList();

        // Fetch all options for these questions
        List<OptionSnapshot> allOptions = questionIds.isEmpty()
                ? List.of()
                : optionSnapshotRepository.findByQuestionSnapshotIdInOrderByPosition(questionIds);
        Map<UUID, List<OptionSnapshot>> optionsByQuestion = allOptions.stream()
                .collect(Collectors.groupingBy(OptionSnapshot::getQuestionSnapshotId));

        // Fetch user answers
        List<UserAnswer> answers = userAnswerRepository.findBySubmissionId(submissionId);
        Map<UUID, UserAnswer> answerByQuestion = new HashMap<>();
        for (UserAnswer a : answers) {
            answerByQuestion.put(a.getQuestionSnapshotId(), a);
        }

        // Fetch selected options
        List<UUID> answerIds = answers.stream().map(UserAnswer::getId).toList();
        List<UserAnswerSelectedOption> selectedOptions = answerIds.isEmpty()
                ? List.of()
                : userAnswerSelectedOptionRepository.findByAnswerIdIn(answerIds);
        Map<UUID, List<UserAnswerSelectedOption>> selectedByAnswer = selectedOptions.stream()
                .collect(Collectors.groupingBy(UserAnswerSelectedOption::getAnswerId));

        // Assemble review questions
        List<ReviewQuestion> reviewQuestions = questions.stream()
                .map(q -> {
                    UserAnswer answer = answerByQuestion.get(q.getId());

                    List<String> selectedOptionIds = List.of();
                    if (answer != null) {
                        List<UserAnswerSelectedOption> sel = selectedByAnswer
                                .getOrDefault(answer.getId(), List.of());
                        selectedOptionIds = sel.stream()
                                .map(s -> s.getOptionSnapshotId().toString())
                                .toList();
                    }

                    List<ReviewOption> reviewOptions = optionsByQuestion
                            .getOrDefault(q.getId(), List.of()).stream()
                            .map(o -> new ReviewOption(
                                    o.getId().toString(),
                                    o.getText(),
                                    o.getPosition(),
                                    o.isCorrect()
                            ))
                            .toList();

                    return new ReviewQuestion(
                            q.getId().toString(),
                            q.getType(),
                            q.getText(),
                            q.getCode(),
                            q.getPosition(),
                            answer != null ? answer.getIsCorrect() : null,
                            selectedOptionIds,
                            answer != null ? answer.getText() : null,
                            answer != null ? answer.getMotivation() : null,
                            reviewOptions
                    );
                })
                .toList();

        return new SubmissionReviewResponse(
                submission.getId().toString(),
                snapshot.getTitle(),
                submission.getStartedAt() != null ? submission.getStartedAt().toString() : null,
                submission.getSubmittedAt().toString(),
                submission.getScore(),
                reviewQuestions
        );
    }
}
