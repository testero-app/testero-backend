package app.testero.security;

import java.util.UUID;

public record StudentPrincipal(
        UUID studentId,
        String username,
        UUID classId
) {}
