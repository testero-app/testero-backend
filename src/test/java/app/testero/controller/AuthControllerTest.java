package app.testero.controller;

import app.testero.config.CorsProperties;
import app.testero.config.JwtProperties;
import app.testero.config.SecurityConfig;
import app.testero.dto.LoginRequest;
import app.testero.dto.LoginResponse;
import app.testero.dto.SetPasswordRequest;
import app.testero.exception.InvalidPasswordException;
import app.testero.security.JwtService;
import app.testero.security.UserPrincipal;
import app.testero.service.AuthService;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean AuthService authService;
    @MockitoBean JwtService jwtService;

    private static final UUID USER_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");

    private static RequestPostProcessor limitedJwt() {
        var principal = new UserPrincipal(USER_ID, "mario", "set-password");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        return authentication(auth);
    }

    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        @Test
        @DisplayName("valid credentials → 200 with token and user info")
        void success() throws Exception {
            var userInfo = new LoginResponse.UserInfo("user-id-1", "Mario Rossi", "mario", "5A");
            var loginResponse = new LoginResponse("jwt.token.here", userInfo, false, false);
            when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username": "mario", "password": "secret123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("X-Request-Id"))
                    .andExpect(jsonPath("$.token").value("jwt.token.here"))
                    .andExpect(jsonPath("$.user.id").value("user-id-1"))
                    .andExpect(jsonPath("$.user.name").value("Mario Rossi"))
                    .andExpect(jsonPath("$.user.username").value("mario"))
                    .andExpect(jsonPath("$.user.class_name").value("5A"))
                    .andExpect(jsonPath("$.must_change_password").value(false))
                    .andExpect(jsonPath("$.password_expired").value(false));
        }

        @Test
        @DisplayName("must change password → 200 with flags")
        void mustChangePassword() throws Exception {
            var userInfo = new LoginResponse.UserInfo("user-id-1", "Mario Rossi", "mario", "5A");
            var loginResponse = new LoginResponse("limited.token", userInfo, true, false);
            when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username": "mario", "password": "secret123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.must_change_password").value(true))
                    .andExpect(jsonPath("$.password_expired").value(false));
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
                    .thenReturn(new LoginResponse("token", userInfo, false, false));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username": "user", "password": "pass"}
                                    """))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /auth/set-password")
    class SetPasswordEndpoint {

        @Test
        @DisplayName("valid request with limited token → 200")
        void success() throws Exception {
            var userInfo = new LoginResponse.UserInfo(USER_ID.toString(), "Mario", "mario", "");
            var response = new LoginResponse("full.token", userInfo, false, false);
            when(authService.setPassword(eq(USER_ID), any(SetPasswordRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/auth/set-password")
                            .with(limitedJwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"new_password": "NewPass1", "confirm_password": "NewPass1"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("full.token"))
                    .andExpect(jsonPath("$.must_change_password").value(false));
        }

        @Test
        @DisplayName("blank new_password → 400")
        void blankPassword() throws Exception {
            mockMvc.perform(post("/auth/set-password")
                            .with(limitedJwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"new_password": "", "confirm_password": "NewPass1"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("no token → 403")
        void noToken() throws Exception {
            mockMvc.perform(post("/auth/set-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"new_password": "NewPass1", "confirm_password": "NewPass1"}
                                    """))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("weak password → 400")
        void weakPassword() throws Exception {
            when(authService.setPassword(eq(USER_ID), any(SetPasswordRequest.class)))
                    .thenThrow(new InvalidPasswordException(
                            "Password must be at least 8 characters with 1 uppercase letter and 1 number"));

            mockMvc.perform(post("/auth/set-password")
                            .with(limitedJwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"new_password": "weak", "confirm_password": "weak"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").exists());
        }
    }
}
