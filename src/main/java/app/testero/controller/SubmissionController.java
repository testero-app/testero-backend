package app.testero.controller;

import app.testero.dto.SaveAnswerRequest;
import app.testero.dto.SavedAnswersResponse;
import app.testero.dto.SubmissionFeedbackResponse;
import app.testero.dto.SubmissionHistoryResponse;
import app.testero.dto.SubmissionReviewResponse;
import app.testero.dto.SubmissionSubmitRequest;
import app.testero.security.UserPrincipal;
import app.testero.service.SubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/submissions")
@Tag(name = "Submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping("/mine")
    public ResponseEntity<SubmissionHistoryResponse> getMySubmissions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                submissionService.getSubmissionHistory(principal.userId(), page, size));
    }

    @GetMapping("/{submissionId}/answers")
    public ResponseEntity<SavedAnswersResponse> getSavedAnswers(
            @PathVariable UUID submissionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                submissionService.getSavedAnswers(submissionId, principal.userId()));
    }

    @PutMapping("/{submissionId}/answers/{questionSnapshotId}")
    public ResponseEntity<Void> saveAnswer(
            @PathVariable UUID submissionId,
            @PathVariable UUID questionSnapshotId,
            @Valid @RequestBody SaveAnswerRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        submissionService.saveAnswer(submissionId, questionSnapshotId, principal.userId(), request);
        return ResponseEntity.noContent().build();
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

    @GetMapping("/{submissionId}/review")
    public ResponseEntity<SubmissionReviewResponse> getSubmissionReview(
            @PathVariable UUID submissionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                submissionService.getSubmissionReview(submissionId, principal.userId()));
    }
}
