package app.testero.service;

import app.testero.dto.CompetencyResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetencyServiceTest {

    @Mock SubmissionRepository submissionRepository;
    @Mock UserAnswerRepository userAnswerRepository;
    @Mock QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository;
    @Mock AssessmentSnapshotRepository assessmentSnapshotRepository;
    @Mock TopicRepository topicRepository;
    @Mock TopicSubjectRepository topicSubjectRepository;
    @Mock SubjectRepository subjectRepository;

    @InjectMocks CompetencyService competencyService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID SNAPSHOT_ID = UUID.randomUUID();
    private static final UUID SUBMISSION_ID = UUID.randomUUID();
    private static final UUID TOPIC_ID = UUID.randomUUID();
    private static final UUID SUBJECT_1 = UUID.randomUUID();
    private static final UUID SUBJECT_2 = UUID.randomUUID();
    private static final UUID Q1 = UUID.randomUUID();
    private static final UUID Q2 = UUID.randomUUID();
    private static final UUID Q3 = UUID.randomUUID();

    private Topic topic;
    private Subject subject1;
    private Subject subject2;

    @BeforeEach
    void setUp() {
        topic = new Topic();
        topic.setId(TOPIC_ID);
        topic.setTitle("Fondamenti Python I");
        topic.setEnabled(true);
        topic.setPosition(0);

        subject1 = new Subject();
        subject1.setId(SUBJECT_1);
        subject1.setLabel("Variabili");

        subject2 = new Subject();
        subject2.setId(SUBJECT_2);
        subject2.setLabel("Cicli");
    }

    @Test
    @DisplayName("no submissions returns empty mastery")
    void noSubmissions() {
        when(submissionRepository.findByUserIdAndStatusInOrderBySubmittedAtDesc(eq(USER_ID), any()))
                .thenReturn(List.of());
        when(topicRepository.findByEnabledTrueOrderByPositionAsc())
                .thenReturn(List.of(topic));
        when(topicSubjectRepository.findByTopicIdInOrderByPositionAsc(any()))
                .thenReturn(List.of());

        CompetencyResponse response = competencyService.calculateMastery(USER_ID);

        assertThat(response.topics()).hasSize(1);
        assertThat(response.topics().getFirst().mastery()).isZero();
        assertThat(response.topics().getFirst().subjects()).isEmpty();
    }

    @Test
    @DisplayName("training submissions are excluded")
    void trainingExcluded() {
        UUID trainingSnapshotId = UUID.randomUUID();

        Submission sub = new Submission();
        sub.setId(SUBMISSION_ID);
        sub.setUserId(USER_ID);
        sub.setAssessmentSnapshotId(trainingSnapshotId);
        sub.setStatus(SubmissionStatus.SUBMITTED);

        when(submissionRepository.findByUserIdAndStatusInOrderBySubmittedAtDesc(eq(USER_ID), any()))
                .thenReturn(List.of(sub));

        AssessmentSnapshot trainingSnapshot = new AssessmentSnapshot();
        trainingSnapshot.setId(trainingSnapshotId);
        trainingSnapshot.setType(AssessmentType.TRAINING);

        when(assessmentSnapshotRepository.findAllById(any()))
                .thenReturn(List.of(trainingSnapshot));

        when(topicRepository.findByEnabledTrueOrderByPositionAsc())
                .thenReturn(List.of(topic));
        when(topicSubjectRepository.findByTopicIdInOrderByPositionAsc(any()))
                .thenReturn(List.of());

        CompetencyResponse response = competencyService.calculateMastery(USER_ID);

        assertThat(response.topics()).hasSize(1);
        assertThat(response.topics().getFirst().mastery()).isZero();
    }

    @Test
    @DisplayName("calculates mastery as correct/total percentage per subject")
    void calculatesPercentage() {
        Submission sub = createSubmission(SUBMISSION_ID, SNAPSHOT_ID);

        when(submissionRepository.findByUserIdAndStatusInOrderBySubmittedAtDesc(eq(USER_ID), any()))
                .thenReturn(List.of(sub));

        AssessmentSnapshot snapshot = createExamSnapshot(SNAPSHOT_ID);
        when(assessmentSnapshotRepository.findAllById(any()))
                .thenReturn(List.of(snapshot));

        // 3 answers: 2 correct, 1 wrong — all linked to SUBJECT_1
        UserAnswer a1 = createAnswer(Q1, true);
        UserAnswer a2 = createAnswer(Q2, false);
        UserAnswer a3 = createAnswer(Q3, true);

        when(userAnswerRepository.findBySubmissionIdIn(any()))
                .thenReturn(List.of(a1, a2, a3));

        // All questions linked to SUBJECT_1
        when(questionSnapshotSubjectRepository.findByQuestionSnapshotIdIn(any()))
                .thenReturn(List.of(
                        createQSSubject(Q1, SUBJECT_1),
                        createQSSubject(Q2, SUBJECT_1),
                        createQSSubject(Q3, SUBJECT_1)
                ));

        stubTopicHierarchy();

        CompetencyResponse response = competencyService.calculateMastery(USER_ID);

        assertThat(response.topics()).hasSize(1);
        var topicMastery = response.topics().getFirst();

        // Subject 1: 2/3 = 67%
        var s1 = topicMastery.subjects().stream()
                .filter(s -> s.id().equals(SUBJECT_1.toString()))
                .findFirst().orElseThrow();
        assertThat(s1.mastery()).isEqualTo(67);

        // Subject 2: no answers = 0%
        var s2 = topicMastery.subjects().stream()
                .filter(s -> s.id().equals(SUBJECT_2.toString()))
                .findFirst().orElseThrow();
        assertThat(s2.mastery()).isZero();

        // Topic mastery = average of (67, 0) = 34 (rounded)
        assertThat(topicMastery.mastery()).isEqualTo(34);
    }

    @Test
    @DisplayName("question linked to multiple subjects contributes to all")
    void questionMultipleSubjects() {
        Submission sub = createSubmission(SUBMISSION_ID, SNAPSHOT_ID);

        when(submissionRepository.findByUserIdAndStatusInOrderBySubmittedAtDesc(eq(USER_ID), any()))
                .thenReturn(List.of(sub));

        AssessmentSnapshot snapshot = createExamSnapshot(SNAPSHOT_ID);
        when(assessmentSnapshotRepository.findAllById(any()))
                .thenReturn(List.of(snapshot));

        // 1 correct answer linked to both subjects
        UserAnswer a1 = createAnswer(Q1, true);
        when(userAnswerRepository.findBySubmissionIdIn(any()))
                .thenReturn(List.of(a1));

        when(questionSnapshotSubjectRepository.findByQuestionSnapshotIdIn(any()))
                .thenReturn(List.of(
                        createQSSubject(Q1, SUBJECT_1),
                        createQSSubject(Q1, SUBJECT_2)
                ));

        stubTopicHierarchy();

        CompetencyResponse response = competencyService.calculateMastery(USER_ID);

        var topicMastery = response.topics().getFirst();
        assertThat(topicMastery.subjects()).hasSize(2);
        assertThat(topicMastery.subjects()).allMatch(s -> s.mastery() == 100);
        assertThat(topicMastery.mastery()).isEqualTo(100);
    }

    @Test
    @DisplayName("nested topics aggregate correctly")
    void nestedTopics() {
        UUID childTopicId = UUID.randomUUID();
        UUID childSubjectId = UUID.randomUUID();

        Topic childTopic = new Topic();
        childTopic.setId(childTopicId);
        childTopic.setTitle("Capitolo 1.1");
        childTopic.setEnabled(true);
        childTopic.setParentId(TOPIC_ID);
        childTopic.setPosition(0);

        Subject childSubject = new Subject();
        childSubject.setId(childSubjectId);
        childSubject.setLabel("Sottotema");

        Submission sub = createSubmission(SUBMISSION_ID, SNAPSHOT_ID);
        when(submissionRepository.findByUserIdAndStatusInOrderBySubmittedAtDesc(eq(USER_ID), any()))
                .thenReturn(List.of(sub));

        AssessmentSnapshot snapshot = createExamSnapshot(SNAPSHOT_ID);
        when(assessmentSnapshotRepository.findAllById(any()))
                .thenReturn(List.of(snapshot));

        // 1 correct answer for child subject
        UserAnswer a1 = createAnswer(Q1, true);
        when(userAnswerRepository.findBySubmissionIdIn(any()))
                .thenReturn(List.of(a1));

        when(questionSnapshotSubjectRepository.findByQuestionSnapshotIdIn(any()))
                .thenReturn(List.of(createQSSubject(Q1, childSubjectId)));

        // Topic hierarchy: root topic has no subjects, child topic has childSubject
        when(topicRepository.findByEnabledTrueOrderByPositionAsc())
                .thenReturn(List.of(topic, childTopic));

        TopicSubject ts = new TopicSubject();
        ts.setTopicId(childTopicId);
        ts.setSubjectId(childSubjectId);
        ts.setPosition(0);

        when(topicSubjectRepository.findByTopicIdInOrderByPositionAsc(any()))
                .thenReturn(List.of(ts));
        when(subjectRepository.findByIdIn(any()))
                .thenReturn(List.of(childSubject));

        CompetencyResponse response = competencyService.calculateMastery(USER_ID);

        assertThat(response.topics()).hasSize(1);
        var root = response.topics().getFirst();
        assertThat(root.children()).hasSize(1);

        var child = root.children().getFirst();
        assertThat(child.title()).isEqualTo("Capitolo 1.1");
        assertThat(child.subjects().getFirst().mastery()).isEqualTo(100);
        assertThat(child.mastery()).isEqualTo(100);

        // Root mastery = average of child mastery (100)
        assertThat(root.mastery()).isEqualTo(100);
    }

    // --- helpers ---

    private Submission createSubmission(UUID id, UUID snapshotId) {
        Submission sub = new Submission();
        sub.setId(id);
        sub.setUserId(USER_ID);
        sub.setAssessmentSnapshotId(snapshotId);
        sub.setStatus(SubmissionStatus.SUBMITTED);
        return sub;
    }

    private AssessmentSnapshot createExamSnapshot(UUID id) {
        AssessmentSnapshot snapshot = new AssessmentSnapshot();
        snapshot.setId(id);
        snapshot.setType(AssessmentType.EXAM);
        return snapshot;
    }

    private UserAnswer createAnswer(UUID questionSnapshotId, boolean correct) {
        UserAnswer answer = new UserAnswer();
        answer.setId(UUID.randomUUID());
        answer.setSubmissionId(SUBMISSION_ID);
        answer.setQuestionSnapshotId(questionSnapshotId);
        answer.setType("SINGLE_CHOICE");
        answer.setIsCorrect(correct);
        return answer;
    }

    private QuestionSnapshotSubject createQSSubject(UUID questionSnapshotId, UUID subjectId) {
        QuestionSnapshotSubject qss = new QuestionSnapshotSubject();
        qss.setQuestionSnapshotId(questionSnapshotId);
        qss.setSubjectId(subjectId);
        qss.setWeight(new BigDecimal("1.00"));
        return qss;
    }

    private void stubTopicHierarchy() {
        when(topicRepository.findByEnabledTrueOrderByPositionAsc())
                .thenReturn(List.of(topic));

        TopicSubject ts1 = new TopicSubject();
        ts1.setTopicId(TOPIC_ID);
        ts1.setSubjectId(SUBJECT_1);
        ts1.setPosition(0);
        TopicSubject ts2 = new TopicSubject();
        ts2.setTopicId(TOPIC_ID);
        ts2.setSubjectId(SUBJECT_2);
        ts2.setPosition(1);

        when(topicSubjectRepository.findByTopicIdInOrderByPositionAsc(any()))
                .thenReturn(List.of(ts1, ts2));
        when(subjectRepository.findByIdIn(any()))
                .thenReturn(List.of(subject1, subject2));
    }
}
