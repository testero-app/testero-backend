package app.testero.service.submission;
import app.testero.service.notification.NotificationService;

import app.testero.dto.submission.AnswerInput;
import app.testero.dto.submission.SaveAnswerRequest;
import app.testero.dto.submission.SavedAnswersResponse;
import app.testero.dto.submission.SavedAnswersResponse.SavedAnswer;
import app.testero.dto.assessment.SubjectDto;
import app.testero.dto.submission.SubmissionFeedbackResponse;
import app.testero.dto.submission.SubmissionFeedbackResponse.AnswerResult;
import app.testero.dto.submission.SubmissionFeedbackResponse.SubjectScore;
import app.testero.dto.common.PaginationMetadata;
import app.testero.dto.submission.SubmissionHistoryResponse;
import app.testero.dto.submission.SubmissionHistoryResponse.SubmissionSummary;
import app.testero.dto.submission.SubmissionReviewResponse;
import app.testero.dto.submission.SubmissionReviewResponse.ReviewOption;
import app.testero.dto.submission.SubmissionReviewResponse.ReviewQuestion;
import app.testero.dto.submission.SubmissionStartResponse;
import app.testero.dto.submission.SubmissionSubmitRequest;
import app.testero.dto.assessment.AssessmentQuestionsResponse;
import app.testero.dto.assessment.AssessmentQuestionsResponse.QuestionDto;
import app.testero.entity.assessment.AssessmentType;
import app.testero.entity.assessment.Subject;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.user.AppUser;
import app.testero.entity.user.NotificationType;
import app.testero.entity.snapshot.OptionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshotSubject;
import app.testero.entity.submission.Submission;
import app.testero.entity.submission.SubmissionQuestion;
import app.testero.entity.submission.SubmissionStatus;
import app.testero.entity.submission.UserAnswer;
import app.testero.entity.submission.UserAnswerSelectedOption;
import app.testero.event.SubmissionCompletedEvent;
import app.testero.event.SubmissionStartedEvent;
import app.testero.exception.IllegalSubmissionStateException;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.assessment.AssessmentSnapshotRepository;
import app.testero.repository.assessment.OptionSnapshotRepository;
import app.testero.repository.assessment.QuestionSnapshotRepository;
import app.testero.repository.assessment.QuestionSnapshotSubjectRepository;
import app.testero.repository.assessment.SubjectRepository;
import app.testero.repository.user.AppUserRepository;
import app.testero.repository.submission.SubmissionQuestionRepository;
import app.testero.repository.submission.SubmissionRepository;
import app.testero.repository.submission.UserAnswerRepository;
import app.testero.repository.submission.UserAnswerSelectedOptionRepository;
import app.testero.service.assessment.AssessmentService;
import app.testero.service.assessment.QuestionPrepService;
import app.testero.service.submission.ScoringService.ScoringResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserAnswerSelectedOptionRepository userAnswerSelectedOptionRepository;
    private final OptionSnapshotRepository optionSnapshotRepository;
    private final AssessmentSnapshotRepository assessmentSnapshotRepository;
    private final QuestionSnapshotRepository questionSnapshotRepository;
    private final QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final SubmissionQuestionRepository submissionQuestionRepository;
    private final AppUserRepository appUserRepository;
    private final ScoringService scoringService;
    private final NotificationService notificationService;
    private final AssessmentService assessmentService;
    private final QuestionPrepService questionPrepService;
    private final MessageSource messageSource;
    private final ApplicationEventPublisher eventPublisher;

    public SubmissionService(SubmissionRepository submissionRepository,
                             UserAnswerRepository userAnswerRepository,
                             UserAnswerSelectedOptionRepository userAnswerSelectedOptionRepository,
                             OptionSnapshotRepository optionSnapshotRepository,
                             AssessmentSnapshotRepository assessmentSnapshotRepository,
                             QuestionSnapshotRepository questionSnapshotRepository,
                             QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository,
                             SubjectRepository subjectRepository,
                             SubmissionQuestionRepository submissionQuestionRepository,
                             AppUserRepository appUserRepository,
                             ScoringService scoringService,
                             NotificationService notificationService,
                             AssessmentService assessmentService,
                             QuestionPrepService questionPrepService,
                             MessageSource messageSource,
                             ApplicationEventPublisher eventPublisher) {
        this.submissionRepository = submissionRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.userAnswerSelectedOptionRepository = userAnswerSelectedOptionRepository;
        this.optionSnapshotRepository = optionSnapshotRepository;
        this.assessmentSnapshotRepository = assessmentSnapshotRepository;
        this.questionSnapshotRepository = questionSnapshotRepository;
        this.questionSnapshotSubjectRepository = questionSnapshotSubjectRepository;
        this.subjectRepository = subjectRepository;
        this.submissionQuestionRepository = submissionQuestionRepository;
        this.appUserRepository = appUserRepository;
        this.scoringService = scoringService;
        this.notificationService = notificationService;
        this.assessmentService = assessmentService;
        this.questionPrepService = questionPrepService;
        this.messageSource = messageSource;
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

        // Enforce the attempt limit frozen into the snapshot. null = unlimited. Checked only
        // once past the idempotent branch above, so resuming an in-progress attempt never
        // counts against the limit — every remaining submission here is a finished attempt.
        Integer maxAttempts = snapshot.getMaxAttempts();
        if (maxAttempts != null) {
            long attemptsMade = submissionRepository
                    .countByAssessmentSnapshotIdAndUserId(assessmentSnapshotId, userId);
            if (attemptsMade >= maxAttempts) {
                throw new IllegalSubmissionStateException(
                        "Maximum number of attempts (" + maxAttempts + ") reached for this assessment");
            }
        }

        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setAssessmentSnapshotId(assessmentSnapshotId);
        submission.setStatus(SubmissionStatus.IN_PROGRESS);
        submission.setStartedAt(LocalDateTime.now());
        // Freeze the randomisation seed now: the question draw for this submission is derived
        // from it, so it stays identical across reloads and resumes for its whole lifetime.
        submission.setSeed(ThreadLocalRandom.current().nextLong());
        submission = submissionRepository.save(submission);

        log.info("Submission started: submissionId={}, userId={}, snapshotId={}",
                submission.getId(), userId, assessmentSnapshotId);

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

        // The question must be one of those administered: for a sitting, it belongs to the
        // submission's snapshot; for a free training session, which has none, it is one of the
        // questions drawn for it.
        boolean administered = submission.getAssessmentSnapshotId() == null
                ? submissionQuestionRepository
                        .existsBySubmissionIdAndQuestionSnapshotId(submissionId, questionSnapshotId)
                : questionSnapshotRepository.findById(questionSnapshotId)
                        .filter(q -> q.getAssessmentSnapshotId()
                                .equals(submission.getAssessmentSnapshotId()))
                        .isPresent();
        if (!administered) {
            throw new ResourceNotFoundException("Question not found in this assessment");
        }

        // Upsert: find existing or create new
        Optional<UserAnswer> existingAnswer = userAnswerRepository
                .findBySubmissionIdAndQuestionSnapshotId(submissionId, questionSnapshotId);

        UserAnswer answer;
        if (existingAnswer.isPresent()) {
            answer = existingAnswer.get();
            answer.setType(request.type());
            answer.setText(request.text() != null ? request.text() : "");
            answer.setMotivation(request.motivation() != null ? request.motivation() : "");
            answer.setFlagged(Boolean.TRUE.equals(request.flagged()));
            // Clear old selected options
            userAnswerSelectedOptionRepository.deleteByAnswerId(answer.getId());
        } else {
            answer = new UserAnswer();
            answer.setSubmissionId(submissionId);
            answer.setQuestionSnapshotId(questionSnapshotId);
            answer.setType(request.type());
            answer.setText(request.text() != null ? request.text() : "");
            answer.setMotivation(request.motivation() != null ? request.motivation() : "");
            answer.setFlagged(Boolean.TRUE.equals(request.flagged()));
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

        // 2. Upsert answers: reuse pre-existing answers from incremental saves
        Map<UUID, UserAnswer> existingByQuestion = userAnswerRepository
                .findBySubmissionId(submissionId).stream()
                .collect(Collectors.toMap(UserAnswer::getQuestionSnapshotId, a -> a));

        List<UserAnswer> answers = new ArrayList<>();
        List<UUID> existingAnswerIds = new ArrayList<>();
        for (AnswerInput input : request.answers()) {
            UUID qsId = UUID.fromString(input.questionId());
            UserAnswer answer = existingByQuestion.getOrDefault(qsId, new UserAnswer());
            if (answer.getId() != null) {
                existingAnswerIds.add(answer.getId());
            } else {
                answer.setSubmissionId(submission.getId());
                answer.setQuestionSnapshotId(qsId);
            }
            answer.setType(input.type());
            answer.setText(input.text() != null ? input.text() : "");
            answer.setMotivation(input.motivation() != null ? input.motivation() : "");
            answers.add(answer);
        }
        if (!existingAnswerIds.isEmpty()) {
            userAnswerSelectedOptionRepository.deleteByAnswerIdIn(existingAnswerIds);
            userAnswerSelectedOptionRepository.flush();
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
        ScoringResult scoringResult = scoringService.scoreSubmission(submission, answers, selectedOptions);

        log.info("Submission submitted: submissionId={}, userId={}, score={}",
                submission.getId(), userId, submission.getScore());

        // 6. Notify scheduler to cancel auto-close
        eventPublisher.publishEvent(new SubmissionCompletedEvent(submission.getId()));

        AssessmentSnapshot snapshot = snapshotOf(submission);

        // Notify student of result (non-training assessments only)
        if (snapshot != null && snapshot.getType() != AssessmentType.TRAINING) {
            NotificationType notifEvent = snapshot.getType() == AssessmentType.EXAM
                    ? NotificationType.EXAM_RESULT
                    : NotificationType.CERT_SIMULATION_RESULT;
            String titleKey = snapshot.getType() == AssessmentType.EXAM
                    ? "notification.exam_result.title"
                    : "notification.cert_result.title";
            notificationService.notify(submission.getUserId(), notifEvent,
                    titleKey, "notification.result.message",
                    submission.getId(),
                    snapshot.getTitle(), Math.round(submission.getScore()));
        }

        // TODO: ALL_SUBMITTED notification for teachers.
        // When all students in a class have submitted for this snapshot,
        // notify the class teacher(s) via NotificationType.ALL_SUBMITTED.
        // Deferred until teacher UI is available.

        // Fetch question snapshots for accurate maxScore
        List<QuestionSnapshot> questionSnapshots = questionSnapshotsOf(answers);

        return new SubmissionFeedbackResponse(
                submission.getId().toString(),
                submission.getUserId().toString(),
                idOrNull(submission.getAssessmentSnapshotId()),
                submission.getStartedAt() != null ? submission.getStartedAt().toString() : null,
                submission.getSubmittedAt().toString(),
                submission.getScore(),
                computeMaxScore(snapshot, questionSnapshots),
                computePassed(submission.getScore(), snapshot),
                snapshot != null && snapshot.getPassingScore() != null
                        ? snapshot.getPassingScore().doubleValue() : null,
                scoringResult.answerResults(),
                scoringResult.subjectScores()
        );
    }

    @Transactional
    public void autoCloseSubmission(UUID submissionId) {
        Submission submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission == null || submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            return;
        }

        log.warn("Auto-closing submission: submissionId={}, userId={}",
                submissionId, submission.getUserId());

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

    @Transactional(readOnly = true)
    public SavedAnswersResponse getSavedAnswers(UUID submissionId, UUID userId) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalSubmissionStateException("Submission is not in progress");
        }

        List<UserAnswer> answers = userAnswerRepository.findBySubmissionId(submissionId);

        if (answers.isEmpty()) {
            return new SavedAnswersResponse(List.of());
        }

        List<UUID> answerIds = answers.stream().map(UserAnswer::getId).toList();
        Map<UUID, List<UserAnswerSelectedOption>> selectedByAnswer =
                userAnswerSelectedOptionRepository.findByAnswerIdIn(answerIds).stream()
                        .collect(Collectors.groupingBy(UserAnswerSelectedOption::getAnswerId));

        List<SavedAnswer> savedAnswers = answers.stream()
                .map(a -> new SavedAnswer(
                        a.getQuestionSnapshotId().toString(),
                        a.getType(),
                        a.getText(),
                        a.getMotivation(),
                        selectedByAnswer.getOrDefault(a.getId(), List.of()).stream()
                                .map(o -> o.getOptionSnapshotId().toString())
                                .toList(),
                        a.isFlagged()
                ))
                .toList();

        return new SavedAnswersResponse(savedAnswers);
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
                        correctMap.getOrDefault(a.getQuestionSnapshotId(), List.of()),
                        a.getPointsAwarded()
                ))
                .toList();

        AssessmentSnapshot feedbackSnapshot = snapshotOf(submission);

        // Fetch question snapshots for accurate maxScore
        List<QuestionSnapshot> questionSnapshots = questionSnapshotsOf(answers);

        // Compute subject scores via ScoringService
        List<SubjectScore> subjectScores = scoringService.computeSubjectScores(
                answers, submission.getAssessmentSnapshotId());

        return new SubmissionFeedbackResponse(
                submission.getId().toString(),
                submission.getUserId().toString(),
                idOrNull(submission.getAssessmentSnapshotId()),
                submission.getStartedAt() != null ? submission.getStartedAt().toString() : null,
                submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString() : null,
                submission.getScore(),
                computeMaxScore(feedbackSnapshot, questionSnapshots),
                computePassed(submission.getScore(), feedbackSnapshot),
                feedbackSnapshot != null && feedbackSnapshot.getPassingScore() != null
                        ? feedbackSnapshot.getPassingScore().doubleValue() : null,
                answerResults,
                subjectScores
        );
    }

    /**
     * The questions of a session, whichever kind it is.
     *
     * <p>A sitting delegates to the assessment: its paper is drawn from the published snapshot
     * and frozen by the seed. A free training session has no snapshot — its paper was drawn
     * across the pools its class may practise on and recorded at start, so it is replayed from
     * {@code submission_question}, in the order drawn.
     */
    @Transactional(readOnly = true)
    public AssessmentQuestionsResponse getSubmissionQuestions(UUID submissionId, UUID userId) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (submission.getAssessmentSnapshotId() != null) {
            return assessmentService.getAssessmentQuestions(
                    submission.getAssessmentSnapshotId().toString(), userId);
        }

        List<UUID> drawn = submissionQuestionRepository
                .findBySubmissionIdOrderByPositionAsc(submissionId).stream()
                .map(SubmissionQuestion::getQuestionSnapshotId)
                .toList();
        Map<UUID, QuestionSnapshot> byId = drawn.isEmpty()
                ? Map.of()
                : questionSnapshotRepository.findByIdIn(drawn).stream()
                        .collect(Collectors.toMap(QuestionSnapshot::getId, q -> q));
        List<QuestionSnapshot> questions = drawn.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();

        // The question order is the one drawn at start; only the options are shuffled, with the
        // submission's seed, so a reload replays the identical paper.
        List<QuestionDto> prepared = questionPrepService.prepare(
                new ArrayList<>(assessmentService.toQuestionDtos(questions)),
                questions.size(), false, true, submission.getSeed());

        return new AssessmentQuestionsResponse(
                null,
                freeTrainingTitle(userId),
                null,
                null,
                submission.getTimerMinutes() == null ? 0 : submission.getTimerMinutes(),
                prepared.size(),
                prepared
        );
    }

    @Transactional(readOnly = true)
    public SubmissionHistoryResponse getSubmissionHistory(UUID userId, int page, int size) {
        Page<Submission> submissionPage = submissionRepository
                .findByUserIdAndStatusInOrderBySubmittedAtDesc(
                        userId,
                        List.of(SubmissionStatus.SUBMITTED, SubmissionStatus.AUTO_CLOSED),
                        PageRequest.of(page, size));

        List<Submission> submissions = submissionPage.getContent();

        if (submissions.isEmpty()) {
            PaginationMetadata pagination = new PaginationMetadata(
                    0, 0, page, size);
            return new SubmissionHistoryResponse(List.of(), pagination);
        }

        // Batch-fetch snapshot titles
        List<UUID> snapshotIds = submissions.stream()
                .map(Submission::getAssessmentSnapshotId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, AssessmentSnapshot> snapshotMap = assessmentSnapshotRepository
                .findAllById(snapshotIds).stream()
                .collect(Collectors.toMap(AssessmentSnapshot::getId, s -> s));

        // Batch-fetch answers for correct/wrong counts
        List<UUID> submissionIds = submissions.stream()
                .map(Submission::getId)
                .toList();
        List<UserAnswer> allAnswers = userAnswerRepository.findBySubmissionIdIn(submissionIds);
        Map<UUID, List<UserAnswer>> answersBySubmission = allAnswers.stream()
                .collect(Collectors.groupingBy(UserAnswer::getSubmissionId));

        // Batch-fetch the question snapshots actually drawn, for an accurate maxScore
        Map<UUID, QuestionSnapshot> questionSnapshotById = questionSnapshotRepository
                .findByIdIn(allAnswers.stream()
                        .map(UserAnswer::getQuestionSnapshotId)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(QuestionSnapshot::getId, qs -> qs));

        // Batch-compute the per-subject breakdown, so the history detail shows the same
        // chart the student saw right after submitting
        Map<UUID, Double> ptsCorrectBySubmission = submissions.stream()
                .filter(s -> s.getAssessmentSnapshotId() != null
                        && snapshotMap.containsKey(s.getAssessmentSnapshotId()))
                .collect(Collectors.toMap(
                        Submission::getId,
                        s -> snapshotMap.get(s.getAssessmentSnapshotId())
                                .getPtsCorrect().doubleValue(),
                        (a, b) -> a));
        Map<UUID, List<SubjectScore>> subjectScoresBySubmission =
                scoringService.computeSubjectScoresBySubmission(
                        answersBySubmission, questionSnapshotById, ptsCorrectBySubmission);

        List<SubmissionSummary> summaries = submissions.stream()
                .map(s -> {
                    AssessmentSnapshot snapshot = s.getAssessmentSnapshotId() == null
                            ? null
                            : snapshotMap.get(s.getAssessmentSnapshotId());
                    // A session with no snapshot is free training: it is named for what it is,
                    // in the student's language, since there is no assessment to name it after.
                    String title = snapshot != null
                            ? snapshot.getTitle()
                            : freeTrainingTitle(userId);
                    String type = snapshot != null && snapshot.getType() != null
                            ? snapshot.getType().name()
                            : AssessmentType.TRAINING.name();

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
                            idOrNull(s.getAssessmentSnapshotId()),
                            title,
                            type,
                            s.getStartedAt() != null
                                    ? s.getStartedAt().toString()
                                    : null,
                            s.getSubmittedAt() != null
                                    ? s.getSubmittedAt().toString()
                                    : null,
                            s.getScore(),
                            computeMaxScore(snapshot, answers.stream()
                                    .map(a -> questionSnapshotById.get(a.getQuestionSnapshotId()))
                                    .filter(Objects::nonNull)
                                    .toList()),
                            computePassed(s.getScore(), snapshot),
                            mcAnswers.size(),
                            correct,
                            wrong,
                            unanswered,
                            subjectScoresBySubmission.getOrDefault(s.getId(), List.of())
                    );
                })
                .toList();

        PaginationMetadata pagination = new PaginationMetadata(
                submissionPage.getTotalElements(),
                submissionPage.getTotalPages(),
                submissionPage.getNumber(),
                submissionPage.getSize());

        return new SubmissionHistoryResponse(summaries, pagination);
    }

    @Transactional(readOnly = true)
    public SubmissionReviewResponse getSubmissionReview(UUID submissionId, UUID userId) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (submission.getStatus() == SubmissionStatus.IN_PROGRESS) {
            throw new IllegalSubmissionStateException("Submission not yet completed");
        }

        AssessmentSnapshot snapshot = snapshotOf(submission);

        // Fetch user answers first: they identify the questions this submission drew
        List<UserAnswer> answers = userAnswerRepository.findBySubmissionId(submissionId);
        Map<UUID, UserAnswer> answerByQuestion = new HashMap<>();
        for (UserAnswer a : answers) {
            answerByQuestion.put(a.getQuestionSnapshotId(), a);
        }

        List<QuestionSnapshot> questions = questionSnapshotsOf(answers);

        List<UUID> questionIds = questions.stream()
                .map(QuestionSnapshot::getId)
                .toList();

        // Fetch all options for these questions
        List<OptionSnapshot> allOptions = questionIds.isEmpty()
                ? List.of()
                : optionSnapshotRepository.findByQuestionSnapshotIdInOrderByPosition(questionIds);
        Map<UUID, List<OptionSnapshot>> optionsByQuestion = allOptions.stream()
                .collect(Collectors.groupingBy(OptionSnapshot::getQuestionSnapshotId));

        // Fetch selected options
        List<UUID> answerIds = answers.stream().map(UserAnswer::getId).toList();
        List<UserAnswerSelectedOption> selectedOptions = answerIds.isEmpty()
                ? List.of()
                : userAnswerSelectedOptionRepository.findByAnswerIdIn(answerIds);
        Map<UUID, List<UserAnswerSelectedOption>> selectedByAnswer = selectedOptions.stream()
                .collect(Collectors.groupingBy(UserAnswerSelectedOption::getAnswerId));

        // Fetch subjects for each question snapshot
        List<QuestionSnapshotSubject> qsSubjects = questionIds.isEmpty()
                ? List.of()
                : questionSnapshotSubjectRepository.findByQuestionSnapshotIdIn(questionIds);
        Map<UUID, List<QuestionSnapshotSubject>> subjectsByQuestion = qsSubjects.stream()
                .collect(Collectors.groupingBy(QuestionSnapshotSubject::getQuestionSnapshotId));

        // Fetch subject labels
        List<UUID> subjectIds = qsSubjects.stream()
                .map(QuestionSnapshotSubject::getSubjectId)
                .distinct()
                .toList();
        Map<UUID, String> subjectLabels = subjectIds.isEmpty()
                ? Map.of()
                : subjectRepository.findByIdIn(subjectIds).stream()
                        .collect(Collectors.toMap(Subject::getId, Subject::getLabel));

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

                    Double questionPoints = q.getPoints() != null
                            ? q.getPoints().doubleValue() : null;
                    Double pointsAwarded = answer != null ? answer.getPointsAwarded() : null;

                    List<SubjectDto> questionSubjects = subjectsByQuestion
                            .getOrDefault(q.getId(), List.of()).stream()
                            .map(qs -> new SubjectDto(
                                    qs.getSubjectId().toString(),
                                    subjectLabels.getOrDefault(qs.getSubjectId(), "Unknown")
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
                            reviewOptions,
                            questionPoints,
                            pointsAwarded,
                            questionSubjects
                    );
                })
                .toList();

        return new SubmissionReviewResponse(
                submission.getId().toString(),
                snapshot != null ? snapshot.getTitle() : freeTrainingTitle(userId),
                submission.getStartedAt() != null ? submission.getStartedAt().toString() : null,
                submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString() : null,
                submission.getScore(),
                computeMaxScore(snapshot, questions),
                reviewQuestions
        );
    }

    /**
     * Compute max score. If questionSnapshots are provided, sum per-question points
     * (falling back to ptsCorrect for questions without custom points).
     * If questionSnapshots are null, use the legacy approximation.
     */
    /**
     * The question snapshots a submission actually drew, resolved from its answers.
     * An assessment snapshot holds the whole question pool while each submission draws
     * only {@code questionsPerAssessment} of them, so the pool must not be used to
     * measure a single submission.
     */
    private List<QuestionSnapshot> questionSnapshotsOf(List<UserAnswer> answers) {
        List<UUID> ids = answers.stream()
                .map(UserAnswer::getQuestionSnapshotId)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return questionSnapshotRepository.findByIdIn(ids).stream()
                .sorted(Comparator.comparingInt(QuestionSnapshot::getPosition))
                .toList();
    }

    /** How a session with no assessment is named, in the student's own language. */
    private String freeTrainingTitle(UUID userId) {
        String language = appUserRepository.findById(userId)
                .map(AppUser::getLanguage)
                .orElse("it");
        return messageSource.getMessage("training.free.title", null, Locale.of(language));
    }

    /** The published assessment behind a submission, or null for a free training session. */
    private AssessmentSnapshot snapshotOf(Submission submission) {
        return submission.getAssessmentSnapshotId() == null
                ? null
                : assessmentSnapshotRepository.findById(submission.getAssessmentSnapshotId())
                        .orElse(null);
    }

    private static String idOrNull(UUID id) {
        return id == null ? null : id.toString();
    }

    private static Double computeMaxScore(AssessmentSnapshot snapshot,
                                           List<QuestionSnapshot> questionSnapshots) {
        if (snapshot == null) {
            // Free training: no snapshot to read the rules from, so the maximum is what the
            // questions themselves are worth, a point each unless they say otherwise.
            if (questionSnapshots == null || questionSnapshots.isEmpty()) {
                return null;
            }
            double total = 0.0;
            for (QuestionSnapshot qs : questionSnapshots) {
                total += (qs.getPoints() != null) ? qs.getPoints().doubleValue() : 1.0;
            }
            return total;
        }
        if (questionSnapshots != null && !questionSnapshots.isEmpty()) {
            double ptsCorrect = snapshot.getPtsCorrect().doubleValue();
            double total = 0.0;
            for (QuestionSnapshot qs : questionSnapshots) {
                total += (qs.getPoints() != null) ? qs.getPoints().doubleValue() : ptsCorrect;
            }
            return total;
        }
        // Fallback: legacy approximation for list views
        return snapshot.getQuestionsPerAssessment() * snapshot.getPtsCorrect().doubleValue();
    }

    private static Boolean computePassed(Double score, AssessmentSnapshot snapshot) {
        if (snapshot == null || snapshot.getPassingScore() == null || score == null) {
            return null;
        }
        return score >= snapshot.getPassingScore().doubleValue();
    }
}
