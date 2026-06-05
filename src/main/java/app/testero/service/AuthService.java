package app.testero.service;

import app.testero.dto.LoginRequest;
import app.testero.dto.LoginResponse;
import app.testero.entity.AppUser;
import app.testero.entity.StudentProfile;
import app.testero.repository.AppUserRepository;
import app.testero.repository.StudentProfileRepository;
import app.testero.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        AppUser user = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException());

        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
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

        return new LoginResponse(token, userInfo);
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid credentials");
        }
    }
}
