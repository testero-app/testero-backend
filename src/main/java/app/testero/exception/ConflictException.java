package app.testero.exception;

/** The request conflicts with current state — e.g. a duplicate that must be unique. Maps to 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
