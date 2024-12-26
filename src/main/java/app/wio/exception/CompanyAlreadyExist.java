package app.wio.exception;

public class CompanyAlreadyExist extends RuntimeException {
    public CompanyAlreadyExist(String message) {
        super(message);
    }
}
