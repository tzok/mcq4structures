package pl.poznan.put.comparison;

/**
 * Exception thrown when there was an invalid attempt to compare structures.
 * 
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public class IncomparableStructuresException extends Exception {
    private static final long serialVersionUID = 1L;

    public IncomparableStructuresException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncomparableStructuresException(String message) {
        super(message);
    }
}
