package app.testero.controller;

import app.testero.config.CorsProperties;
import app.testero.config.JwtProperties;
import app.testero.config.SecurityConfig;
import app.testero.dto.TopicListResponse;
import app.testero.dto.TopicListResponse.ChapterItem;
import app.testero.dto.TopicListResponse.QuestionCounts;
import app.testero.dto.TopicListResponse.TopicItem;
import app.testero.security.JwtService;
import app.testero.security.UserPrincipal;
import app.testero.service.TopicService;

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
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TopicController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
@ActiveProfiles("test")
class TopicControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean TopicService topicService;
    @MockitoBean JwtService jwtService;

    private static final UUID USER_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");

    private static RequestPostProcessor jwt() {
        var principal = new UserPrincipal(USER_ID, "mario");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        return authentication(auth);
    }

    @Nested
    @DisplayName("GET /topics")
    class GetTopics {

        @Test
        @DisplayName("authenticated → 200 with topic list")
        void success() throws Exception {
            var chapters = List.of(
                    new ChapterItem("s1", "Variabili e tipi", new QuestionCounts(8, 5, 1)),
                    new ChapterItem("s2", "Cicli e condizioni", new QuestionCounts(7, 4, 2))
            );
            var topic = new TopicItem(
                    "t1", "Fondamenti Python I", "Py",
                    "Variabili, tipi, operatori...", true,
                    chapters, 2, 27
            );
            when(topicService.getTopics())
                    .thenReturn(new TopicListResponse(List.of(topic)));

            mockMvc.perform(get("/topics").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.topics.length()").value(1))
                    .andExpect(jsonPath("$.topics[0].title").value("Fondamenti Python I"))
                    .andExpect(jsonPath("$.topics[0].abbreviation").value("Py"))
                    .andExpect(jsonPath("$.topics[0].total_chapters").value(2))
                    .andExpect(jsonPath("$.topics[0].total_questions").value(27))
                    .andExpect(jsonPath("$.topics[0].chapters[0].label").value("Variabili e tipi"))
                    .andExpect(jsonPath("$.topics[0].chapters[0].question_counts.base").value(8))
                    .andExpect(jsonPath("$.topics[0].chapters[0].question_counts.intermediate").value(5))
                    .andExpect(jsonPath("$.topics[0].chapters[0].question_counts.advanced").value(1));
        }

        @Test
        @DisplayName("authenticated, empty → 200 with empty list")
        void empty() throws Exception {
            when(topicService.getTopics())
                    .thenReturn(new TopicListResponse(List.of()));

            mockMvc.perform(get("/topics").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.topics.length()").value(0));
        }

        @Test
        @DisplayName("no token → 403")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/topics"))
                    .andExpect(status().isForbidden());
        }
    }
}
