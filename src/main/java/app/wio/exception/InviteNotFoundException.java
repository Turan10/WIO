package app.wio.exception;

public class InviteNotFoundException extends RuntimeException {
    public InviteNotFoundException(String message) {
        super(message);
    }
}