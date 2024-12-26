package app.wio.exception;

public class LockNotAvailableException extends RuntimeException {
    public LockNotAvailableException(String message) {
        super(message);
    }
}