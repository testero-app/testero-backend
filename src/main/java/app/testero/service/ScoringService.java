package app.testero.service;

import app.testero.dto.SubmissionFeedbackResponse.AnswerResult;
import app.testero.dto.SubmissionFeedbackResponse.SubjectScore;
import app.testero.entity.assessment.Subject;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.snapshot.OptionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshotSubject;
import app.testero.entity.submission.Submission;
import app.testero.entity.submission.UserAnswer;
import app.testero.entity.submission.UserAnswerSelectedOption;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.OptionSnapshotRepository;
import app.testero.repository.QuestionSnapshotRepository;
import app.testero.repository.QuestionSnapshotSubjectRepository;
import app.testero.repository.SubjectRepository;
import app.testero.repository.SubmissionRepository;
import app.testero.repository.UserAnswerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ScoringService {

    private final SubmissionRepository submissionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final OptionSnapshotRepository optionSnapshotRepository;
    private final AssessmentSnapshotRepository assessmentSnapshotRepository;
    private final QuestionSnapshotRepository questionSnapshotRepository;
    private final QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository;
    private final SubjectRepository subjectRepository;

    public ScoringService(SubmissionRepository submissionRepository,
                          UserAnswerRepository userAnswerRepository,
                          OptionSnapshotRepository optionSnapshotRepository,
                          AssessmentSnapshotRepository assessmentSnapshotRepository,
                          QuestionSnapshotRepository questionSnapshotRepository,
                          QuestionSnapshotSubjectRepository questionSnapshotSubjectRepository,
                          SubjectRepository subjectRepository) {
        this.submissionRepository = submissionRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.optionSnapshotRepository = optionSnapshotRepository;
        this.assessmentSnapshotRepository = assessmentSnapshotRepository;
        this.questionSnapshotRepository = questionSnapshotRepository;
        this.questionSnapshotSubjectRepository = questionSnapshotSubjectRepository;
        this.subjectRepository = subjectRepository;
    }

    public record ScoringResult(List<AnswerResult> answerResults, List<SubjectScore> subjectScores) {}

    public ScoringResult scoreSubmission(Submission submission,
                                          List<UserAnswer> answers,
                                          List<UserAnswerSelectedOption> selectedOptions) {
        UUID snapshotId = submission.getAssessmentSnapshotId();

        AssessmentSnapshot snapshot = assessmentSnapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment snapshot not found"));
        double ptsCorrect = snapshot.getPtsCorrect().doubleValue();
        double ptsWrong = snapshot.getPtsWrong().doubleValue();

        // Batch-fetch QuestionSnapshot for per-question points
        List<UUID> allQuestionSnapshotIds = answers.stream()
                .map(UserAnswer::getQuestionSnapshotId)
                .toList();
        Map<UUID, QuestionSnapshot> qsMap = allQuestionSnapshotIds.isEmpty()
                ? Map.of()
                : questionSnapshotRepository.findByIdIn(allQuestionSnapshotIds).stream()
                        .collect(Collectors.toMap(QuestionSnapshot::getId, qs -> qs));

        List<UUID> mcQuestionSnapshotIds = answers.stream()
                .filter(a -> "multiple".equals(a.getType()))
                .map(UserAnswer::getQuestionSnapshotId)
                .toList();

        Map<UUID, Set<UUID>> correctMap = new HashMap<>();
        if (!mcQuestionSnapshotIds.isEmpty()) {
            List<OptionSnapshot> correctOpts = optionSnapshotRepository
                    .findByQuestionSnapshotIdInAndCorrectTrue(mcQuestionSnapshotIds);
            for (OptionSnapshot opt : correctOpts) {
                correctMap.computeIfAbsent(opt.getQuestionSnapshotId(), k -> new HashSet<>())
                        .add(opt.getId());
            }
        }

        Map<UUID, Set<UUID>> selectedByAnswer = new HashMap<>();
        for (UserAnswerSelectedOption aso : selectedOptions) {
            selectedByAnswer.computeIfAbsent(aso.getAnswerId(), k -> new HashSet<>())
                    .add(aso.getOptionSnapshotId());
        }

        double totalScore = 0.0;
        List<AnswerResult> answerResults = new ArrayList<>();

        for (UserAnswer answer : answers) {
            if ("multiple".equals(answer.getType())) {
                QuestionSnapshot qs = qsMap.get(answer.getQuestionSnapshotId());
                double questionPts = (qs != null && qs.getPoints() != null)
                        ? qs.getPoints().doubleValue() : ptsCorrect;

                Set<UUID> correctIds = correctMap.getOrDefault(
                        answer.getQuestionSnapshotId(), Set.of());
                Set<UUID> selectedIds = selectedByAnswer.getOrDefault(
                        answer.getId(), Set.of());

                if (selectedIds.isEmpty()) {
                    answer.setIsCorrect(null);
                    answer.setPointsAwarded(0.0);
                } else {
                    boolean isCorrect = selectedIds.equals(correctIds);
                    double points = isCorrect ? questionPts : ptsWrong;
                    answer.setIsCorrect(isCorrect);
                    answer.setPointsAwarded(points);
                    totalScore += points;
                }

                userAnswerRepository.save(answer);

                answerResults.add(new AnswerResult(
                        answer.getQuestionSnapshotId().toString(),
                        "multiple",
                        answer.getIsCorrect(),
                        correctIds.stream().map(UUID::toString).toList(),
                        answer.getPointsAwarded()
                ));
            } else {
                answerResults.add(new AnswerResult(
                        answer.getQuestionSnapshotId().toString(),
                        "open",
                        null,
                        List.of(),
                        null
                ));
            }
        }

        submission.setScore(totalScore);
        submissionRepository.save(submission);

        // Compute subject scores
        List<SubjectScore> subjectScores = computeSubjectScores(answers, snapshotId, qsMap, ptsCorrect);

        return new ScoringResult(answerResults, subjectScores);
    }

    /**
     * Compute subject scores for a set of answers.
     * Public so that SubmissionService can call it for read paths (e.g. getSubmission).
     */
    public List<SubjectScore> computeSubjectScores(List<UserAnswer> answers, UUID snapshotId) {
        List<UUID> questionSnapshotIds = answers.stream()
                .map(UserAnswer::getQuestionSnapshotId)
                .toList();
        AssessmentSnapshot snapshot = assessmentSnapshotRepository.findById(snapshotId)
                .orElse(null);
        double ptsCorrect = snapshot != null ? snapshot.getPtsCorrect().doubleValue() : 0.0;

        Map<UUID, QuestionSnapshot> qsMap = questionSnapshotIds.isEmpty()
                ? Map.of()
                : questionSnapshotRepository.findByIdIn(questionSnapshotIds).stream()
                        .collect(Collectors.toMap(QuestionSnapshot::getId, qs -> qs));

        return computeSubjectScores(answers, snapshotId, qsMap, ptsCorrect);
    }

    private List<SubjectScore> computeSubjectScores(List<UserAnswer> answers, UUID snapshotId,
                                                     Map<UUID, QuestionSnapshot> qsMap,
                                                     double defaultPtsCorrect) {
        List<UUID> questionSnapshotIds = answers.stream()
                .map(UserAnswer::getQuestionSnapshotId)
                .toList();

        if (questionSnapshotIds.isEmpty()) {
            return List.of();
        }

        List<QuestionSnapshotSubject> qsSubjects =
                questionSnapshotSubjectRepository.findByQuestionSnapshotIdIn(questionSnapshotIds);

        if (qsSubjects.isEmpty()) {
            return List.of();
        }

        // Collect unique subject IDs and fetch labels
        List<UUID> subjectIds = qsSubjects.stream()
                .map(QuestionSnapshotSubject::getSubjectId)
                .distinct()
                .toList();
        Map<UUID, String> subjectLabels = subjectRepository.findByIdIn(subjectIds).stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getLabel));

        // Build answer lookup: questionSnapshotId -> UserAnswer
        Map<UUID, UserAnswer> answerByQuestion = answers.stream()
                .collect(Collectors.toMap(UserAnswer::getQuestionSnapshotId, a -> a, (a, b) -> a));

        // Accumulate per-subject scores
        Map<UUID, double[]> subjectAccum = new HashMap<>(); // [pointsEarned, pointsAvailable]

        for (QuestionSnapshotSubject qss : qsSubjects) {
            UUID qsId = qss.getQuestionSnapshotId();
            UUID subjectId = qss.getSubjectId();
            double weight = qss.getWeight().doubleValue();

            QuestionSnapshot qs = qsMap.get(qsId);
            double questionMaxPts = (qs != null && qs.getPoints() != null)
                    ? qs.getPoints().doubleValue() : defaultPtsCorrect;

            UserAnswer answer = answerByQuestion.get(qsId);
            double pointsAwarded = (answer != null && answer.getPointsAwarded() != null)
                    ? answer.getPointsAwarded() : 0.0;

            double[] accum = subjectAccum.computeIfAbsent(subjectId, k -> new double[]{0.0, 0.0});
            accum[0] += pointsAwarded * weight;
            accum[1] += questionMaxPts * weight;
        }

        return subjectAccum.entrySet().stream()
                .map(e -> new SubjectScore(
                        e.getKey().toString(),
                        subjectLabels.getOrDefault(e.getKey(), "Unknown"),
                        e.getValue()[0],
                        e.getValue()[1]
                ))
                .toList();
    }
}
