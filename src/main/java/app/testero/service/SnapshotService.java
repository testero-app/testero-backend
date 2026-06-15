package app.testero.service;

import app.testero.entity.assessment.Assessment;
import app.testero.entity.assessment.Option;
import app.testero.entity.assessment.Question;
import app.testero.entity.assessment.QuestionSubject;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.snapshot.OptionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshotSubject;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AssessmentRepository;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.OptionRepository;
import app.testero.repository.OptionSnapshotRepository;
import app.testero.repository.QuestionRepository;
import app.testero.repository.QuestionSnapshotRepository;
import app.testero.repository.QuestionSubjectRepository;
import app.testero.repository.QuestionSnapshotSubjectRepository;
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

    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final QuestionSubjectRepository questionSubjectRepository;
    private final AssessmentSnapshotRepository snapshotRepository;
    private final QuestionSnapshotRepository questionSnapshotRepository;
    private final OptionSnapshotRepository optionSnapshotRepository;
    private final QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository;

    public SnapshotService(AssessmentRepository assessmentRepository,
                           QuestionRepository questionRepository,
                           OptionRepository optionRepository,
                           QuestionSubjectRepository questionSubjectRepository,
                           AssessmentSnapshotRepository snapshotRepository,
                           QuestionSnapshotRepository questionSnapshotRepository,
                           OptionSnapshotRepository optionSnapshotRepository,
                           QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository) {
        this.assessmentRepository = assessmentRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.questionSubjectRepository = questionSubjectRepository;
        this.snapshotRepository = snapshotRepository;
        this.questionSnapshotRepository = questionSnapshotRepository;
        this.optionSnapshotRepository = optionSnapshotRepository;
        this.questionSnapshotSubjectRepository = questionSnapshotSubjectRepository;
    }

    @Transactional
    public AssessmentSnapshot publishSnapshot(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));

        List<Question> questions = questionRepository
                .findByAssessmentIdOrderByPosition(assessmentId);
        List<UUID> questionIds = questions.stream().map(Question::getId).toList();
        List<Option> options = questionIds.isEmpty()
                ? List.of()
                : optionRepository.findByQuestionIdInOrderByPosition(questionIds);

        Map<UUID, List<Option>> optionsByQuestion = options.stream()
                .collect(Collectors.groupingBy(Option::getQuestionId));

        List<QuestionSubject> questionSubjects = questionIds.isEmpty()
                ? List.of()
                : questionSubjectRepository.findByQuestionIdIn(questionIds);
        Map<UUID, List<QuestionSubject>> subjectsByQuestion = questionSubjects.stream()
                .collect(Collectors.groupingBy(QuestionSubject::getQuestionId));

        String hash = computeContentHash(assessment, questions, optionsByQuestion,
                subjectsByQuestion);

        // If an identical snapshot already exists, return it
        Optional<AssessmentSnapshot> existing = snapshotRepository
                .findByAssessmentIdAndContentHash(assessmentId, hash);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Determine next version number
        int nextVersion = snapshotRepository
                .findTopByAssessmentIdOrderByVersionDesc(assessmentId)
                .map(s -> s.getVersion() + 1)
                .orElse(1);

        // Create snapshot
        AssessmentSnapshot snapshot = new AssessmentSnapshot();
        snapshot.setAssessmentId(assessmentId);
        snapshot.setContentHash(hash);
        snapshot.setVersion(nextVersion);
        snapshot.setTitle(assessment.getTitle());
        snapshot.setTimerMinutes(assessment.getTimerMinutes());
        snapshot.setQuestionsPerAssessment(assessment.getQuestionsPerAssessment());
        snapshot.setPtsCorrect(assessment.getPtsCorrect());
        snapshot.setPtsWrong(assessment.getPtsWrong());
        snapshot.setDifficulty(assessment.getDifficulty());
        snapshot.setPassingScore(assessment.getPassingScore());
        snapshot.setPublishedAt(LocalDateTime.now());
        snapshot = snapshotRepository.save(snapshot);

        // Copy questions and options
        for (Question q : questions) {
            QuestionSnapshot qs = new QuestionSnapshot();
            qs.setAssessmentSnapshotId(snapshot.getId());
            qs.setOriginalQuestionId(q.getId());
            qs.setType(q.getType());
            qs.setText(q.getText());
            qs.setCode(q.getCode());
            qs.setExplanation(q.getExplanation());
            qs.setPosition(q.getPosition());
            qs = questionSnapshotRepository.save(qs);

            List<Option> qOptions = optionsByQuestion.getOrDefault(q.getId(), List.of());
            for (Option o : qOptions) {
                OptionSnapshot os = new OptionSnapshot();
                os.setQuestionSnapshotId(qs.getId());
                os.setOriginalOptionId(o.getId());
                os.setText(o.getText());
                os.setCorrect(o.isCorrect());
                os.setFallback(o.isFallback());
                os.setPosition(o.getPosition());
                optionSnapshotRepository.save(os);
            }

            List<QuestionSubject> qSubjects = subjectsByQuestion
                    .getOrDefault(q.getId(), List.of());
            for (QuestionSubject qsub : qSubjects) {
                QuestionSnapshotSubject qss = new QuestionSnapshotSubject();
                qss.setQuestionSnapshotId(qs.getId());
                qss.setSubjectId(qsub.getSubjectId());
                qss.setWeight(qsub.getWeight());
                questionSnapshotSubjectRepository.save(qss);
            }
        }

        return snapshot;
    }

    static String computeContentHash(Assessment assessment,
                                     List<Question> questions,
                                     Map<UUID, List<Option>> optionsByQuestion,
                                     Map<UUID, List<QuestionSubject>> subjectsByQuestion) {
        StringBuilder sb = new StringBuilder();
        sb.append(assessment.getTitle());
        sb.append('|').append(assessment.getTimerMinutes());
        sb.append('|').append(assessment.getPtsCorrect().toPlainString());
        sb.append('|').append(assessment.getPtsWrong().toPlainString());
        sb.append('|').append(assessment.getQuestionsPerAssessment());
        sb.append('|').append(assessment.getDifficulty() != null ? assessment.getDifficulty().name() : "");
        sb.append('|').append(assessment.getPassingScore() != null ? assessment.getPassingScore().toPlainString() : "");

        for (Question q : questions) {
            sb.append("||Q|").append(q.getType());
            sb.append('|').append(q.getText());
            sb.append('|').append(q.getCode() != null ? q.getCode() : "");
            sb.append('|').append(q.getExplanation() != null ? q.getExplanation() : "");

            List<Option> opts = optionsByQuestion.getOrDefault(q.getId(), List.of());
            for (Option o : opts) {
                sb.append("|O|").append(o.getText());
                sb.append('|').append(o.isCorrect());
                sb.append('|').append(o.isFallback());
            }

            List<QuestionSubject> subs = subjectsByQuestion
                    .getOrDefault(q.getId(), List.of()).stream()
                    .sorted(Comparator.comparing(QuestionSubject::getSubjectId))
                    .toList();
            for (QuestionSubject s : subs) {
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
