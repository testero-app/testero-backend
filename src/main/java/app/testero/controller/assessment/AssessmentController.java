package app.testero.controller.assessment;

import app.testero.dto.assessment.AssessmentConfigResponse;
import app.testero.dto.assessment.AssessmentListResponse;
import app.testero.dto.assessment.AssessmentQuestionsResponse;
import app.testero.dto.submission.SubmissionStartResponse;
import app.testero.entity.user.StudentProfile;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.user.StudentProfileRepository;
import app.testero.security.UserPrincipal;
import app.testero.service.user.AccessService;
import app.testero.service.assessment.AssessmentService;
import app.testero.service.assessment.SnapshotService;
import app.testero.service.submission.SubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/assessments")
@Tag(name = "Assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final SubmissionService submissionService;
    private final SnapshotService snapshotService;
    private final StudentProfileRepository studentProfileRepository;
    private final AccessService accessService;

    public AssessmentController(AssessmentService assessmentService,
                                SubmissionService submissionService,
                                SnapshotService snapshotService,
                                StudentProfileRepository studentProfileRepository,
                                AccessService accessService) {
        this.assessmentService = assessmentService;
        this.submissionService = submissionService;
        this.snapshotService = snapshotService;
        this.studentProfileRepository = studentProfileRepository;
        this.accessService = accessService;
    }

    @GetMapping
    public ResponseEntity<AssessmentListResponse> getAvailableAssessments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        StudentProfile profile = studentProfileRepository.findByUserId(principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
        UUID classId = profile.getClassId();
        return ResponseEntity.ok(
                assessmentService.getAvailableAssessments(classId, principal.userId(), page, size));
    }

    @GetMapping("/{snapshotId}/config")
    public ResponseEntity<AssessmentConfigResponse> getAssessmentConfig(
            @PathVariable String snapshotId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(assessmentService.getAssessmentConfig(snapshotId));
    }

    @PostMapping("/{snapshotId}/start")
    public ResponseEntity<SubmissionStartResponse> startAssessment(
            @PathVariable String snapshotId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(submissionService.startSubmission(
                        UUID.fromString(snapshotId), principal.userId()));
    }

    @GetMapping("/{snapshotId}/questions")
    public ResponseEntity<AssessmentQuestionsResponse> getAssessmentQuestions(
            @PathVariable String snapshotId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(assessmentService.getAssessmentQuestions(snapshotId));
    }

    /**
     * Publishing freezes an assessment into a snapshot that students can then be made to
     * sit. Only the teacher who owns the template may publish it; an admin may publish any
     * template, including platform content. Everyone else — students included — is forbidden.
     */
    @PostMapping("/{assessmentId}/publish")
    public ResponseEntity<Void> publishAssessment(
            @PathVariable UUID assessmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        accessService.requireCanManageAssessment(principal.userId(), assessmentId);
        snapshotService.publishSnapshot(assessmentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
