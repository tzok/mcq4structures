package pl.poznan.put.matching;

public class InvalidQueryException extends RuntimeException {
    private static final long serialVersionUID = -2279294204700049704L;

    public InvalidQueryException(final String s) {
        super(s);
    }

    public InvalidQueryException(final String s, final Throwable throwable) {
        super(s, throwable);
    }
}
