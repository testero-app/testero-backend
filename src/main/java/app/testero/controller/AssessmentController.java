package app.testero.controller;

import app.testero.dto.AssessmentConfigResponse;
import app.testero.dto.AssessmentListResponse;
import app.testero.dto.AssessmentQuestionsResponse;
import app.testero.dto.SubmissionStartResponse;
import app.testero.entity.user.StudentProfile;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.StudentProfileRepository;
import app.testero.security.UserPrincipal;
import app.testero.service.AssessmentService;
import app.testero.service.SnapshotService;
import app.testero.service.SubmissionService;
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

    public AssessmentController(AssessmentService assessmentService,
                                SubmissionService submissionService,
                                SnapshotService snapshotService,
                                StudentProfileRepository studentProfileRepository) {
        this.assessmentService = assessmentService;
        this.submissionService = submissionService;
        this.snapshotService = snapshotService;
        this.studentProfileRepository = studentProfileRepository;
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

    @PostMapping("/{assessmentId}/publish")
    public ResponseEntity<Void> publishAssessment(
            @PathVariable UUID assessmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        snapshotService.publishSnapshot(assessmentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
