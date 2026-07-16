package app.testero.controller;

import app.testero.dto.TaggedQuestionResponse;
import app.testero.security.UserPrincipal;
import app.testero.service.TagService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Teacher-facing question-bank operations: tagging a question and browsing the bank by tag.
 *
 * <p>Distinct from the student assessment flow — these act on editable question templates the
 * caller owns, and return the full template (with tags), never the answer-hiding student view.
 */
@RestController
@RequestMapping("/questions")
@Tag(name = "Questions")
public class QuestionController {

    private final TagService tagService;

    public QuestionController(TagService tagService) {
        this.tagService = tagService;
    }

    /** List the caller's own questions carrying the given tag. */
    @GetMapping
    public List<TaggedQuestionResponse> byTag(
            @RequestParam("tagId") UUID tagId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return tagService.questionsByTag(principal.userId(), tagId);
    }

    @PostMapping("/{questionId}/tags/{tagId}")
    public ResponseEntity<Void> attachTag(
            @PathVariable UUID questionId,
            @PathVariable UUID tagId,
            @AuthenticationPrincipal UserPrincipal principal) {
        tagService.attach(principal.userId(), questionId, tagId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{questionId}/tags/{tagId}")
    public ResponseEntity<Void> detachTag(
            @PathVariable UUID questionId,
            @PathVariable UUID tagId,
            @AuthenticationPrincipal UserPrincipal principal) {
        tagService.detach(principal.userId(), questionId, tagId);
        return ResponseEntity.noContent().build();
    }
}
