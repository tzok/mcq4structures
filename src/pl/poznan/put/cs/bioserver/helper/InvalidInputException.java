package pl.poznan.put.cs.bioserver.helper;

public class InvalidInputException extends Exception {
    private static final long serialVersionUID = 1873169469355235471L;

    public InvalidInputException(String message) {
        super(message);
    }
}
