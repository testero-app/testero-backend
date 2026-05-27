package app.testero.controller;

import app.testero.dto.SubmissionCreateRequest;
import app.testero.dto.SubmissionFeedbackResponse;
import app.testero.security.StudentPrincipal;
import app.testero.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<SubmissionFeedbackResponse> createSubmission(
            @Valid @RequestBody SubmissionCreateRequest request,
            @AuthenticationPrincipal StudentPrincipal principal) {
        SubmissionFeedbackResponse response = submissionService.createSubmission(
                principal.studentId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<SubmissionFeedbackResponse> getSubmission(
            @PathVariable UUID submissionId,
            @AuthenticationPrincipal StudentPrincipal principal) {
        SubmissionFeedbackResponse response = submissionService.getSubmission(
                submissionId, principal.studentId());
        return ResponseEntity.ok(response);
    }
}
