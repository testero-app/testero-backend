package app.testero.service;

import app.testero.dto.CompetencyResponse;
import app.testero.dto.CompetencyResponse.SubjectMastery;
import app.testero.dto.CompetencyResponse.TopicMastery;
import app.testero.entity.assessment.AssessmentType;
import app.testero.entity.assessment.Subject;
import app.testero.entity.assessment.Topic;
import app.testero.entity.assessment.TopicSubject;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.snapshot.QuestionSnapshotSubject;
import app.testero.entity.submission.Submission;
import app.testero.entity.submission.SubmissionStatus;
import app.testero.entity.submission.UserAnswer;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.QuestionSnapshotSubjectRepository;
import app.testero.repository.SubjectRepository;
import app.testero.repository.SubmissionRepository;
import app.testero.repository.TopicRepository;
import app.testero.repository.TopicSubjectRepository;
import app.testero.repository.UserAnswerRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompetencyService {

    private final SubmissionRepository submissionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository;
    private final AssessmentSnapshotRepository assessmentSnapshotRepository;
    private final TopicRepository topicRepository;
    private final TopicSubjectRepository topicSubjectRepository;
    private final SubjectRepository subjectRepository;

    public CompetencyService(
            SubmissionRepository submissionRepository,
            UserAnswerRepository userAnswerRepository,
            QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository,
            AssessmentSnapshotRepository assessmentSnapshotRepository,
            TopicRepository topicRepository,
            TopicSubjectRepository topicSubjectRepository,
            SubjectRepository subjectRepository) {
        this.submissionRepository = submissionRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.questionSnapshotSubjectRepository = questionSnapshotSubjectRepository;
        this.assessmentSnapshotRepository = assessmentSnapshotRepository;
        this.topicRepository = topicRepository;
        this.topicSubjectRepository = topicSubjectRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional(readOnly = true)
    public CompetencyResponse calculateMastery(UUID userId) {
        // 1. Get completed submissions (SUBMITTED or AUTO_CLOSED)
        List<Submission> submissions = submissionRepository
                .findByUserIdAndStatusInOrderBySubmittedAtDesc(
                        userId,
                        List.of(SubmissionStatus.SUBMITTED, SubmissionStatus.AUTO_CLOSED));

        if (submissions.isEmpty()) {
            return buildEmptyResponse();
        }

        // 2. Filter to EXAM and CERT_SIMULATION only (exclude TRAINING)
        Set<UUID> snapshotIds = submissions.stream()
                .map(Submission::getAssessmentSnapshotId)
                .collect(Collectors.toSet());

        List<AssessmentSnapshot> snapshots = assessmentSnapshotRepository
                .findAllById(snapshotIds);

        Set<UUID> validSnapshotIds = snapshots.stream()
                .filter(s -> s.getType() == AssessmentType.EXAM
                        || s.getType() == AssessmentType.CERT_SIMULATION)
                .map(AssessmentSnapshot::getId)
                .collect(Collectors.toSet());

        List<Submission> validSubmissions = submissions.stream()
                .filter(s -> validSnapshotIds.contains(s.getAssessmentSnapshotId()))
                .toList();

        if (validSubmissions.isEmpty()) {
            return buildEmptyResponse();
        }

        // 3. Get all user answers for those submissions
        List<UUID> submissionIds = validSubmissions.stream()
                .map(Submission::getId)
                .toList();

        List<UserAnswer> allAnswers = userAnswerRepository.findBySubmissionIdIn(submissionIds);

        if (allAnswers.isEmpty()) {
            return buildEmptyResponse();
        }

        // 4. Get question-subject links for all answered questions
        List<UUID> questionSnapshotIds = allAnswers.stream()
                .map(UserAnswer::getQuestionSnapshotId)
                .distinct()
                .toList();

        List<QuestionSnapshotSubject> qsSubjects = questionSnapshotSubjectRepository
                .findByQuestionSnapshotIdIn(questionSnapshotIds);

        // Build lookup: questionSnapshotId -> list of subject links
        Map<UUID, List<QuestionSnapshotSubject>> subjectsByQuestion = qsSubjects.stream()
                .collect(Collectors.groupingBy(QuestionSnapshotSubject::getQuestionSnapshotId));

        // 5. Calculate mastery per subject: correct / total * 100
        //    Each answer contributes to all subjects linked to its question
        Map<UUID, int[]> subjectStats = new HashMap<>(); // subjectId -> [correct, total]

        for (UserAnswer answer : allAnswers) {
            List<QuestionSnapshotSubject> links = subjectsByQuestion
                    .get(answer.getQuestionSnapshotId());
            if (links == null || answer.getIsCorrect() == null) {
                continue;
            }
            for (QuestionSnapshotSubject link : links) {
                int[] stats = subjectStats.computeIfAbsent(
                        link.getSubjectId(), k -> new int[]{0, 0});
                stats[1]++; // total
                if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                    stats[0]++; // correct
                }
            }
        }

        // 6. Build topic hierarchy with mastery
        return buildResponse(subjectStats);
    }

    private CompetencyResponse buildEmptyResponse() {
        return buildResponse(Map.of());
    }

    private CompetencyResponse buildResponse(Map<UUID, int[]> subjectStats) {
        // Load all enabled topics
        List<Topic> allTopics = topicRepository.findByEnabledTrueOrderByPositionAsc();

        // Load all topic-subject mappings
        List<UUID> topicIds = allTopics.stream().map(Topic::getId).toList();
        List<TopicSubject> allTopicSubjects = topicSubjectRepository
                .findByTopicIdInOrderByPositionAsc(topicIds);

        // Load all subjects referenced
        Set<UUID> allSubjectIds = allTopicSubjects.stream()
                .map(TopicSubject::getSubjectId)
                .collect(Collectors.toSet());
        Map<UUID, Subject> subjectMap = subjectRepository.findByIdIn(new ArrayList<>(allSubjectIds))
                .stream()
                .collect(Collectors.toMap(Subject::getId, s -> s));

        // Group topic-subjects by topic
        Map<UUID, List<TopicSubject>> topicSubjectMap = allTopicSubjects.stream()
                .collect(Collectors.groupingBy(TopicSubject::getTopicId));

        // Group topics by parent
        Map<UUID, List<Topic>> childrenByParent = allTopics.stream()
                .filter(t -> t.getParentId() != null)
                .collect(Collectors.groupingBy(Topic::getParentId));

        // Build hierarchy: root topics (parentId == null)
        List<Topic> rootTopics = allTopics.stream()
                .filter(t -> t.getParentId() == null)
                .toList();

        List<TopicMastery> topicMasteries = rootTopics.stream()
                .map(t -> buildTopicMastery(t, childrenByParent, topicSubjectMap,
                        subjectMap, subjectStats))
                .toList();

        return new CompetencyResponse(topicMasteries);
    }

    private TopicMastery buildTopicMastery(
            Topic topic,
            Map<UUID, List<Topic>> childrenByParent,
            Map<UUID, List<TopicSubject>> topicSubjectMap,
            Map<UUID, Subject> subjectMap,
            Map<UUID, int[]> subjectStats) {

        // Build subject mastery list for this topic
        List<TopicSubject> topicSubjects = topicSubjectMap
                .getOrDefault(topic.getId(), List.of());

        List<SubjectMastery> subjectMasteries = topicSubjects.stream()
                .map(ts -> {
                    Subject subject = subjectMap.get(ts.getSubjectId());
                    if (subject == null) {
                        return null;
                    }
                    int mastery = calculateSubjectMastery(ts.getSubjectId(), subjectStats);
                    return new SubjectMastery(
                            subject.getId().toString(),
                            subject.getLabel(),
                            mastery);
                })
                .filter(sm -> sm != null)
                .toList();

        // Build children recursively
        List<Topic> children = childrenByParent
                .getOrDefault(topic.getId(), List.of());

        List<TopicMastery> childMasteries = children.stream()
                .map(c -> buildTopicMastery(c, childrenByParent, topicSubjectMap,
                        subjectMap, subjectStats))
                .toList();

        // Topic mastery = average of its subjects' mastery values
        // If topic has children, also include their mastery in the average
        int topicMastery = calculateTopicMastery(subjectMasteries, childMasteries);

        return new TopicMastery(
                topic.getId().toString(),
                topic.getTitle(),
                topicMastery,
                childMasteries.isEmpty() ? List.of() : childMasteries,
                subjectMasteries);
    }

    private int calculateSubjectMastery(UUID subjectId, Map<UUID, int[]> subjectStats) {
        int[] stats = subjectStats.get(subjectId);
        if (stats == null || stats[1] == 0) {
            return 0;
        }
        return Math.round((float) stats[0] / stats[1] * 100);
    }

    private int calculateTopicMastery(
            List<SubjectMastery> subjectMasteries,
            List<TopicMastery> childMasteries) {

        // Collect all mastery values: direct subjects + child topic averages
        List<Integer> values = new ArrayList<>();

        for (SubjectMastery sm : subjectMasteries) {
            values.add(sm.mastery());
        }
        for (TopicMastery cm : childMasteries) {
            values.add(cm.mastery());
        }

        if (values.isEmpty()) {
            return 0;
        }

        int sum = values.stream().mapToInt(Integer::intValue).sum();
        return Math.round((float) sum / values.size());
    }
}
