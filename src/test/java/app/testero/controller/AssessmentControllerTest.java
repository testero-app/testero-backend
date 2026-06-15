package app.testero.controller;

import app.testero.config.CorsProperties;
import app.testero.config.JwtProperties;
import app.testero.config.SecurityConfig;
import app.testero.dto.AssessmentConfigResponse;
import app.testero.dto.AssessmentListResponse;
import app.testero.dto.AssessmentListResponse.AssessmentListItem;
import app.testero.dto.PaginationMetadata;
import app.testero.dto.AssessmentQuestionsResponse;
import app.testero.dto.AssessmentQuestionsResponse.OptionDto;
import app.testero.dto.AssessmentQuestionsResponse.QuestionDto;
import app.testero.dto.SubmissionStartResponse;
import app.testero.entity.user.StudentProfile;
import app.testero.exception.ResourceNotFoundException;
import app.testero.repository.StudentProfileRepository;
import app.testero.security.JwtService;
import app.testero.security.UserPrincipal;
import app.testero.service.AssessmentService;
import app.testero.service.SnapshotService;
import app.testero.service.SubmissionService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssessmentController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
@ActiveProfiles("test")
class AssessmentControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean AssessmentService assessmentService;
    @MockitoBean SubmissionService submissionService;
    @MockitoBean SnapshotService snapshotService;
    @MockitoBean StudentProfileRepository studentProfileRepository;
    @MockitoBean JwtService jwtService;

    private static final UUID USER_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");
    private static final UUID CLASS_ID = UUID.fromString("bb000000-0000-0000-0000-000000000001");
    private static final String ASSESSMENT_ID = "cc000000-0000-0000-0000-000000000001";

    private static RequestPostProcessor jwt() {
        var principal = new UserPrincipal(USER_ID, "testuser");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        return authentication(auth);
    }

    private StudentProfile buildProfile() {
        StudentProfile profile = new StudentProfile();
        profile.setUserId(USER_ID);
        profile.setClassId(CLASS_ID);
        return profile;
    }

    // ── GET /assessments ───────────────────────────────────────────

    @Nested
    @DisplayName("GET /assessments")
    class GetAssessments {

        @Test
        @DisplayName("authenticated → 200 with assessment list")
        void success() throws Exception {
            when(studentProfileRepository.findByUserId(USER_ID))
                    .thenReturn(Optional.of(buildProfile()));
            when(assessmentService.getAvailableAssessments(CLASS_ID, USER_ID, 0, 20))
                    .thenReturn(new AssessmentListResponse(List.of(
                            new AssessmentListItem(ASSESSMENT_ID, "Test 1", "2026-06-15", 45, 5, "INTERMEDIATE", "NOT_STARTED", null)
                    ), new PaginationMetadata(1, 1, 0, 20)));

            mockMvc.perform(get("/assessments").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.assessments[0].id").value(ASSESSMENT_ID))
                    .andExpect(jsonPath("$.assessments[0].title").value("Test 1"));
        }

        @Test
        @DisplayName("no token → 403")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/assessments"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("student profile not found → 404")
        void profileNotFound() throws Exception {
            when(studentProfileRepository.findByUserId(USER_ID))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/assessments").with(jwt()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").exists());
        }
    }

    // ── GET /assessments/{id}/config ───────────────────────────────

    @Nested
    @DisplayName("GET /assessments/{id}/config")
    class GetConfig {

        @Test
        @DisplayName("authenticated → 200 with config")
        void success() throws Exception {
            when(assessmentService.getAssessmentConfig(ASSESSMENT_ID))
                    .thenReturn(new AssessmentConfigResponse(
                            ASSESSMENT_ID, "Test 1", "2026-06-15", 45, 10, 5,
                            new AssessmentConfigResponse.ScoringRules(1.0, -0.25)
                    ));

            mockMvc.perform(get("/assessments/{id}/config", ASSESSMENT_ID).with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.assessmentId").value(ASSESSMENT_ID))
                    .andExpect(jsonPath("$.scoring.pointsPerCorrect").value(1.0))
                    .andExpect(jsonPath("$.scoring.pointsPerWrong").value(-0.25));
        }

        @Test
        @DisplayName("not found → 404")
        void notFound() throws Exception {
            when(assessmentService.getAssessmentConfig(ASSESSMENT_ID))
                    .thenThrow(new ResourceNotFoundException("Assessment not found"));

            mockMvc.perform(get("/assessments/{id}/config", ASSESSMENT_ID).with(jwt()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("Assessment not found"));
        }
    }

    // ── POST /assessments/{id}/start ───────────────────────────────

    @Nested
    @DisplayName("POST /assessments/{id}/start")
    class StartAssessment {

        @Test
        @DisplayName("authenticated → 201 with submission_id and started_at")
        void success() throws Exception {
            when(submissionService.startSubmission(
                    eq(UUID.fromString(ASSESSMENT_ID)), eq(USER_ID)))
                    .thenReturn(new SubmissionStartResponse(
                            "ff000000-0000-0000-0000-000000000001",
                            "2026-06-15T10:00:00"));

            mockMvc.perform(post("/assessments/{id}/start", ASSESSMENT_ID).with(jwt()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.submission_id").value("ff000000-0000-0000-0000-000000000001"))
                    .andExpect(jsonPath("$.started_at").value("2026-06-15T10:00:00"));
        }
    }

    // ── GET /assessments/{id}/questions ────────────────────────────

    @Nested
    @DisplayName("GET /assessments/{id}/questions")
    class GetQuestions {

        @Test
        @DisplayName("authenticated → 200 with questions")
        void success() throws Exception {
            var questions = List.of(
                    new QuestionDto("q1", "multiple", "What is 2+2?", null,
                            List.of(new OptionDto("o1", "4", false), new OptionDto("o2", "5", false)))
            );
            when(assessmentService.getAssessmentQuestions(ASSESSMENT_ID))
                    .thenReturn(new AssessmentQuestionsResponse(
                            ASSESSMENT_ID, "Test 1", "2026-06-15", 45, 1, questions
                    ));

            mockMvc.perform(get("/assessments/{id}/questions", ASSESSMENT_ID).with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.assessmentId").value(ASSESSMENT_ID))
                    .andExpect(jsonPath("$.questions[0].id").value("q1"))
                    .andExpect(jsonPath("$.questions[0].options").isArray());
        }

        @Test
        @DisplayName("no token → 403")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/assessments/{id}/questions", ASSESSMENT_ID))
                    .andExpect(status().isForbidden());
        }
    }
}
