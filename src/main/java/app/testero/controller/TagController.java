package app.testero.controller;

import app.testero.dto.TagRequest;
import app.testero.dto.TagResponse;
import app.testero.security.UserPrincipal;
import app.testero.service.TagService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A teacher's private tag vocabulary. Every operation acts on the caller's own tags
 * (an admin may act on any); a student is forbidden.
 */
@RestController
@RequestMapping("/tags")
@Tag(name = "Tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ResponseEntity<TagResponse> create(
            @Valid @RequestBody TagRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        TagResponse created = tagService.create(principal.userId(), request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<TagResponse> listOwn(@AuthenticationPrincipal UserPrincipal principal) {
        return tagService.listOwn(principal.userId());
    }

    @PatchMapping("/{tagId}")
    public TagResponse rename(
            @PathVariable UUID tagId,
            @Valid @RequestBody TagRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return tagService.rename(principal.userId(), tagId, request.name());
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID tagId,
            @AuthenticationPrincipal UserPrincipal principal) {
        tagService.delete(principal.userId(), tagId);
        return ResponseEntity.noContent().build();
    }
}
