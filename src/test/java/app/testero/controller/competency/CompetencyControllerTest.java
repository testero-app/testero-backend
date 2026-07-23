package app.testero.controller.competency;

import app.testero.config.CorsProperties;
import app.testero.config.JwtProperties;
import app.testero.config.SecurityConfig;
import app.testero.dto.competency.CompetencyResponse;
import app.testero.dto.competency.CompetencyResponse.SubjectMastery;
import app.testero.dto.competency.CompetencyResponse.TopicMastery;
import app.testero.security.JwtService;
import app.testero.security.UserPrincipal;
import app.testero.service.competency.CompetencyService;
import org.junit.jupiter.api.DisplayName;
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

@WebMvcTest(CompetencyController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
@ActiveProfiles("test")
class CompetencyControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean CompetencyService competencyService;
    @MockitoBean JwtService jwtService;

    private static final UUID USER_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");

    private static RequestPostProcessor jwt() {
        var principal = new UserPrincipal(USER_ID, "mario");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        return authentication(auth);
    }

    @Test
    @DisplayName("GET /competencies authenticated returns 200 with mastery data")
    void success() throws Exception {
        var subjects = List.of(
                new SubjectMastery("s1", "Variabili", 80),
                new SubjectMastery("s2", "Cicli", 60)
        );
        var topic = new TopicMastery("t1", "Fondamenti Python I", 70, List.of(), subjects);
        when(competencyService.calculateMastery(USER_ID, null, null))
                .thenReturn(new CompetencyResponse(List.of(topic)));

        mockMvc.perform(get("/competencies").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topics.length()").value(1))
                .andExpect(jsonPath("$.topics[0].id").value("t1"))
                .andExpect(jsonPath("$.topics[0].title").value("Fondamenti Python I"))
                .andExpect(jsonPath("$.topics[0].mastery").value(70))
                .andExpect(jsonPath("$.topics[0].subjects.length()").value(2))
                .andExpect(jsonPath("$.topics[0].subjects[0].label").value("Variabili"))
                .andExpect(jsonPath("$.topics[0].subjects[0].mastery").value(80));
    }

    @Test
    @DisplayName("GET /competencies empty mastery returns 200 with empty topics")
    void empty() throws Exception {
        when(competencyService.calculateMastery(USER_ID, null, null))
                .thenReturn(new CompetencyResponse(List.of()));

        mockMvc.perform(get("/competencies").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topics.length()").value(0));
    }

    @Test
    @DisplayName("GET /competencies no token returns 403")
    void unauthorized() throws Exception {
        mockMvc.perform(get("/competencies"))
                .andExpect(status().isForbidden());
    }
}
