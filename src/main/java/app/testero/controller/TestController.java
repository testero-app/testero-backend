package app.testero.controller;

import app.testero.dto.TestConfigResponse;
import app.testero.dto.TestListResponse;
import app.testero.dto.TestQuestionsResponse;
import app.testero.security.StudentPrincipal;
import app.testero.service.TestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/tests")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping
    public ResponseEntity<TestListResponse> getAvailableTests(
            @AuthenticationPrincipal StudentPrincipal principal) {
        return ResponseEntity.ok(testService.getAvailableTests(principal.classId()));
    }

    @GetMapping("/{testId}/config")
    public ResponseEntity<TestConfigResponse> getTestConfig(
            @PathVariable String testId,
            @AuthenticationPrincipal StudentPrincipal principal) {
        return ResponseEntity.ok(testService.getTestConfig(testId));
    }

    @PostMapping("/{testId}/start")
    public ResponseEntity<Map<String, Boolean>> recordTestStart(
            @PathVariable String testId,
            @AuthenticationPrincipal StudentPrincipal principal) {
        testService.recordTestStart(testId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/{testId}/questions")
    public ResponseEntity<TestQuestionsResponse> getTestQuestions(
            @PathVariable String testId,
            @AuthenticationPrincipal StudentPrincipal principal) {
        return ResponseEntity.ok(testService.getTestQuestions(testId));
    }
}
