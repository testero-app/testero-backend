package app.testero.service;

import app.testero.dto.AssessmentConfigResponse;
import app.testero.dto.AssessmentListResponse;
import app.testero.dto.AssessmentQuestionsResponse;
import app.testero.dto.AssessmentQuestionsResponse.OptionDto;
import app.testero.dto.AssessmentQuestionsResponse.QuestionDto;
import app.testero.dto.PaginationMetadata;
import app.testero.dto.SubjectDto;
import app.testero.entity.assessment.AssessmentSubject;
import app.testero.entity.assessment.Subject;
import app.testero.entity.snapshot.AssessmentSnapshot;
import app.testero.entity.snapshot.OptionSnapshot;
import app.testero.entity.snapshot.QuestionSnapshot;
import app.testero.entity.submission.Submission;
import app.testero.entity.submission.SubmissionStatus;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.AssessmentSnapshotRepository;
import app.testero.repository.AssessmentSubjectRepository;
import app.testero.repository.OptionSnapshotRepository;
import app.testero.repository.QuestionSnapshotRepository;
import app.testero.repository.SubjectRepository;
import app.testero.repository.SubmissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final SubmissionRepository submissionRepository;
    private final QuestionPrepService questionPrepService;
    private final AssessmentSubjectRepository assessmentSubjectRepository;
    private final SubjectRepository subjectRepository;

    public AssessmentService(AssessmentSnapshotRepository snapshotRepository,
                             QuestionSnapshotRepository questionSnapshotRepository,
                             OptionSnapshotRepository optionSnapshotRepository,
                             SubmissionRepository submissionRepository,
                             QuestionPrepService questionPrepService,
                             AssessmentSubjectRepository assessmentSubjectRepository,
                             SubjectRepository subjectRepository) {
        this.snapshotRepository = snapshotRepository;
        this.questionSnapshotRepository = questionSnapshotRepository;
        this.optionSnapshotRepository = optionSnapshotRepository;
        this.submissionRepository = submissionRepository;
        this.questionPrepService = questionPrepService;
        this.assessmentSubjectRepository = assessmentSubjectRepository;
        this.subjectRepository = subjectRepository;
    }

    public AssessmentListResponse getAvailableAssessments(UUID classId, UUID userId,
                                                           int page, int size) {
        Page<AssessmentSnapshot> snapshotPage = snapshotRepository
                .findSnapshotsByClassId(classId, PageRequest.of(page, size));
        List<AssessmentSnapshot> snapshots = snapshotPage.getContent();

        List<UUID> snapshotIds = snapshots.stream().map(AssessmentSnapshot::getId).toList();
        Map<UUID, Submission> latestSubmissionBySnapshot = snapshotIds.isEmpty()
                ? Map.of()
                : submissionRepository.findByUserIdAndAssessmentSnapshotIdIn(userId, snapshotIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Submission::getAssessmentSnapshotId,
                                s -> s,
                                (a, b) -> resolveLatest(a, b)
                        ));

        // Batch-fetch subjects for all assessments
        List<UUID> assessmentIds = snapshots.stream()
                .map(AssessmentSnapshot::getAssessmentId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<UUID, List<SubjectDto>> subjectsByAssessment = fetchSubjectsByAssessmentIds(assessmentIds);

        List<AssessmentListResponse.AssessmentListItem> items = snapshots.stream()
                .map(s -> {
                    Submission sub = latestSubmissionBySnapshot.get(s.getId());
                    String status;
                    Double score;
                    if (sub == null) {
                        status = "NOT_STARTED";
                        score = null;
                    } else if (sub.getStatus() == SubmissionStatus.IN_PROGRESS) {
                        status = "IN_PROGRESS";
                        score = null;
                    } else {
                        status = "COMPLETED";
                        score = sub.getScore();
                    }
                    List<SubjectDto> subjects = s.getAssessmentId() != null
                            ? subjectsByAssessment.getOrDefault(s.getAssessmentId(), List.of())
                            : List.of();
                    return new AssessmentListResponse.AssessmentListItem(
                            s.getId().toString(),
                            s.getTitle(),
                            s.getPublishedAt().toLocalDate().toString(),
                            s.getTimerMinutes(),
                            s.getQuestionsPerAssessment(),
                            s.getDifficulty() != null ? s.getDifficulty().name() : null,
                            status,
                            score,
                            subjects
                    );
                })
                .toList();

        PaginationMetadata pagination = new PaginationMetadata(
                snapshotPage.getTotalElements(),
                snapshotPage.getTotalPages(),
                snapshotPage.getNumber(),
                snapshotPage.getSize());

        return new AssessmentListResponse(items, pagination);
    }

    /**
     * When multiple submissions exist for the same snapshot, prefer COMPLETED over IN_PROGRESS.
     * Among same-status submissions, prefer the most recent one.
     */
    private static Submission resolveLatest(Submission a, Submission b) {
        boolean aCompleted = a.getStatus() != SubmissionStatus.IN_PROGRESS;
        boolean bCompleted = b.getStatus() != SubmissionStatus.IN_PROGRESS;
        if (aCompleted != bCompleted) {
            return aCompleted ? a : b;
        }
        // Both same status — prefer the one with the latest timestamp
        LocalDateTime aTime = a.getSubmittedAt() != null ? a.getSubmittedAt() : a.getStartedAt();
        LocalDateTime bTime = b.getSubmittedAt() != null ? b.getSubmittedAt() : b.getStartedAt();
        if (aTime == null) {
            return b;
        }
        if (bTime == null) {
            return a;
        }
        return aTime.isAfter(bTime) ? a : b;
    }

    public AssessmentConfigResponse getAssessmentConfig(String snapshotId) {
        AssessmentSnapshot snapshot = findSnapshotOrThrow(snapshotId);

        List<SubjectDto> subjects = snapshot.getAssessmentId() != null
                ? fetchSubjectsForAssessment(snapshot.getAssessmentId())
                : List.of();

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
                ),
                subjects
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
                            "multiple".equals(q.getType()) ? optionDtos : null,
                            q.getPoints() != null ? q.getPoints().doubleValue() : null
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

    private List<SubjectDto> fetchSubjectsForAssessment(UUID assessmentId) {
        List<AssessmentSubject> links = assessmentSubjectRepository.findByAssessmentId(assessmentId);
        if (links.isEmpty()) {
            return List.of();
        }
        List<UUID> subjectIds = links.stream().map(AssessmentSubject::getSubjectId).toList();
        Map<UUID, String> labels = subjectRepository.findByIdIn(subjectIds).stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getLabel));
        return links.stream()
                .map(l -> new SubjectDto(l.getSubjectId().toString(),
                        labels.getOrDefault(l.getSubjectId(), "Unknown")))
                .toList();
    }

    private Map<UUID, List<SubjectDto>> fetchSubjectsByAssessmentIds(List<UUID> assessmentIds) {
        if (assessmentIds.isEmpty()) {
            return Map.of();
        }
        List<AssessmentSubject> allLinks = assessmentSubjectRepository.findByAssessmentIdIn(assessmentIds);
        if (allLinks.isEmpty()) {
            return Map.of();
        }
        List<UUID> subjectIds = allLinks.stream()
                .map(AssessmentSubject::getSubjectId)
                .distinct()
                .toList();
        Map<UUID, String> labels = subjectRepository.findByIdIn(subjectIds).stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getLabel));

        return allLinks.stream()
                .collect(Collectors.groupingBy(
                        AssessmentSubject::getAssessmentId,
                        Collectors.mapping(
                                l -> new SubjectDto(l.getSubjectId().toString(),
                                        labels.getOrDefault(l.getSubjectId(), "Unknown")),
                                Collectors.toList()
                        )
                ));
    }
}
