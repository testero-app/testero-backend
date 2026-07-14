package app.testero.exception;

/**
 * The caller is authenticated but not allowed to perform this action.
 *
 * <p>Distinct from an authentication failure: the identity is known and valid, it simply
 * lacks the required role. Maps to 403, never to 401.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
