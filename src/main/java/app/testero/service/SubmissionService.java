package app.testero.service;

import app.testero.dto.AnswerInput;
import app.testero.dto.SaveAnswerRequest;
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
import app.testero.entity.submission.SubmissionStatus;
import app.testero.entity.submission.UserAnswer;
import app.testero.entity.submission.UserAnswerSelectedOption;
import app.testero.event.SubmissionCompletedEvent;
import app.testero.event.SubmissionStartedEvent;
import app.testero.exception.IllegalSubmissionStateException;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.OptionSnapshotRepository;
import app.testero.repository.QuestionSnapshotRepository;
import app.testero.repository.SubmissionRepository;
import app.testero.repository.UserAnswerRepository;
import app.testero.repository.UserAnswerSelectedOptionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final ScoringService scoringService;
    private final ApplicationEventPublisher eventPublisher;

    public SubmissionService(SubmissionRepository submissionRepository,
                             UserAnswerRepository userAnswerRepository,
                             UserAnswerSelectedOptionRepository userAnswerSelectedOptionRepository,
                             OptionSnapshotRepository optionSnapshotRepository,
                             AssessmentSnapshotRepository assessmentSnapshotRepository,
                             QuestionSnapshotRepository questionSnapshotRepository,
                             ScoringService scoringService,
                             ApplicationEventPublisher eventPublisher) {
        this.submissionRepository = submissionRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.userAnswerSelectedOptionRepository = userAnswerSelectedOptionRepository;
        this.optionSnapshotRepository = optionSnapshotRepository;
        this.assessmentSnapshotRepository = assessmentSnapshotRepository;
        this.questionSnapshotRepository = questionSnapshotRepository;
        this.scoringService = scoringService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SubmissionStartResponse startSubmission(UUID assessmentSnapshotId, UUID userId) {
        AssessmentSnapshot snapshot = assessmentSnapshotRepository.findById(assessmentSnapshotId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment snapshot not found"));

        // Idempotent: if the user has an in-progress submission for this snapshot, return it.
        Optional<Submission> existing = submissionRepository
                .findByAssessmentSnapshotIdAndUserIdAndStatus(
                        assessmentSnapshotId, userId, SubmissionStatus.IN_PROGRESS);
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
        submission.setStatus(SubmissionStatus.IN_PROGRESS);
        submission.setStartedAt(LocalDateTime.now());
        submission = submissionRepository.save(submission);

        // Schedule auto-close at startedAt + timerMinutes + 1s grace
        eventPublisher.publishEvent(new SubmissionStartedEvent(
                submission.getId(),
                submission.getStartedAt()
                        .plusMinutes(snapshot.getTimerMinutes())
                        .plusSeconds(1)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        ));

        return new SubmissionStartResponse(
                submission.getId().toString(),
                submission.getStartedAt().toString()
        );
    }

    @Transactional
    public void saveAnswer(UUID submissionId, UUID questionSnapshotId,
                           UUID userId, SaveAnswerRequest request) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalSubmissionStateException("Submission is not in progress");
        }

        // Validate question belongs to this submission's assessment snapshot
        questionSnapshotRepository.findById(questionSnapshotId)
                .filter(q -> q.getAssessmentSnapshotId().equals(submission.getAssessmentSnapshotId()))
                .orElseThrow(() -> new ResourceNotFoundException("Question not found in this assessment"));

        // Upsert: find existing or create new
        Optional<UserAnswer> existingAnswer = userAnswerRepository
                .findBySubmissionIdAndQuestionSnapshotId(submissionId, questionSnapshotId);

        UserAnswer answer;
        if (existingAnswer.isPresent()) {
            answer = existingAnswer.get();
            answer.setType(request.type());
            answer.setText(request.text() != null ? request.text() : "");
            answer.setMotivation(request.motivation() != null ? request.motivation() : "");
            // Clear old selected options
            userAnswerSelectedOptionRepository.deleteByAnswerId(answer.getId());
        } else {
            answer = new UserAnswer();
            answer.setSubmissionId(submissionId);
            answer.setQuestionSnapshotId(questionSnapshotId);
            answer.setType(request.type());
            answer.setText(request.text() != null ? request.text() : "");
            answer.setMotivation(request.motivation() != null ? request.motivation() : "");
        }
        answer = userAnswerRepository.save(answer);

        // Save selected options
        if (!request.selectedOptionIds().isEmpty()) {
            List<UserAnswerSelectedOption> options = new ArrayList<>();
            for (String optionId : request.selectedOptionIds()) {
                UserAnswerSelectedOption aso = new UserAnswerSelectedOption();
                aso.setAnswerId(answer.getId());
                aso.setOptionSnapshotId(UUID.fromString(optionId));
                options.add(aso);
            }
            userAnswerSelectedOptionRepository.saveAll(options);
        }
    }

    @Transactional
    public SubmissionFeedbackResponse submitAnswers(UUID submissionId, UUID userId,
                                                     SubmissionSubmitRequest request) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalSubmissionStateException("Submission already completed");
        }

        // 1. Set submitted timestamp and status
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(SubmissionStatus.SUBMITTED);

        // 2. Delete any pre-existing answers from incremental saves
        List<UserAnswer> existingAnswers = userAnswerRepository.findBySubmissionId(submissionId);
        if (!existingAnswers.isEmpty()) {
            List<UUID> existingAnswerIds = existingAnswers.stream()
                    .map(UserAnswer::getId).toList();
            if (!existingAnswerIds.isEmpty()) {
                userAnswerSelectedOptionRepository.findByAnswerIdIn(existingAnswerIds)
                        .forEach(aso -> userAnswerSelectedOptionRepository.delete(aso));
            }
            userAnswerRepository.deleteAll(existingAnswers);
        }

        // 3. Create answer records (referencing question snapshots)
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

        // 4. Create answer_selected_option records (referencing option snapshots)
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

        // 5. Score and save
        List<AnswerResult> answerResults = scoringService.scoreSubmission(submission, answers, selectedOptions);

        // 6. Notify scheduler to cancel auto-close
        eventPublisher.publishEvent(new SubmissionCompletedEvent(submission.getId()));

        AssessmentSnapshot snapshot = assessmentSnapshotRepository
                .findById(submission.getAssessmentSnapshotId())
                .orElse(null);

        return new SubmissionFeedbackResponse(
                submission.getId().toString(),
                submission.getUserId().toString(),
                submission.getAssessmentSnapshotId().toString(),
                submission.getStartedAt() != null ? submission.getStartedAt().toString() : null,
                submission.getSubmittedAt().toString(),
                submission.getScore(),
                computePassed(submission.getScore(), snapshot),
                snapshot != null && snapshot.getPassingScore() != null
                        ? snapshot.getPassingScore().doubleValue() : null,
                answerResults
        );
    }

    @Transactional
    public void autoCloseSubmission(UUID submissionId) {
        Submission submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission == null || submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            return;
        }

        submission.setStatus(SubmissionStatus.AUTO_CLOSED);
        submission.setSubmittedAt(LocalDateTime.now());

        List<UserAnswer> answers = userAnswerRepository.findBySubmissionId(submissionId);

        if (!answers.isEmpty()) {
            // Load selected options for scoring
            List<UUID> answerIds = answers.stream().map(UserAnswer::getId).toList();
            List<UserAnswerSelectedOption> selectedOptions =
                    userAnswerSelectedOptionRepository.findByAnswerIdIn(answerIds);
            scoringService.scoreSubmission(submission, answers, selectedOptions);
        } else {
            submission.setScore(0.0);
            submissionRepository.save(submission);
        }
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

        AssessmentSnapshot feedbackSnapshot = assessmentSnapshotRepository
                .findById(submission.getAssessmentSnapshotId())
                .orElse(null);

        return new SubmissionFeedbackResponse(
                submission.getId().toString(),
                submission.getUserId().toString(),
                submission.getAssessmentSnapshotId().toString(),
                submission.getStartedAt() != null ? submission.getStartedAt().toString() : null,
                submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString() : null,
                submission.getScore(),
                computePassed(submission.getScore(), feedbackSnapshot),
                feedbackSnapshot != null && feedbackSnapshot.getPassingScore() != null
                        ? feedbackSnapshot.getPassingScore().doubleValue() : null,
                answerResults
        );
    }

    @Transactional(readOnly = true)
    public SubmissionHistoryResponse getSubmissionHistory(UUID userId) {
        List<Submission> submissions = submissionRepository
                .findByUserIdAndStatusInOrderBySubmittedAtDesc(
                        userId,
                        List.of(SubmissionStatus.SUBMITTED, SubmissionStatus.AUTO_CLOSED));

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
                            s.getSubmittedAt() != null
                                    ? s.getSubmittedAt().toString()
                                    : null,
                            s.getScore(),
                            computePassed(s.getScore(), snapshot),
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

        if (submission.getStatus() == SubmissionStatus.IN_PROGRESS) {
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
                            q.getExplanation(),
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
                submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString() : null,
                submission.getScore(),
                reviewQuestions
        );
    }

    private static Boolean computePassed(Double score, AssessmentSnapshot snapshot) {
        if (snapshot == null || snapshot.getPassingScore() == null || score == null) {
            return null;
        }
        return score >= snapshot.getPassingScore().doubleValue();
    }
}
