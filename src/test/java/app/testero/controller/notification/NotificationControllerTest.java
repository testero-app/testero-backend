package app.testero.controller.notification;

import app.testero.config.CorsProperties;
import app.testero.config.JwtProperties;
import app.testero.config.SecurityConfig;
import app.testero.dto.notification.NotificationItemDto;
import app.testero.security.JwtService;
import app.testero.security.UserPrincipal;
import app.testero.service.notification.NotificationService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean NotificationService notificationService;
    @MockitoBean JwtService jwtService;

    private static final UUID USER_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");
    private static final UUID NOTIF_ID = UUID.fromString("bb000000-0000-0000-0000-000000000001");
    private static final UUID SOURCE_ID = UUID.fromString("dd000000-0000-0000-0000-000000000001");

    private static RequestPostProcessor jwt() {
        var principal = new UserPrincipal(USER_ID, "mario");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        return authentication(auth);
    }

    @Nested
    @DisplayName("GET /notifications/unread")
    class GetUnread {

        @Test
        @DisplayName("returns unread notifications")
        void success() throws Exception {
            var dto = new NotificationItemDto(
                    NOTIF_ID.toString(), "EXAM_RESULT", "Result ready",
                    "Score: 80", false, "2026-06-15T10:00",
                    SOURCE_ID.toString());
            when(notificationService.getUnread(USER_ID)).thenReturn(List.of(dto));

            mockMvc.perform(get("/notifications/unread").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(NOTIF_ID.toString()))
                    .andExpect(jsonPath("$[0].event").value("EXAM_RESULT"))
                    .andExpect(jsonPath("$[0].title").value("Result ready"))
                    .andExpect(jsonPath("$[0].read").value(false))
                    .andExpect(jsonPath("$[0].source_event_id").value(SOURCE_ID.toString()));
        }

        @Test
        @DisplayName("unauthenticated returns 403")
        void unauthenticated() throws Exception {
            mockMvc.perform(get("/notifications/unread"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /notifications/count")
    class GetCount {

        @Test
        @DisplayName("returns unread count")
        void success() throws Exception {
            when(notificationService.getUnreadCount(USER_ID)).thenReturn(5L);

            mockMvc.perform(get("/notifications/count").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(5));
        }
    }

    @Nested
    @DisplayName("PUT /notifications/{id}/read")
    class MarkAsRead {

        @Test
        @DisplayName("marks single notification as read")
        void success() throws Exception {
            mockMvc.perform(put("/notifications/{id}/read", NOTIF_ID).with(jwt()))
                    .andExpect(status().isOk());

            verify(notificationService).markAsRead(NOTIF_ID, USER_ID);
        }
    }

    @Nested
    @DisplayName("PUT /notifications/read-all")
    class MarkAllAsRead {

        @Test
        @DisplayName("marks all notifications as read")
        void success() throws Exception {
            mockMvc.perform(put("/notifications/read-all").with(jwt()))
                    .andExpect(status().isOk());

            verify(notificationService).markAllAsRead(USER_ID);
        }
    }
}
