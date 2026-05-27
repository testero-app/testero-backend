package app.testero.service;

import app.testero.dto.LoginRequest;
import app.testero.dto.LoginResponse;
import app.testero.entity.Student;
import app.testero.repository.StudentRepository;
import app.testero.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(StudentRepository studentRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Student student = studentRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException());

        if (student.getPasswordHash() == null ||
                !passwordEncoder.matches(request.password(), student.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(
                student.getId(),
                student.getUsername(),
                student.getClassId()
        );

        String className = student.getStudentClass() != null
                ? student.getStudentClass().getName()
                : "";

        var studentInfo = new LoginResponse.StudentInfo(
                student.getId().toString(),
                student.getName(),
                student.getUsername(),
                className
        );

        return new LoginResponse(token, studentInfo);
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid credentials");
        }
    }
}
