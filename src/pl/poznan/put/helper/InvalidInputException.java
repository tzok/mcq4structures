package pl.poznan.put.helper;

public class InvalidInputException extends Exception {
    private static final long serialVersionUID = 1873169469355235471L;

    public InvalidInputException(String message) {
        super(message);
    }

    InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

    InvalidInputException(Throwable cause) {
        super(cause);
    }
}
