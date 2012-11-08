package pl.poznan.put.cs.bioserver.comparison;

/**
 * Exception thrown when there was an invalid attempt to compare structures.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
@SuppressWarnings("serial")
public class IncomparableStructuresException extends Exception {
    @SuppressWarnings("javadoc")
    public IncomparableStructuresException(String message) {
        super(message);
    }

    @SuppressWarnings("javadoc")
    public IncomparableStructuresException(Throwable cause) {
        super(cause);
    }
}
