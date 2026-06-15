package app.testero.controller;

import app.testero.config.CorsProperties;
import app.testero.config.JwtProperties;
import app.testero.config.SecurityConfig;
import app.testero.dto.SubmissionFeedbackResponse;
import app.testero.dto.SubmissionFeedbackResponse.AnswerResult;
import app.testero.exception.IllegalSubmissionStateException;
import app.testero.exception.ResourceNotFoundException;
import app.testero.security.JwtService;
import app.testero.security.UserPrincipal;
import app.testero.service.SubmissionService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubmissionController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
@ActiveProfiles("test")
class SubmissionControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean SubmissionService submissionService;
    @MockitoBean JwtService jwtService;

    private static final UUID USER_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");
    private static final UUID SUBMISSION_ID = UUID.fromString("ff000000-0000-0000-0000-000000000001");

    private static RequestPostProcessor jwt() {
        var principal = new UserPrincipal(USER_ID, "testuser");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        return authentication(auth);
    }

    private SubmissionFeedbackResponse buildFeedback() {
        return new SubmissionFeedbackResponse(
                SUBMISSION_ID.toString(),
                USER_ID.toString(),
                "snapshot-1",
                "2026-06-09T10:00:00",
                "2026-06-09T10:30:00",
                4.0,
                16.0,
                true,
                3.0,
                List.of(new AnswerResult("q1", "multiple", true, List.of("opt-1"), 1.0)),
                List.of()
        );
    }

    // ── PUT /submissions/{id} ─────────────────────────────────────

    @Nested
    @DisplayName("PUT /submissions/{id}")
    class SubmitAnswers {

        @Test
        @DisplayName("valid request → 200 with feedback")
        void success() throws Exception {
            when(submissionService.submitAnswers(eq(SUBMISSION_ID), eq(USER_ID), any()))
                    .thenReturn(buildFeedback());

            mockMvc.perform(put("/submissions/{id}", SUBMISSION_ID)
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "answers": [
                                        {
                                          "question_id": "q1",
                                          "type": "multiple",
                                          "selected_option_ids": ["opt-1"]
                                        }
                                      ]
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(SUBMISSION_ID.toString()))
                    .andExpect(jsonPath("$.answers[0].is_correct").value(true));
        }

        @Test
        @DisplayName("empty answers list → 400")
        void emptyAnswers() throws Exception {
            mockMvc.perform(put("/submissions/{id}", SUBMISSION_ID)
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "answers": []
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").exists());
        }

        @Test
        @DisplayName("submission not found → 404")
        void notFound() throws Exception {
            when(submissionService.submitAnswers(eq(SUBMISSION_ID), eq(USER_ID), any()))
                    .thenThrow(new ResourceNotFoundException("Submission not found"));

            mockMvc.perform(put("/submissions/{id}", SUBMISSION_ID)
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "answers": [
                                        {"question_id": "q1", "type": "multiple"}
                                      ]
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("Submission not found"));
        }

        @Test
        @DisplayName("already submitted → 409")
        void alreadySubmitted() throws Exception {
            when(submissionService.submitAnswers(eq(SUBMISSION_ID), eq(USER_ID), any()))
                    .thenThrow(new IllegalSubmissionStateException("Submission already completed"));

            mockMvc.perform(put("/submissions/{id}", SUBMISSION_ID)
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "answers": [
                                        {"question_id": "q1", "type": "multiple"}
                                      ]
                                    }
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("Submission already completed"));
        }

        @Test
        @DisplayName("no token → 403")
        void unauthorized() throws Exception {
            mockMvc.perform(put("/submissions/{id}", SUBMISSION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "answers": [{"question_id": "q1", "type": "multiple"}]
                                    }
                                    """))
                    .andExpect(status().isForbidden());
        }
    }

    // ── GET /submissions/{id} ──────────────────────────────────────

    @Nested
    @DisplayName("GET /submissions/{id}")
    class GetSubmission {

        @Test
        @DisplayName("authenticated → 200 with feedback")
        void success() throws Exception {
            when(submissionService.getSubmission(SUBMISSION_ID, USER_ID))
                    .thenReturn(buildFeedback());

            mockMvc.perform(get("/submissions/{id}", SUBMISSION_ID).with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(SUBMISSION_ID.toString()))
                    .andExpect(jsonPath("$.user_id").value(USER_ID.toString()));
        }
    }
}
