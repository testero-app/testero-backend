package app.testero.service.training;

import app.testero.dto.training.TrainingStartRequest;
import app.testero.dto.training.TrainingStartResponse;
import app.testero.entity.assessment.AssessmentType;
import app.testero.entity.assessment.Difficulty;
import app.testero.entity.assessment.TopicSubject;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.snapshot.QuestionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshotSubject;
import app.testero.entity.submission.Submission;
import app.testero.entity.submission.SubmissionQuestion;
import app.testero.entity.submission.SubmissionStatus;
import app.testero.entity.user.StudentProfile;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.assessment.AssessmentSnapshotRepository;
import app.testero.repository.assessment.QuestionSnapshotRepository;
import app.testero.repository.assessment.QuestionSnapshotSubjectRepository;
import app.testero.repository.assessment.TopicRepository;
import app.testero.repository.assessment.TopicSubjectRepository;
import app.testero.repository.submission.SubmissionQuestionRepository;
import app.testero.repository.submission.SubmissionRepository;
import app.testero.repository.user.StudentProfileRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Free training: the student picks what to practise on and gets a paper drawn on the spot.
 *
 * <p>The questions come from the pools their class may practise on — published assessments
 * assigned to it — and are <b>referenced, not copied</b>. A session that spans several pools
 * cannot belong to a single assessment snapshot, so it has none: its paper lives in
 * {@code submission_question}. Sessions on one published assessment (exams, certification
 * simulations) keep working the other way round, through the snapshot and the seed.
 *
 * <p>Until the teacher flow can mark an assessment as practisable, the material is every
 * {@code TRAINING} or {@code CERT_SIMULATION} assigned to the class. Exams are never drawn
 * from: a student must not meet the questions of a test they have yet to sit.
 */
@Slf4j
@Service
public class TrainingService {

    private static final double MINUTES_PER_QUESTION = 1.5;

    /** What a student may practise on, until the teacher can decide it per assignment. */
    private static final List<AssessmentType> PRACTISABLE_TYPES =
            List.of(AssessmentType.TRAINING, AssessmentType.CERT_SIMULATION);

    private final TopicRepository topicRepository;
    private final TopicSubjectRepository topicSubjectRepository;
    private final AssessmentSnapshotRepository snapshotRepository;
    private final QuestionSnapshotRepository questionSnapshotRepository;
    private final QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionQuestionRepository submissionQuestionRepository;
    private final StudentProfileRepository studentProfileRepository;

