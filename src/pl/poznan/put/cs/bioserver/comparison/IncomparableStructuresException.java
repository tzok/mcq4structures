package pl.poznan.put.cs.bioserver.comparison;

/**
 * Exception thrown when there was an invalid attempt to compare structures.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class IncomparableStructuresException extends Exception {
    private static final long serialVersionUID = 1L;

    IncomparableStructuresException(Throwable cause) {
        super(cause);
    }
}
