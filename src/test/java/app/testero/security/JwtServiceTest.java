package app.testero.security;

import app.testero.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private static final UUID USER_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");
    private static final String USERNAME = "mario.rossi";

    @BeforeEach
    void setUp() {
        var props = new JwtProperties(
                "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm",
                "HS256",
                1
        );
        jwtService = new JwtService(props);
    }

    @Nested
    @DisplayName("generateToken + parseToken")
    class NormalToken {

        @Test
        @DisplayName("round-trips userId and username")
        void roundTrip() {
            String token = jwtService.generateToken(USER_ID, USERNAME);
            UserPrincipal principal = jwtService.parseToken(token);

            assertThat(principal.userId()).isEqualTo(USER_ID);
            assertThat(principal.username()).isEqualTo(USERNAME);
            assertThat(principal.purpose()).isNull();
            assertThat(principal.isLimited()).isFalse();
        }
    }

    @Nested
    @DisplayName("generateLimitedToken + parseToken")
    class LimitedToken {

        @Test
        @DisplayName("includes set-password purpose claim")
        void purposeClaim() {
            String token = jwtService.generateLimitedToken(USER_ID, USERNAME);
            UserPrincipal principal = jwtService.parseToken(token);

            assertThat(principal.userId()).isEqualTo(USER_ID);
            assertThat(principal.username()).isEqualTo(USERNAME);
            assertThat(principal.purpose()).isEqualTo("set-password");
            assertThat(principal.isLimited()).isTrue();
        }
    }

    @Nested
    @DisplayName("parseToken — errors")
    class ParseErrors {

        @Test
        @DisplayName("throws on expired token")
        void expired() {
            var shortLivedProps = new JwtProperties(
                    "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm",
                    "HS256",
                    0 // 0 hours = already expired
            );
            var shortLivedService = new JwtService(shortLivedProps);
            String token = shortLivedService.generateToken(USER_ID, USERNAME);

            assertThatThrownBy(() -> jwtService.parseToken(token))
                    .isInstanceOf(JwtService.JwtAuthenticationException.class)
                    .hasMessage("Token expired");
        }

        @Test
        @DisplayName("throws on invalid token")
        void invalid() {
            assertThatThrownBy(() -> jwtService.parseToken("not.a.valid.token"))
                    .isInstanceOf(JwtService.JwtAuthenticationException.class)
                    .hasMessage("Invalid token");
        }
    }
}
