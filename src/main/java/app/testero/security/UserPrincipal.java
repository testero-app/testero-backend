package app.testero.security;

import java.util.UUID;

public record UserPrincipal(UUID userId, String username, String purpose) {

    public UserPrincipal(UUID userId, String username) {
        this(userId, username, null);
    }

    public boolean isLimited() {
        return "set-password".equals(purpose);
    }
}
