package app.testero.service;

import app.testero.dto.SubmissionFeedbackResponse.AnswerResult;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.snapshot.OptionSnapshot;
import app.testero.entity.submission.Submission;
import app.testero.entity.submission.UserAnswer;
import app.testero.entity.submission.UserAnswerSelectedOption;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.OptionSnapshotRepository;
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

@Service
public class ScoringService {

    private final SubmissionRepository submissionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final OptionSnapshotRepository optionSnapshotRepository;
    private final AssessmentSnapshotRepository assessmentSnapshotRepository;

    public ScoringService(SubmissionRepository submissionRepository,
                          UserAnswerRepository userAnswerRepository,
                          OptionSnapshotRepository optionSnapshotRepository,
                          AssessmentSnapshotRepository assessmentSnapshotRepository) {
        this.submissionRepository = submissionRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.optionSnapshotRepository = optionSnapshotRepository;
        this.assessmentSnapshotRepository = assessmentSnapshotRepository;
    }

    public List<AnswerResult> scoreSubmission(Submission submission,
                                               List<UserAnswer> answers,
                                               List<UserAnswerSelectedOption> selectedOptions) {
        UUID snapshotId = submission.getAssessmentSnapshotId();

        AssessmentSnapshot snapshot = assessmentSnapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment snapshot not found"));
        double ptsCorrect = snapshot.getPtsCorrect().doubleValue();
        double ptsWrong = snapshot.getPtsWrong().doubleValue();

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
                Set<UUID> correctIds = correctMap.getOrDefault(
                        answer.getQuestionSnapshotId(), Set.of());
                Set<UUID> selectedIds = selectedByAnswer.getOrDefault(
                        answer.getId(), Set.of());

                if (selectedIds.isEmpty()) {
                    answer.setIsCorrect(null);
                    answer.setPointsAwarded(0.0);
                } else {
                    boolean isCorrect = selectedIds.equals(correctIds);
                    double points = isCorrect ? ptsCorrect : ptsWrong;
                    answer.setIsCorrect(isCorrect);
                    answer.setPointsAwarded(points);
                    totalScore += points;
                }

                userAnswerRepository.save(answer);

                answerResults.add(new AnswerResult(
                        answer.getQuestionSnapshotId().toString(),
                        "multiple",
                        answer.getIsCorrect(),
                        correctIds.stream().map(UUID::toString).toList()
                ));
            } else {
                answerResults.add(new AnswerResult(
                        answer.getQuestionSnapshotId().toString(),
                        "open",
                        null,
                        List.of()
                ));
            }
        }

        submission.setScore(totalScore);
        submissionRepository.save(submission);

        return answerResults;
    }
}
