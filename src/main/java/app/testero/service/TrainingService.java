package app.testero.service;

import app.testero.dto.TrainingStartRequest;
import app.testero.dto.TrainingStartResponse;
import app.testero.entity.assessment.AssessmentType;
import app.testero.entity.assessment.Difficulty;
import app.testero.entity.assessment.Option;
import app.testero.entity.assessment.Question;
import app.testero.entity.assessment.QuestionSubject;
import app.testero.entity.assessment.Topic;
import app.testero.entity.assessment.TopicSubject;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.snapshot.OptionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshotSubject;
import app.testero.entity.submission.Submission;
import app.testero.entity.submission.SubmissionStatus;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.OptionRepository;
import app.testero.repository.OptionSnapshotRepository;
import app.testero.repository.QuestionRepository;
import app.testero.repository.QuestionSnapshotRepository;
import app.testero.repository.QuestionSnapshotSubjectRepository;
import app.testero.repository.QuestionSubjectRepository;
import app.testero.repository.SubmissionRepository;
import app.testero.repository.TopicRepository;
import app.testero.repository.TopicSubjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TrainingService {

    private static final double MINUTES_PER_QUESTION = 1.5;

    private final TopicRepository topicRepository;
    private final TopicSubjectRepository topicSubjectRepository;
    private final QuestionSubjectRepository questionSubjectRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final AssessmentSnapshotRepository snapshotRepository;
    private final QuestionSnapshotRepository questionSnapshotRepository;
    private final OptionSnapshotRepository optionSnapshotRepository;
    private final QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository;
    private final SubmissionRepository submissionRepository;

    public TrainingService(TopicRepository topicRepository,
                           TopicSubjectRepository topicSubjectRepository,
                           QuestionSubjectRepository questionSubjectRepository,
                           QuestionRepository questionRepository,
                           OptionRepository optionRepository,
                           AssessmentSnapshotRepository snapshotRepository,
                           QuestionSnapshotRepository questionSnapshotRepository,
                           OptionSnapshotRepository optionSnapshotRepository,
                           QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository,
                           SubmissionRepository submissionRepository) {
        this.topicRepository = topicRepository;
        this.topicSubjectRepository = topicSubjectRepository;
        this.questionSubjectRepository = questionSubjectRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.snapshotRepository = snapshotRepository;
        this.questionSnapshotRepository = questionSnapshotRepository;
        this.optionSnapshotRepository = optionSnapshotRepository;
        this.questionSnapshotSubjectRepository = questionSnapshotSubjectRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional
    public TrainingStartResponse startTraining(TrainingStartRequest request, UUID userId) {
        UUID topicId = UUID.fromString(request.topicId());
        Topic topic = topicRepository.findById(topicId)
                .filter(Topic::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Validate chapter IDs belong to the topic
        List<UUID> chapterIds = request.chapterIds().stream().map(UUID::fromString).toList();
        Set<UUID> topicSubjectIds = topicSubjectRepository.findByTopicIdOrderByPositionAsc(topicId)
                .stream()
                .map(TopicSubject::getSubjectId)
                .collect(Collectors.toSet());
        for (UUID chapterId : chapterIds) {
            if (!topicSubjectIds.contains(chapterId)) {
                throw new IllegalArgumentException("Chapter " + chapterId + " does not belong to topic " + topicId);
            }
        }

        // Find questions linked to the selected chapters
        List<QuestionSubject> questionSubjects = questionSubjectRepository.findBySubjectIdIn(chapterIds);
        Set<UUID> candidateQuestionIds = questionSubjects.stream()
                .map(QuestionSubject::getQuestionId)
                .collect(Collectors.toSet());

        // Fetch questions and filter by difficulty
        List<Question> allQuestions = questionRepository.findAllById(candidateQuestionIds);
        Difficulty targetDifficulty = parseDifficulty(request.difficulty());
        List<Question> pool;
        if (targetDifficulty == null) {
            // "mista" — all difficulties
            pool = new ArrayList<>(allQuestions);
        } else {
            pool = allQuestions.stream()
                    .filter(q -> q.getDifficulty() == targetDifficulty)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        if (pool.isEmpty()) {
            throw new IllegalArgumentException("No questions available for the selected chapters and difficulty");
        }

        // Random selection
        Collections.shuffle(pool);
        int count = Math.min(request.questionCount(), pool.size());
        List<Question> selected = pool.subList(0, count);

        // Create a training snapshot
        AssessmentSnapshot snapshot = new AssessmentSnapshot();
        snapshot.setAssessmentId(null); // no parent assessment for training
        snapshot.setContentHash(UUID.randomUUID().toString()); // unique hash per session
        snapshot.setVersion(1);
        snapshot.setTitle("Allenamento — " + topic.getTitle());
        snapshot.setQuestionsPerAssessment(count);
        snapshot.setPtsCorrect(BigDecimal.ONE);
        snapshot.setPtsWrong(BigDecimal.ZERO);
        snapshot.setDifficulty(targetDifficulty);
        snapshot.setType(AssessmentType.TRAINING);
        snapshot.setPassingScore(null);
        snapshot.setPublishedAt(LocalDateTime.now());

        Integer timerMinutes = null;
        if (request.timerEnabled()) {
            timerMinutes = (int) Math.ceil(count * MINUTES_PER_QUESTION);
            snapshot.setTimerMinutes(timerMinutes);
        } else {
            snapshot.setTimerMinutes(0);
        }
        snapshot = snapshotRepository.save(snapshot);

        // Copy questions and options into snapshots
        List<UUID> selectedIds = selected.stream().map(Question::getId).toList();
        Map<UUID, List<Option>> optionsByQuestion = optionRepository
                .findByQuestionIdInOrderByPosition(selectedIds)
                .stream()
                .collect(Collectors.groupingBy(Option::getQuestionId));
        Map<UUID, List<QuestionSubject>> subjectsByQuestion = questionSubjectRepository
                .findByQuestionIdIn(selectedIds)
                .stream()
                .collect(Collectors.groupingBy(QuestionSubject::getQuestionId));

        for (int i = 0; i < selected.size(); i++) {
            Question q = selected.get(i);
            QuestionSnapshot qs = new QuestionSnapshot();
            qs.setAssessmentSnapshotId(snapshot.getId());
            qs.setOriginalQuestionId(q.getId());
            qs.setType(q.getType());
            qs.setText(q.getText());
            qs.setCode(q.getCode());
            qs.setExplanation(q.getExplanation());
            qs.setPosition(i);
            qs.setPoints(q.getPoints() != null ? q.getPoints() : BigDecimal.ONE);
            qs = questionSnapshotRepository.save(qs);

            for (Option o : optionsByQuestion.getOrDefault(q.getId(), List.of())) {
                OptionSnapshot os = new OptionSnapshot();
                os.setQuestionSnapshotId(qs.getId());
                os.setOriginalOptionId(o.getId());
                os.setText(o.getText());
                os.setCorrect(o.isCorrect());
                os.setFallback(o.isFallback());
                os.setPosition(o.getPosition());
                optionSnapshotRepository.save(os);
            }

            for (QuestionSubject qsub : subjectsByQuestion.getOrDefault(q.getId(), List.of())) {
                QuestionSnapshotSubject qss = new QuestionSnapshotSubject();
                qss.setQuestionSnapshotId(qs.getId());
                qss.setSubjectId(qsub.getSubjectId());
                questionSnapshotSubjectRepository.save(qss);
            }
        }

        // Create submission
        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setAssessmentSnapshotId(snapshot.getId());
        submission.setStatus(SubmissionStatus.IN_PROGRESS);
        submission.setStartedAt(LocalDateTime.now());
        submission = submissionRepository.save(submission);

        log.info("Training started: submissionId={}, userId={}, topicId={}, questions={}",
                submission.getId(), userId, topicId, count);

        return new TrainingStartResponse(
                submission.getId().toString(),
                snapshot.getId().toString(),
                timerMinutes,
                count
        );
    }

    private Difficulty parseDifficulty(String value) {
        if (value == null || value.isBlank() || "mista".equalsIgnoreCase(value)) {
            return null;
        }
        return switch (value.toUpperCase()) {
            case "BASE", "BEGINNER" -> Difficulty.BEGINNER;
            case "INTERMEDIO", "INTERMEDIATE" -> Difficulty.INTERMEDIATE;
            case "AVANZATO", "ADVANCED" -> Difficulty.ADVANCED;
            default -> null;
        };
    }
}
