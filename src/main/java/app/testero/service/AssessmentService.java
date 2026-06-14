package app.testero.service;

import app.testero.dto.AssessmentConfigResponse;
import app.testero.dto.AssessmentListResponse;
import app.testero.dto.AssessmentQuestionsResponse;
import app.testero.dto.AssessmentQuestionsResponse.OptionDto;
import app.testero.dto.AssessmentQuestionsResponse.QuestionDto;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.snapshot.OptionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshot;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.OptionSnapshotRepository;
import app.testero.repository.QuestionSnapshotRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    private final AssessmentSnapshotRepository snapshotRepository;
    private final QuestionSnapshotRepository questionSnapshotRepository;
    private final OptionSnapshotRepository optionSnapshotRepository;
    private final QuestionPrepService questionPrepService;

    public AssessmentService(AssessmentSnapshotRepository snapshotRepository,
                             QuestionSnapshotRepository questionSnapshotRepository,
                             OptionSnapshotRepository optionSnapshotRepository,
                             QuestionPrepService questionPrepService) {
        this.snapshotRepository = snapshotRepository;
        this.questionSnapshotRepository = questionSnapshotRepository;
        this.optionSnapshotRepository = optionSnapshotRepository;
        this.questionPrepService = questionPrepService;
    }

    public AssessmentListResponse getAvailableAssessments(UUID classId) {
        List<AssessmentSnapshot> snapshots = snapshotRepository.findSnapshotsByClassId(classId);

        List<AssessmentListResponse.AssessmentListItem> items = snapshots.stream()
                .map(s -> new AssessmentListResponse.AssessmentListItem(
                        s.getId().toString(),
                        s.getTitle(),
                        s.getPublishedAt().toLocalDate().toString(),
                        s.getTimerMinutes(),
                        s.getQuestionsPerAssessment(),
                        s.getDifficulty() != null ? s.getDifficulty().name() : null
                ))
                .toList();

        return new AssessmentListResponse(items);
    }

    public AssessmentConfigResponse getAssessmentConfig(String snapshotId) {
        AssessmentSnapshot snapshot = findSnapshotOrThrow(snapshotId);

        return new AssessmentConfigResponse(
                snapshot.getId().toString(),
                snapshot.getTitle(),
                snapshot.getPublishedAt().toLocalDate().toString(),
                snapshot.getTimerMinutes(),
                snapshot.getQuestionsPerAssessment(),
                snapshot.getQuestionsPerAssessment(),
                new AssessmentConfigResponse.ScoringRules(
                        snapshot.getPtsCorrect().doubleValue(),
                        snapshot.getPtsWrong().doubleValue()
                )
        );
    }

    public AssessmentQuestionsResponse getAssessmentQuestions(String snapshotId) {
        AssessmentSnapshot snapshot = findSnapshotOrThrow(snapshotId);

        List<QuestionSnapshot> questions = questionSnapshotRepository
                .findByAssessmentSnapshotIdOrderByPosition(snapshot.getId());
        List<UUID> questionIds = questions.stream().map(QuestionSnapshot::getId).toList();

        List<OptionSnapshot> allOptions = questionIds.isEmpty()
                ? List.of()
                : optionSnapshotRepository.findByQuestionSnapshotIdInOrderByPosition(questionIds);

        Map<UUID, List<OptionSnapshot>> optionsByQuestion = allOptions.stream()
                .collect(Collectors.groupingBy(OptionSnapshot::getQuestionSnapshotId));

        List<QuestionDto> questionDtos = questions.stream()
                .map(q -> {
                    List<OptionSnapshot> opts = optionsByQuestion
                            .getOrDefault(q.getId(), List.of());
                    List<OptionDto> optionDtos = opts.stream()
                            .map(o -> new OptionDto(o.getId().toString(), o.getText(), o.isFallback()))
                            .toList();
                    return new QuestionDto(
                            q.getId().toString(),
                            q.getType(),
                            q.getText(),
                            q.getCode(),
                            "multiple".equals(q.getType()) ? optionDtos : null
                    );
                })
                .toList();

        // Run through preparation pipeline: random subset, shuffle, shuffle options
        List<QuestionDto> prepared = questionPrepService.prepare(
                new ArrayList<>(questionDtos),
                snapshot.getQuestionsPerAssessment()
        );

        return new AssessmentQuestionsResponse(
                snapshot.getId().toString(),
                snapshot.getTitle(),
                snapshot.getPublishedAt().toLocalDate().toString(),
                snapshot.getTimerMinutes(),
                prepared.size(),
                prepared
        );
    }

    private AssessmentSnapshot findSnapshotOrThrow(String snapshotId) {
        return snapshotRepository.findById(UUID.fromString(snapshotId))
                .orElseThrow(() -> new ResourceNotFoundException("Assessment snapshot not found"));
    }
}
