package app.testero.controller;

import app.testero.dto.AssessmentConfigResponse;
import app.testero.dto.AssessmentListResponse;
import app.testero.dto.AssessmentQuestionsResponse;
import app.testero.entity.user.StudentProfile;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.StudentProfileRepository;
import app.testero.security.UserPrincipal;
import app.testero.service.AssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final StudentProfileRepository studentProfileRepository;

    public AssessmentController(AssessmentService assessmentService,
                                StudentProfileRepository studentProfileRepository) {
        this.assessmentService = assessmentService;
        this.studentProfileRepository = studentProfileRepository;
    }

    @GetMapping
    public ResponseEntity<AssessmentListResponse> getAvailableAssessments(
            @AuthenticationPrincipal UserPrincipal principal) {
        StudentProfile profile = studentProfileRepository.findByUserId(principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
        UUID classId = profile.getClassId();
        return ResponseEntity.ok(assessmentService.getAvailableAssessments(classId));
    }

    @GetMapping("/{assessmentId}/config")
    public ResponseEntity<AssessmentConfigResponse> getAssessmentConfig(
            @PathVariable String assessmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(assessmentService.getAssessmentConfig(assessmentId));
    }

    @PostMapping("/{assessmentId}/start")
    public ResponseEntity<Map<String, Boolean>> recordAssessmentStart(
            @PathVariable String assessmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        assessmentService.recordAssessmentStart(assessmentId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/{assessmentId}/questions")
    public ResponseEntity<AssessmentQuestionsResponse> getAssessmentQuestions(
            @PathVariable String assessmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(assessmentService.getAssessmentQuestions(assessmentId));
    }
}
