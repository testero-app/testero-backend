package app.testero.controller;

import app.testero.dto.TestConfigResponse;
import app.testero.dto.TestListResponse;
import app.testero.dto.TestQuestionsResponse;
import app.testero.entity.StudentProfile;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.StudentProfileRepository;
import app.testero.security.UserPrincipal;
import app.testero.service.TestService;
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
@RequestMapping("/tests")
public class TestController {

    private final TestService testService;
    private final StudentProfileRepository studentProfileRepository;

    public TestController(TestService testService,
                          StudentProfileRepository studentProfileRepository) {
        this.testService = testService;
        this.studentProfileRepository = studentProfileRepository;
    }

    @GetMapping
    public ResponseEntity<TestListResponse> getAvailableTests(
            @AuthenticationPrincipal UserPrincipal principal) {
        StudentProfile profile = studentProfileRepository.findByUserId(principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
        UUID classId = profile.getClassId();
        return ResponseEntity.ok(testService.getAvailableTests(classId));
    }

    @GetMapping("/{testId}/config")
    public ResponseEntity<TestConfigResponse> getTestConfig(
            @PathVariable String testId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(testService.getTestConfig(testId));
    }

    @PostMapping("/{testId}/start")
    public ResponseEntity<Map<String, Boolean>> recordTestStart(
            @PathVariable String testId,
            @AuthenticationPrincipal UserPrincipal principal) {
        testService.recordTestStart(testId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/{testId}/questions")
    public ResponseEntity<TestQuestionsResponse> getTestQuestions(
            @PathVariable String testId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(testService.getTestQuestions(testId));
    }
}
