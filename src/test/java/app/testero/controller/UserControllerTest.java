package app.testero.controller;

import app.testero.config.CorsProperties;
import app.testero.config.JwtProperties;
import app.testero.config.SecurityConfig;
import app.testero.dto.ChangePasswordRequest;
import app.testero.dto.NotificationPreferenceDto;
import app.testero.dto.UpdateProfileRequest;
import app.testero.dto.UserProfileResponse;
import app.testero.exception.InvalidPasswordException;
import app.testero.security.JwtService;
import app.testero.security.UserPrincipal;
import app.testero.service.UserService;

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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean UserService userService;
    @MockitoBean JwtService jwtService;

    private static final UUID USER_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");

    private static RequestPostProcessor jwt() {
        var principal = new UserPrincipal(USER_ID, "mario");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        return authentication(auth);
    }

    @Nested
    @DisplayName("GET /users/me")
    class GetProfile {

        @Test
        @DisplayName("authenticated → 200 with profile")
        void success() throws Exception {
            var profile = new UserProfileResponse(
                    USER_ID.toString(), "Mario", "Rossi", "mario",
                    "mario@test.com", "5A", "STUDENT");
            when(userService.getProfile(USER_ID)).thenReturn(profile);

            mockMvc.perform(get("/users/me").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                    .andExpect(jsonPath("$.first_name").value("Mario"))
                    .andExpect(jsonPath("$.last_name").value("Rossi"))
                    .andExpect(jsonPath("$.username").value("mario"))
                    .andExpect(jsonPath("$.email").value("mario@test.com"))
                    .andExpect(jsonPath("$.class_name").value("5A"))
                    .andExpect(jsonPath("$.role").value("STUDENT"));
        }

        @Test
        @DisplayName("no token → 403")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/users/me"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /users/me")
    class UpdateProfile {

        @Test
        @DisplayName("valid request → 200 with updated profile")
        void success() throws Exception {
            var profile = new UserProfileResponse(
                    USER_ID.toString(), "Mario", "Rossi", "mario",
                    "new@test.com", "5A", "STUDENT");
            when(userService.updateProfile(eq(USER_ID), any(UpdateProfileRequest.class)))
                    .thenReturn(profile);

            mockMvc.perform(put("/users/me")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "new@test.com"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("new@test.com"))
                    .andExpect(jsonPath("$.first_name").value("Mario"))
                    .andExpect(jsonPath("$.last_name").value("Rossi"));
        }

        @Test
        @DisplayName("no token → 403")
        void unauthorized() throws Exception {
            mockMvc.perform(put("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "new@test.com"}
                                    """))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /users/me/password")
    class ChangePassword {

        @Test
        @DisplayName("valid request → 204")
        void success() throws Exception {
            doNothing().when(userService).changePassword(eq(USER_ID), any(ChangePasswordRequest.class));

            mockMvc.perform(put("/users/me/password")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "current_password": "OldPass1!",
                                      "new_password": "NewPass1!",
                                      "confirm_password": "NewPass1!"
                                    }
                                    """))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("invalid password → 400 with detail")
        void invalidPassword() throws Exception {
            doThrow(new InvalidPasswordException("Current password is incorrect"))
                    .when(userService).changePassword(eq(USER_ID), any(ChangePasswordRequest.class));

            mockMvc.perform(put("/users/me/password")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "current_password": "wrong",
                                      "new_password": "NewPass1!",
                                      "confirm_password": "NewPass1!"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Current password is incorrect"));
        }

        @Test
        @DisplayName("blank fields → 400")
        void blankFields() throws Exception {
            mockMvc.perform(put("/users/me/password")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "current_password": "",
                                      "new_password": "",
                                      "confirm_password": ""
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").exists());
        }

        @Test
        @DisplayName("no token → 403")
        void unauthorized() throws Exception {
            mockMvc.perform(put("/users/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "current_password": "OldPass1!",
                                      "new_password": "NewPass1!",
                                      "confirm_password": "NewPass1!"
                                    }
                                    """))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /users/me/notifications")
    class GetNotifications {

        @Test
        @DisplayName("authenticated → 200 with default preferences")
        void success() throws Exception {
            when(userService.getNotificationPreferences(USER_ID))
                    .thenReturn(List.of(
                            new NotificationPreferenceDto("EXAM_RESULT", "IN_APP", true),
                            new NotificationPreferenceDto("DEADLINE_REMINDER", "IN_APP", true),
                            new NotificationPreferenceDto("EXAM_RESULT", "EMAIL", false)
                    ));

            mockMvc.perform(get("/users/me/notifications").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].event").value("EXAM_RESULT"))
                    .andExpect(jsonPath("$[0].channel").value("IN_APP"))
                    .andExpect(jsonPath("$[0].enabled").value(true));
        }

        @Test
        @DisplayName("no token → 403")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/users/me/notifications"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /users/me/notifications")
    class UpdateNotifications {

        @Test
        @DisplayName("valid request → 200 with updated preferences")
        void success() throws Exception {
            when(userService.updateNotificationPreferences(eq(USER_ID), any()))
                    .thenReturn(List.of(
                            new NotificationPreferenceDto("EXAM_RESULT", "IN_APP", false),
                            new NotificationPreferenceDto("EXAM_RESULT", "EMAIL", true)
                    ));

            mockMvc.perform(put("/users/me/notifications")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "preferences": [
                                        { "event": "EXAM_RESULT", "channel": "IN_APP", "enabled": false },
                                        { "event": "EXAM_RESULT", "channel": "EMAIL", "enabled": true }
                                      ]
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].enabled").value(false));
        }

        @Test
        @DisplayName("empty preferences → 400")
        void emptyPreferences() throws Exception {
            mockMvc.perform(put("/users/me/notifications")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "preferences": [] }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("no token → 403")
        void unauthorized() throws Exception {
            mockMvc.perform(put("/users/me/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "preferences": [
                                        { "type": "EXAM_RESULT", "enabled": false }
                                      ]
                                    }
                                    """))
                    .andExpect(status().isForbidden());
        }
    }
}
