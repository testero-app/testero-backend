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

    private final SecretKey key;
    private final long expireMillis;

    public JwtService(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        this.expireMillis = props.expireHours() * 3600_000L;
    }

    public String generateToken(UUID studentId, String username, UUID classId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(studentId.toString())
                .claim("username", username)
                .claim("class_id", classId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    public StudentPrincipal parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new StudentPrincipal(
                    UUID.fromString(claims.getSubject()),
                    claims.get("username", String.class),
                    UUID.fromString(claims.get("class_id", String.class))
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
