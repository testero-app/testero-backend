package app.testero.security;

import java.util.UUID;

public record UserPrincipal(UUID userId, String username) {}
