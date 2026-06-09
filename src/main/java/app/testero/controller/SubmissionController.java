package app.testero.controller;

import app.testero.dto.SubmissionFeedbackResponse;
import app.testero.dto.SubmissionSubmitRequest;
import app.testero.security.UserPrincipal;
import app.testero.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PutMapping("/{submissionId}")
    public ResponseEntity<SubmissionFeedbackResponse> submitAnswers(
            @PathVariable UUID submissionId,
            @Valid @RequestBody SubmissionSubmitRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(submissionService.submitAnswers(
                submissionId, principal.userId(), request));
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<SubmissionFeedbackResponse> getSubmission(
            @PathVariable UUID submissionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        SubmissionFeedbackResponse response = submissionService.getSubmission(
                submissionId, principal.userId());
        return ResponseEntity.ok(response);
    }
}
