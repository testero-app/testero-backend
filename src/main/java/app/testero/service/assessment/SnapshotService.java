package app.testero.service.assessment;

import app.testero.entity.assessment.AssessmentSubject;
import app.testero.entity.assessment.AssessmentTemplate;
import app.testero.entity.assessment.OptionTemplate;
import app.testero.entity.assessment.QuestionTemplate;
import app.testero.entity.assessment.QuestionTemplateSubject;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.assessment.AssessmentTemplateTopic;
import app.testero.entity.assessment.Subject;
import app.testero.entity.assessment.Topic;
import app.testero.entity.snapshot.AssessmentSnapshotSubject;
import app.testero.entity.snapshot.AssessmentSnapshotTopic;
import app.testero.entity.snapshot.OptionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshotSubject;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.assessment.AssessmentSubjectRepository;
import app.testero.repository.assessment.AssessmentTemplateRepository;
import app.testero.repository.assessment.AssessmentTemplateTopicRepository;
import app.testero.repository.assessment.AssessmentSnapshotRepository;
import app.testero.repository.assessment.AssessmentSnapshotSubjectRepository;
import app.testero.repository.assessment.AssessmentSnapshotTopicRepository;
import app.testero.repository.assessment.TopicRepository;
import app.testero.repository.assessment.OptionTemplateRepository;
import app.testero.repository.assessment.OptionSnapshotRepository;
import app.testero.repository.assessment.QuestionTemplateRepository;
import app.testero.repository.assessment.QuestionSnapshotRepository;
import app.testero.repository.assessment.QuestionTemplateSubjectRepository;
import app.testero.repository.assessment.QuestionSnapshotSubjectRepository;
import app.testero.repository.assessment.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SnapshotService {

    private final AssessmentTemplateRepository assessmentTemplateRepository;
    private final AssessmentSubjectRepository assessmentSubjectRepository;
    private final QuestionTemplateRepository questionTemplateRepository;
    private final OptionTemplateRepository optionTemplateRepository;
    private final QuestionTemplateSubjectRepository questionTemplateSubjectRepository;
    private final AssessmentSnapshotRepository snapshotRepository;
    private final AssessmentSnapshotSubjectRepository assessmentSnapshotSubjectRepository;
    private final QuestionSnapshotRepository questionSnapshotRepository;
    private final OptionSnapshotRepository optionSnapshotRepository;
    private final QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final AssessmentTemplateTopicRepository assessmentTemplateTopicRepository;
    private final AssessmentSnapshotTopicRepository assessmentSnapshotTopicRepository;
    private final TopicRepository topicRepository;

    public SnapshotService(
            AssessmentTemplateRepository assessmentTemplateRepository,
            AssessmentSubjectRepository assessmentSubjectRepository,
            QuestionTemplateRepository questionTemplateRepository,
            OptionTemplateRepository optionTemplateRepository,
            QuestionTemplateSubjectRepository questionTemplateSubjectRepository,
            AssessmentSnapshotRepository snapshotRepository,
            AssessmentSnapshotSubjectRepository assessmentSnapshotSubjectRepository,
            QuestionSnapshotRepository questionSnapshotRepository,
            OptionSnapshotRepository optionSnapshotRepository,
            QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository,
            SubjectRepository subjectRepository,
            AssessmentTemplateTopicRepository assessmentTemplateTopicRepository,
            AssessmentSnapshotTopicRepository assessmentSnapshotTopicRepository,
            TopicRepository topicRepository) {
        this.assessmentTemplateRepository = assessmentTemplateRepository;
        this.assessmentSubjectRepository = assessmentSubjectRepository;
        this.questionTemplateRepository = questionTemplateRepository;
        this.optionTemplateRepository = optionTemplateRepository;
        this.questionTemplateSubjectRepository = questionTemplateSubjectRepository;
        this.snapshotRepository = snapshotRepository;
        this.assessmentSnapshotSubjectRepository = assessmentSnapshotSubjectRepository;
        this.questionSnapshotRepository = questionSnapshotRepository;
        this.optionSnapshotRepository = optionSnapshotRepository;
        this.questionSnapshotSubjectRepository = questionSnapshotSubjectRepository;
        this.subjectRepository = subjectRepository;
        this.assessmentTemplateTopicRepository = assessmentTemplateTopicRepository;
        this.assessmentSnapshotTopicRepository = assessmentSnapshotTopicRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional
    public AssessmentSnapshot publishSnapshot(UUID assessmentId) {
        AssessmentTemplate assessment = assessmentTemplateRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));

        List<QuestionTemplate> questions = questionTemplateRepository
                .findByAssessmentIdOrderByPosition(assessmentId);
        List<UUID> questionIds = questions.stream().map(QuestionTemplate::getId).toList();
        List<OptionTemplate> options = questionIds.isEmpty()
                ? List.of()
                : optionTemplateRepository.findByQuestionTemplateIdInOrderByPosition(questionIds);

        Map<UUID, List<OptionTemplate>> optionsByQuestion = options.stream()
                .collect(Collectors.groupingBy(OptionTemplate::getQuestionTemplateId));

        List<QuestionTemplateSubject> questionSubjects = questionIds.isEmpty()
                ? List.of()
                : questionTemplateSubjectRepository.findByQuestionTemplateIdIn(questionIds);
        Map<UUID, List<QuestionTemplateSubject>> subjectsByQuestion = questionSubjects.stream()
                .collect(Collectors.groupingBy(QuestionTemplateSubject::getQuestionTemplateId));

        List<AssessmentSubject> assessmentSubjects = assessmentSubjectRepository
                .findByAssessmentId(assessmentId);

        // Collect all subject IDs and fetch labels
        List<UUID> allSubjectIds = new java.util.ArrayList<>();
        assessmentSubjects.forEach(as -> allSubjectIds.add(as.getSubjectId()));
        questionSubjects.forEach(qs -> allSubjectIds.add(qs.getSubjectId()));
        Map<UUID, String> subjectLabels = allSubjectIds.isEmpty()
                ? Map.of()
                : subjectRepository.findByIdIn(allSubjectIds).stream()
                        .collect(Collectors.toMap(Subject::getId, Subject::getLabel));

        String hash = computeContentHash(assessment, questions, optionsByQuestion,
                subjectsByQuestion, assessmentSubjects);

        // If an identical snapshot already exists, return it
        Optional<AssessmentSnapshot> existing = snapshotRepository
                .findByAssessmentTemplateIdAndContentHash(assessmentId, hash);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Create snapshot
        AssessmentSnapshot snapshot = new AssessmentSnapshot();
        snapshot.setAssessmentTemplateId(assessmentId);
        snapshot.setContentHash(hash);
        snapshot.setTitle(assessment.getTitle());
        snapshot.setTimerMinutes(assessment.getTimerMinutes());
        snapshot.setQuestionsPerAssessment(assessment.getQuestionsPerAssessment());
        snapshot.setPtsCorrect(assessment.getPtsCorrect());
        snapshot.setPtsWrong(assessment.getPtsWrong());
        snapshot.setDifficulty(assessment.getDifficulty());
        snapshot.setType(assessment.getType());
        snapshot.setPassingScore(assessment.getPassingScore());
        snapshot.setMaxAttempts(assessment.getMaxAttempts());
        snapshot.setPublishedAt(LocalDateTime.now());
        snapshot = snapshotRepository.save(snapshot);

        // Copy assessment-level subjects with frozen label
        for (AssessmentSubject as : assessmentSubjects) {
            AssessmentSnapshotSubject ass = new AssessmentSnapshotSubject();
            ass.setAssessmentSnapshotId(snapshot.getId());
            ass.setSubjectId(as.getSubjectId());
            ass.setLabel(subjectLabels.get(as.getSubjectId()));
            assessmentSnapshotSubjectRepository.save(ass);
        }

        // Copy assessment-level topics with frozen title
        List<AssessmentTemplateTopic> assessmentTopics =
                assessmentTemplateTopicRepository
                        .findByAssessmentTemplateId(assessmentId);
        if (!assessmentTopics.isEmpty()) {
            List<UUID> topicIds = assessmentTopics.stream()
                    .map(AssessmentTemplateTopic::getTopicId).toList();
            Map<UUID, String> topicTitles = topicRepository
                    .findAllById(topicIds).stream()
                    .collect(Collectors.toMap(
                            Topic::getId, Topic::getTitle));
            for (AssessmentTemplateTopic at : assessmentTopics) {
                AssessmentSnapshotTopic ast =
                        new AssessmentSnapshotTopic();
                ast.setAssessmentSnapshotId(snapshot.getId());
                ast.setTopicId(at.getTopicId());
                ast.setTitle(topicTitles.get(at.getTopicId()));
                assessmentSnapshotTopicRepository.save(ast);
            }
        }

        // Copy questions and options
        for (QuestionTemplate q : questions) {
            QuestionSnapshot qs = new QuestionSnapshot();
            qs.setAssessmentSnapshotId(snapshot.getId());
            qs.setOriginalQuestionId(q.getId());
            qs.setType(q.getType());
            qs.setText(q.getText());
            qs.setCode(q.getCode());
            qs.setExplanation(q.getExplanation());
            qs.setPosition(q.getPosition());
            qs.setPoints(q.getPoints());
            qs.setDifficulty(q.getDifficulty());
            qs = questionSnapshotRepository.save(qs);

            List<OptionTemplate> qOptions = optionsByQuestion.getOrDefault(q.getId(), List.of());
            for (OptionTemplate o : qOptions) {
                OptionSnapshot os = new OptionSnapshot();
                os.setQuestionSnapshotId(qs.getId());
                os.setOriginalOptionId(o.getId());
                os.setText(o.getText());
                os.setCorrect(o.isCorrect());
                os.setFallback(o.isFallback());
                os.setPosition(o.getPosition());
                optionSnapshotRepository.save(os);
            }

            List<QuestionTemplateSubject> qSubjects = subjectsByQuestion
                    .getOrDefault(q.getId(), List.of());
            for (QuestionTemplateSubject qsub : qSubjects) {
                QuestionSnapshotSubject qss = new QuestionSnapshotSubject();
                qss.setQuestionSnapshotId(qs.getId());
                qss.setSubjectId(qsub.getSubjectId());
                qss.setWeight(qsub.getWeight());
                qss.setLabel(subjectLabels.get(qsub.getSubjectId()));
                questionSnapshotSubjectRepository.save(qss);
            }
        }

        return snapshot;
    }

    static String computeContentHash(AssessmentTemplate assessment,
                                     List<QuestionTemplate> questions,
                                     Map<UUID, List<OptionTemplate>> optionsByQuestion,
                                     Map<UUID, List<QuestionTemplateSubject>> subjectsByQuestion,
                                     List<AssessmentSubject> assessmentSubjects) {
        StringBuilder sb = new StringBuilder();
        sb.append(assessment.getTitle());
        sb.append('|').append(assessment.getTimerMinutes());
        sb.append('|').append(assessment.getPtsCorrect().toPlainString());
        sb.append('|').append(assessment.getPtsWrong().toPlainString());
        sb.append('|').append(assessment.getQuestionsPerAssessment());
        sb.append('|').append(assessment.getDifficulty() != null ? assessment.getDifficulty().name() : "");
        sb.append('|').append(assessment.getPassingScore() != null ? assessment.getPassingScore().toPlainString() : "");

        List<AssessmentSubject> sortedAssSubjects = assessmentSubjects.stream()
                .sorted(Comparator.comparing(AssessmentSubject::getSubjectId))
                .toList();
        for (AssessmentSubject as : sortedAssSubjects) {
            sb.append("|AS|").append(as.getSubjectId());
        }

        for (QuestionTemplate q : questions) {
            sb.append("||Q|").append(q.getType());
            sb.append('|').append(q.getText());
            sb.append('|').append(q.getCode() != null ? q.getCode() : "");
            sb.append('|').append(q.getExplanation() != null ? q.getExplanation() : "");
            sb.append('|').append(q.getPoints() != null ? q.getPoints().toPlainString() : "");

            List<OptionTemplate> opts = optionsByQuestion.getOrDefault(q.getId(), List.of());
            for (OptionTemplate o : opts) {
                sb.append("|O|").append(o.getText());
                sb.append('|').append(o.isCorrect());
                sb.append('|').append(o.isFallback());
            }

            List<QuestionTemplateSubject> subs = subjectsByQuestion
                    .getOrDefault(q.getId(), List.of()).stream()
                    .sorted(Comparator.comparing(QuestionTemplateSubject::getSubjectId))
                    .toList();
            for (QuestionTemplateSubject s : subs) {
                sb.append("|S|").append(s.getSubjectId());
                sb.append('|').append(s.getWeight().toPlainString());
            }
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
