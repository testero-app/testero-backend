package app.testero.controller;

import app.testero.config.CorsProperties;
import app.testero.config.JwtProperties;
import app.testero.config.SecurityConfig;
import app.testero.dto.TrainingStartResponse;
import app.testero.exception.ResourceNotFoundException;
import app.testero.security.JwtService;
import app.testero.security.UserPrincipal;
import app.testero.service.TrainingService;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
@ActiveProfiles("test")
class TrainingControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean TrainingService trainingService;
    @MockitoBean JwtService jwtService;

    private static final UUID USER_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");
    private static final String SUBMISSION_ID = "ff000000-0000-0000-0000-000000000001";
    private static final String SNAPSHOT_ID = "ee000000-0000-0000-0000-000000000001";

    private static RequestPostProcessor jwt() {
        var principal = new UserPrincipal(USER_ID, "mario");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        return authentication(auth);
    }

    @Nested
    @DisplayName("POST /training/start")
    class StartTraining {

        @Test
        @DisplayName("valid request → 200 with submission info")
        void success() throws Exception {
            when(trainingService.startTraining(any(), eq(USER_ID)))
                    .thenReturn(new TrainingStartResponse(SUBMISSION_ID, SNAPSHOT_ID, 23, 15));

            mockMvc.perform(post("/training/start")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "topic_id": "bb000000-0000-0000-0000-000000000001",
                                      "chapter_ids": ["cc000000-0000-0000-0000-000000000001"],
                                      "difficulty": "base",
                                      "question_count": 15,
                                      "timer_enabled": true
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.submission_id").value(SUBMISSION_ID))
                    .andExpect(jsonPath("$.assessment_snapshot_id").value(SNAPSHOT_ID))
                    .andExpect(jsonPath("$.timer_minutes").value(23))
                    .andExpect(jsonPath("$.total_questions").value(15));
        }

        @Test
        @DisplayName("topic not found → 404")
        void topicNotFound() throws Exception {
            when(trainingService.startTraining(any(), eq(USER_ID)))
                    .thenThrow(new ResourceNotFoundException("Topic not found"));

            mockMvc.perform(post("/training/start")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "topic_id": "bb000000-0000-0000-0000-000000000001",
                                      "chapter_ids": ["cc000000-0000-0000-0000-000000000001"],
                                      "difficulty": "base",
                                      "question_count": 15,
                                      "timer_enabled": true
                                    }
                                    """))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("empty chapter_ids → 400")
        void emptyChapters() throws Exception {
            mockMvc.perform(post("/training/start")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "topic_id": "bb000000-0000-0000-0000-000000000001",
                                      "chapter_ids": [],
                                      "difficulty": "base",
                                      "question_count": 15,
                                      "timer_enabled": true
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("no token → 403")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/training/start")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "topic_id": "bb000000-0000-0000-0000-000000000001",
                                      "chapter_ids": ["cc000000-0000-0000-0000-000000000001"],
                                      "difficulty": "base",
                                      "question_count": 15,
                                      "timer_enabled": true
                                    }
                                    """))
                    .andExpect(status().isForbidden());
        }
    }
}
