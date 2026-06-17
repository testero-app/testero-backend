package app.testero.security;

import app.testero.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final long LIMITED_TOKEN_EXPIRE_MILLIS = 15 * 60_000L; // 15 minutes

    private final SecretKey key;
    private final long expireMillis;

    public JwtService(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        this.expireMillis = props.expireHours() * 3600_000L;
    }

    public String generateToken(UUID userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    public String generateLimitedToken(UUID userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("purpose", "set-password")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + LIMITED_TOKEN_EXPIRE_MILLIS))
                .signWith(key)
                .compact();
    }

    public UserPrincipal parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String purpose = claims.get("purpose", String.class);
            return new UserPrincipal(
                    UUID.fromString(claims.getSubject()),
                    claims.get("username", String.class),
                    purpose
            );
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("Token expired");
        } catch (JwtException e) {
            throw new JwtAuthenticationException("Invalid token");
        }
    }

    public static class JwtAuthenticationException extends RuntimeException {
        public JwtAuthenticationException(String message) {
            super(message);
        }
    }
}
