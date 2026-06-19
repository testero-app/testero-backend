package app.testero.controller;

import app.testero.dto.TrainingStartRequest;
import app.testero.dto.TrainingStartResponse;
import app.testero.security.UserPrincipal;
import app.testero.service.TrainingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/training")
@Tag(name = "Training")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping("/start")
    public ResponseEntity<TrainingStartResponse> startTraining(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TrainingStartRequest request) {
        return ResponseEntity.ok(trainingService.startTraining(request, principal.userId()));
    }
}
