package pl.poznan.put.cs.bioserver.comparison;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Structure;

import pl.poznan.put.cs.bioserver.helper.PdbManager;

/**
 * An abstraction of all global comparison measures.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public abstract class GlobalComparison {
    private static final Logger LOGGER = Logger
            .getLogger(GlobalComparison.class);

    private class CompareThread extends Thread {
        private Structure[] structures;
        private double[][] result;
        private int i;
        private int j;
        private IncomparableStructuresException exception;

        public CompareThread(Structure[] structures, double[][] result, int i,
                int j) {
            this.structures = structures;
            this.result = result;
            this.i = i;
            this.j = j;
        }

        @Override
        public void run() {
            try {
                double value = compare(structures[i], structures[j]);
                result[i][j] = value;
                result[j][i] = value;
            } catch (IncomparableStructuresException e) {
                exception = e;
            }
        }

    }

    /**
     * Compare two structures.
     * 
     * @param s1
     *            First structure.
     * @param s2
     *            Second structure.
     * @return Distance between the structures according to some measure.
     * @throws IncomparableStructuresException
     *             If the two structure could not be compared.
     */
    public abstract double compare(Structure s1, Structure s2)
            throws IncomparableStructuresException;

    /**
     * Compare each structures with each other.
     * 
     * @param structures
     *            An array of structures to be compared.
     * @return A distance matrix.
     * @throws IncomparableStructuresException
     *             If any two structures were not comparable.
     */
    public double[][] compare(Structure[] structures)
            throws IncomparableStructuresException {
        double[][] result = new double[structures.length][];
        for (int i = 0; i < structures.length; ++i) {
            result[i] = new double[structures.length];
        }

        List<CompareThread> list = new ArrayList<>();
        for (int i = 0; i < structures.length; ++i) {
            for (int j = i + 1; j < structures.length; ++j) {
                GlobalComparison.LOGGER.trace("Comparing: "
                        + PdbManager.getStructureName(structures[i]) + " "
                        + PdbManager.getStructureName(structures[j]));
                CompareThread t = new CompareThread(structures, result, i, j);
                list.add(t);
                t.start();
            }
        }
        for (CompareThread t : list) {
            try {
                t.join();
            } catch (InterruptedException e) {
                GlobalComparison.LOGGER.error("There was a problem with "
                        + "computation threads synchronization", e);
            }
            if (t.exception != null) {
                throw t.exception;
            }
        }
        return result;
    }
}
