package app.testero.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock JwtService jwtService;
    @Mock FilterChain filterChain;

    @InjectMocks JwtAuthFilter jwtAuthFilter;

    private static final UUID USER_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");

    @Test
    @DisplayName("limited token on normal endpoint returns 403 and stops chain")
    void limitedTokenOnNormalEndpoint() throws Exception {
        SecurityContextHolder.clearContext();
        var request = new MockHttpServletRequest("GET", "/assessments");
        request.addHeader("Authorization", "Bearer limited.token");
        var response = new MockHttpServletResponse();

        when(jwtService.parseToken("limited.token"))
                .thenReturn(new UserPrincipal(USER_ID, "mario", "set-password"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(response.getContentAsString()).contains("Password change required");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("limited token on /auth/set-password passes through")
    void limitedTokenOnSetPasswordEndpoint() throws Exception {
        SecurityContextHolder.clearContext();
        var request = new MockHttpServletRequest("POST", "/auth/set-password");
        request.addHeader("Authorization", "Bearer limited.token");
        var response = new MockHttpServletResponse();

        when(jwtService.parseToken("limited.token"))
                .thenReturn(new UserPrincipal(USER_ID, "mario", "set-password"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("normal token passes through to any endpoint")
    void normalTokenPassesThrough() throws Exception {
        SecurityContextHolder.clearContext();
        var request = new MockHttpServletRequest("GET", "/assessments");
        request.addHeader("Authorization", "Bearer normal.token");
        var response = new MockHttpServletResponse();

        when(jwtService.parseToken("normal.token"))
                .thenReturn(new UserPrincipal(USER_ID, "mario"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("no Authorization header continues without authentication")
    void noHeader() throws Exception {
        SecurityContextHolder.clearContext();
        var request = new MockHttpServletRequest("GET", "/api/assessments");
        var response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
