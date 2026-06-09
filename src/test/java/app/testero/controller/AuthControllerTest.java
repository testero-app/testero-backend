package app.testero.controller;

import app.testero.config.CorsProperties;
import app.testero.config.JwtProperties;
import app.testero.config.SecurityConfig;
import app.testero.dto.LoginRequest;
import app.testero.dto.LoginResponse;
import app.testero.security.JwtService;
import app.testero.service.AuthService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean AuthService authService;
    @MockitoBean JwtService jwtService;

    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        @Test
        @DisplayName("valid credentials → 200 with token and user info")
        void success() throws Exception {
            var userInfo = new LoginResponse.UserInfo("user-id-1", "Mario Rossi", "mario", "5A");
            var loginResponse = new LoginResponse("jwt.token.here", userInfo);
            when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username": "mario", "password": "secret123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt.token.here"))
                    .andExpect(jsonPath("$.user.id").value("user-id-1"))
                    .andExpect(jsonPath("$.user.name").value("Mario Rossi"))
                    .andExpect(jsonPath("$.user.username").value("mario"))
                    .andExpect(jsonPath("$.user.class_name").value("5A"));
        }

        @Test
        @DisplayName("invalid credentials → 401")
        void invalidCredentials() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new AuthService.InvalidCredentialsException());

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username": "mario", "password": "wrong"}
                                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Invalid credentials"));
        }

        @Test
        @DisplayName("blank username → 400")
        void blankUsername() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username": "", "password": "secret123"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").exists());
        }

        @Test
        @DisplayName("blank password → 400")
        void blankPassword() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username": "mario", "password": ""}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").exists());
        }

        @Test
        @DisplayName("no Authorization header required (public endpoint)")
        void publicEndpoint() throws Exception {
            var userInfo = new LoginResponse.UserInfo("id", "Name", "user", "");
            when(authService.login(any(LoginRequest.class)))
                    .thenReturn(new LoginResponse("token", userInfo));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username": "user", "password": "pass"}
                                    """))
                    .andExpect(status().isOk());
        }
    }
}
