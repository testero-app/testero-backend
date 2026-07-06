package app.testero.service;

import app.testero.dto.TrainingStartRequest;
import app.testero.dto.TrainingStartResponse;
import app.testero.entity.assessment.AssessmentType;
import app.testero.entity.assessment.Difficulty;
import app.testero.entity.assessment.OptionTemplate;
import app.testero.entity.assessment.QuestionTemplate;
import app.testero.entity.assessment.QuestionTemplateSubject;
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
import app.testero.repository.OptionTemplateRepository;
import app.testero.repository.OptionSnapshotRepository;
import app.testero.repository.QuestionTemplateRepository;
import app.testero.repository.QuestionSnapshotRepository;
import app.testero.repository.QuestionSnapshotSubjectRepository;
import app.testero.repository.QuestionTemplateSubjectRepository;
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
    private final QuestionTemplateSubjectRepository questionTemplateSubjectRepository;
    private final QuestionTemplateRepository questionTemplateRepository;
    private final OptionTemplateRepository optionTemplateRepository;
    private final AssessmentSnapshotRepository snapshotRepository;
    private final QuestionSnapshotRepository questionSnapshotRepository;
    private final OptionSnapshotRepository optionSnapshotRepository;
    private final QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository;
    private final SubmissionRepository submissionRepository;

    public TrainingService(TopicRepository topicRepository,
                           TopicSubjectRepository topicSubjectRepository,
                           QuestionTemplateSubjectRepository questionTemplateSubjectRepository,
                           QuestionTemplateRepository questionTemplateRepository,
                           OptionTemplateRepository optionTemplateRepository,
                           AssessmentSnapshotRepository snapshotRepository,
                           QuestionSnapshotRepository questionSnapshotRepository,
                           OptionSnapshotRepository optionSnapshotRepository,
                           QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository,
                           SubmissionRepository submissionRepository) {
        this.topicRepository = topicRepository;
        this.topicSubjectRepository = topicSubjectRepository;
        this.questionTemplateSubjectRepository = questionTemplateSubjectRepository;
        this.questionTemplateRepository = questionTemplateRepository;
        this.optionTemplateRepository = optionTemplateRepository;
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
        List<QuestionTemplateSubject> questionSubjects = questionTemplateSubjectRepository
                .findBySubjectIdIn(chapterIds);
        Set<UUID> candidateQuestionIds = questionSubjects.stream()
                .map(QuestionTemplateSubject::getQuestionTemplateId)
                .collect(Collectors.toSet());

        // Fetch questions and filter by difficulty
        List<QuestionTemplate> allQuestions = questionTemplateRepository.findAllById(candidateQuestionIds);
        Difficulty targetDifficulty = parseDifficulty(request.difficulty());
        List<QuestionTemplate> pool;
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
        List<QuestionTemplate> selected = pool.subList(0, count);

        // Create a training snapshot
        AssessmentSnapshot snapshot = new AssessmentSnapshot();
        snapshot.setAssessmentTemplateId(null); // no parent assessment for training
        snapshot.setContentHash(UUID.randomUUID().toString()); // unique hash per session
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
        List<UUID> selectedIds = selected.stream().map(QuestionTemplate::getId).toList();
        Map<UUID, List<OptionTemplate>> optionsByQuestion = optionTemplateRepository
                .findByQuestionTemplateIdInOrderByPosition(selectedIds)
                .stream()
                .collect(Collectors.groupingBy(OptionTemplate::getQuestionTemplateId));
        Map<UUID, List<QuestionTemplateSubject>> subjectsByQuestion = questionTemplateSubjectRepository
                .findByQuestionTemplateIdIn(selectedIds)
                .stream()
                .collect(Collectors.groupingBy(QuestionTemplateSubject::getQuestionTemplateId));

        for (int i = 0; i < selected.size(); i++) {
            QuestionTemplate q = selected.get(i);
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

            for (OptionTemplate o : optionsByQuestion.getOrDefault(q.getId(), List.of())) {
                OptionSnapshot os = new OptionSnapshot();
                os.setQuestionSnapshotId(qs.getId());
                os.setOriginalOptionId(o.getId());
                os.setText(o.getText());
                os.setCorrect(o.isCorrect());
                os.setFallback(o.isFallback());
                os.setPosition(o.getPosition());
                optionSnapshotRepository.save(os);
            }

            for (QuestionTemplateSubject qsub : subjectsByQuestion.getOrDefault(q.getId(), List.of())) {
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
