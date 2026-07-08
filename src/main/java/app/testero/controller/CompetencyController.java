package app.testero.controller;

import app.testero.dto.CompetencyResponse;
import app.testero.security.UserPrincipal;
import app.testero.service.CompetencyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/competencies")
@Tag(name = "Competencies")
public class CompetencyController {

    private final CompetencyService competencyService;

    public CompetencyController(CompetencyService competencyService) {
        this.competencyService = competencyService;
    }

    @GetMapping
    public ResponseEntity<CompetencyResponse> getCompetencies(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(competencyService.calculateMastery(principal.userId()));
    }
}
