package pl.poznan.put.cs.bioserver.visualisation;

import org.biojava.bio.structure.jama.EigenvalueDecomposition;
import org.biojava.bio.structure.jama.Matrix;

/**
 * A utility class implementing a Multidimensional Scaling method.
 * 
 * @author tzok
 */
public class MDS {
    /**
     * Calculate the Multidimensional Scaling. It gets a distance matrix and
     * creates a map of points in N-dimensions whose mutual distances correspond
     * to the given input matrix.
     * 
     * @param distance
     *            A distance matrix, NxN.
     * @param dimensions
     *            Desired number of dimensions, K.
     * @return A matrix NxK, where for each row there are K coordinates.
     */
    public static double[][] multidimensionalScaling(double[][] distance,
            int dimensions) {
        /*
         * sanity check (symmetric, square matrix as input)
         */
        String msg = "Distance matrix is not symmetrical!";
        for (int i = 0; i < distance.length; ++i) {
            if (distance[i].length != distance.length) {
                throw new IllegalArgumentException(msg);
            }
            for (int j = 0; j < distance[i].length; ++j) {
                if (distance[i][j] != distance[j][i]) {
                    throw new IllegalArgumentException(msg);
                }
            }
        }

        /*
         * calculate D as distance_ij^2
         */
        double[][] d = new double[distance.length][];
        for (int i = 0; i < distance.length; ++i) {
            d[i] = new double[distance.length];
            for (int j = 0; j < distance.length; ++j) {
                d[i][j] = distance[i][j] * distance[i][j];
            }
        }

        /*
         * calculate mean for each row, column and whole matrix
         */
        double[] meanRow = new double[distance.length];
        double[] meanColumn = new double[distance.length];
        double meanMatrix = 0;
        for (int i = 0; i < distance.length; ++i) {
            for (int j = 0; j < distance.length; ++j) {
                meanRow[i] += d[i][j];
                meanColumn[j] += d[i][j];
                meanMatrix += d[i][j];
            }
        }
        for (int i = 0; i < distance.length; ++i) {
            meanRow[i] /= distance.length;
            meanColumn[i] /= distance.length;
        }
        meanMatrix /= distance.length * distance.length;

        /*
         * calculate B: b_ij = -1/2 * (d_ij - meanRow[i] - meanColumn[j] +
         * meanMatrix)
         */
        double[][] B = new double[distance.length][];
        for (int i = 0; i < distance.length; ++i) {
            B[i] = new double[distance.length];
            for (int j = 0; j < distance.length; ++j) {
                B[i][j] = -0.5
                        * (d[i][j] - meanRow[i] - meanColumn[j] + meanMatrix);
            }
        }

        /*
         * decompose B = VDV^T (or else called KLK^T)
         */
        EigenvalueDecomposition evd = new EigenvalueDecomposition(new Matrix(B));

        /*
         * find maxima in L
         */
        double[][] L = evd.getD().getArrayCopy();
        int[] maxima = new int[dimensions];
        for (int i = 0; i < dimensions; ++i) {
            int max = 0;
            for (int j = 1; j < L.length; ++j) {
                if (L[j][j] > L[max][max]) {
                    max = j;
                }
            }
            // if L[max][max] < 0, then it's impossible to visualise
            if (L[max][max] < 0) {
                return null;
            }
            maxima[i] = max;
            L[max][max] = Double.NEGATIVE_INFINITY;
        }

        /*
         * get sqrt() from those maxima in L
         */
        L = evd.getD().getArrayCopy();
        for (int i = 0; i < dimensions; ++i) {
            L[maxima[i]][maxima[i]] = Math.sqrt(L[maxima[i]][maxima[i]]);
        }

        /*
         * calculate X coordinates for visualisation
         */
        double[][] X = new double[distance.length][];
        double[][] K = evd.getV().getArray();
        for (int i = 0; i < distance.length; ++i) {
            X[i] = new double[dimensions];
            for (int j = 0; j < dimensions; ++j) {
                X[i][j] = K[i][maxima[j]] * L[maxima[j]][maxima[j]];
            }
        }
        return X;
    }

    private MDS() {
    }
}