    public TrainingService(TopicRepository topicRepository,
                           TopicSubjectRepository topicSubjectRepository,
                           AssessmentSnapshotRepository snapshotRepository,
                           QuestionSnapshotRepository questionSnapshotRepository,
                           QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository,
                           SubmissionRepository submissionRepository,
                           SubmissionQuestionRepository submissionQuestionRepository,
                           StudentProfileRepository studentProfileRepository) {
        this.topicRepository = topicRepository;
        this.topicSubjectRepository = topicSubjectRepository;
        this.snapshotRepository = snapshotRepository;
        this.questionSnapshotRepository = questionSnapshotRepository;
        this.questionSnapshotSubjectRepository = questionSnapshotSubjectRepository;
        this.submissionRepository = submissionRepository;
        this.submissionQuestionRepository = submissionQuestionRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    @Transactional
    public TrainingStartResponse startTraining(TrainingStartRequest request, UUID userId) {
        List<QuestionSnapshot> pool = drawPool(request, userId);
        if (pool.isEmpty()) {
            throw new IllegalArgumentException(
                    "No questions available for the selected chapters and difficulty");
        }

        Collections.shuffle(pool);
        int count = Math.min(request.questionCount(), pool.size());
        List<QuestionSnapshot> drawn = pool.subList(0, count);

        Integer timerMinutes = request.timerEnabled()
                ? (int) Math.ceil(count * MINUTES_PER_QUESTION)
                : null;

        Submission submission = new Submission();
        submission.setUserId(userId);
        // No snapshot: the paper spans the pools the class may practise on.
        submission.setAssessmentSnapshotId(null);
        submission.setStatus(SubmissionStatus.IN_PROGRESS);
        submission.setStartedAt(LocalDateTime.now());
        submission.setSeed(ThreadLocalRandom.current().nextLong());
        submission.setTimerMinutes(timerMinutes);
        submission = submissionRepository.save(submission);

        List<SubmissionQuestion> paper = new ArrayList<>(drawn.size());
        for (int i = 0; i < drawn.size(); i++) {
            paper.add(new SubmissionQuestion(submission.getId(), drawn.get(i).getId(), i));
        }
        submissionQuestionRepository.saveAll(paper);

        log.info("Free training started: submissionId={}, userId={}, questions={}",
                submission.getId(), userId, count);

        return new TrainingStartResponse(submission.getId().toString(), timerMinutes, count);
    }

    /**
     * Every question the student may be asked, given their class and the filters they chose.
     * Both filters are optional: no chapters means the whole topic, no topic means everything
     * they may practise on.
     */
    private List<QuestionSnapshot> drawPool(TrainingStartRequest request, UUID userId) {
        UUID classId = studentProfileRepository.findByUserId(userId)
                .map(StudentProfile::getClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        List<AssessmentSnapshot> sources =
                snapshotRepository.findPractisableSnapshots(classId, PRACTISABLE_TYPES);
        if (sources.isEmpty()) {
            return List.of();
        }

        List<QuestionSnapshot> questions = questionSnapshotRepository
                .findByAssessmentSnapshotIdIn(sources.stream().map(AssessmentSnapshot::getId).toList());

        Difficulty difficulty = parseDifficulty(request.difficulty());
        if (difficulty != null) {
            questions = questions.stream()
                    .filter(q -> q.getDifficulty() == difficulty)
                    .collect(Collectors.toList());
        }

        Set<UUID> chapterIds = resolveChapters(request);
        if (!chapterIds.isEmpty()) {
            questions = filterByChapter(questions, chapterIds);
        }

        return new ArrayList<>(deduplicate(questions, sources));
    }

    /**
     * The chapters to draw from: the ones asked for, or all of the chosen topic, or none at
     * all — which means "anything my class can practise on".
     */
    private Set<UUID> resolveChapters(TrainingStartRequest request) {
        if (request.chapterIds() != null && !request.chapterIds().isEmpty()) {
            Set<UUID> chapters = request.chapterIds().stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
            if (request.topicId() != null) {
                Set<UUID> ofTopic = chaptersOf(UUID.fromString(request.topicId()));
                chapters.stream()
                        .filter(id -> !ofTopic.contains(id))
                        .findFirst()
                        .ifPresent(id -> {
                            throw new IllegalArgumentException(
                                    "Chapter " + id + " does not belong to the topic");
                        });
            }
            return chapters;
        }
        return request.topicId() == null
                ? Set.of()
                : chaptersOf(UUID.fromString(request.topicId()));
    }

    private Set<UUID> chaptersOf(UUID topicId) {
        topicRepository.findById(topicId)
                .filter(t -> t.isEnabled())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        return topicSubjectRepository.findByTopicIdOrderByPositionAsc(topicId).stream()
                .map(TopicSubject::getSubjectId)
                .collect(Collectors.toSet());
    }

    private List<QuestionSnapshot> filterByChapter(List<QuestionSnapshot> questions,
                                                   Set<UUID> chapterIds) {
        if (questions.isEmpty()) {
            return questions;
        }
        Set<UUID> matching = questionSnapshotSubjectRepository
                .findByQuestionSnapshotIdIn(questions.stream().map(QuestionSnapshot::getId).toList())
                .stream()
                .filter(link -> chapterIds.contains(link.getSubjectId()))
                .map(QuestionSnapshotSubject::getQuestionSnapshotId)
                .collect(Collectors.toSet());
        return questions.stream()
                .filter(q -> matching.contains(q.getId()))
                .collect(Collectors.toList());
    }

    /**
     * The same bank question lives in one snapshot per publication, so a student would meet
     * it once per edition. Collapse them on the original question, keeping the copy from the
     * most recently published pool — the one carrying the teacher's latest corrections.
     */
    private Collection<QuestionSnapshot> deduplicate(List<QuestionSnapshot> questions,
                                                     List<AssessmentSnapshot> sources) {
        Map<UUID, LocalDateTime> publishedAt = sources.stream()
                .collect(Collectors.toMap(AssessmentSnapshot::getId,
                        s -> s.getPublishedAt() == null ? LocalDateTime.MIN : s.getPublishedAt()));

        Map<UUID, QuestionSnapshot> newest = new HashMap<>();
        List<QuestionSnapshot> unlinked = new ArrayList<>();
        for (QuestionSnapshot q : questions) {
            if (q.getOriginalQuestionId() == null) {
                unlinked.add(q);
                continue;
            }
            newest.merge(q.getOriginalQuestionId(), q, (a, b) ->
                    publishedAt.get(a.getAssessmentSnapshotId())
                            .isAfter(publishedAt.get(b.getAssessmentSnapshotId())) ? a : b);
        }
        List<QuestionSnapshot> result = new ArrayList<>(newest.values());
        result.addAll(unlinked);
        return result;
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
