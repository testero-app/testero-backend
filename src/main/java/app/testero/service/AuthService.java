package app.testero.service;

import app.testero.dto.LoginRequest;
import app.testero.dto.LoginResponse;
import app.testero.entity.user.AppUser;
import app.testero.entity.user.StudentProfile;
import app.testero.repository.AppUserRepository;
import app.testero.repository.StudentProfileRepository;
import app.testero.security.JwtService;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository,
                       StudentProfileRepository studentProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        String identifier = request.username();
        AppUser user = findByIdentifier(identifier)
                .orElseThrow(() -> {
                    log.warn("Login failed: unknown identifier={}", identifier);
                    return new InvalidCredentialsException();
                });

        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed: wrong password for identifier={}", identifier);
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getUsername()
        );

        StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
        String className = (profile != null && profile.getUserClass() != null)
                ? profile.getUserClass().getName()
                : "";

        var userInfo = new LoginResponse.UserInfo(
                user.getId().toString(),
                user.getName(),
                user.getUsername(),
                className
        );

        log.info("Login successful: username={}", user.getUsername());
        return new LoginResponse(token, userInfo);
    }

    private Optional<AppUser> findByIdentifier(String identifier) {
        if (identifier.contains("@")) {
            return appUserRepository.findByEmail(identifier);
        }
        return appUserRepository.findByUsername(identifier);
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid credentials");
        }
    }
}
