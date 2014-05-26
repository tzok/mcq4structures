package pl.poznan.put.clustering.partitional;

import java.util.List;

public class PAM implements ScoringFunction {
    private static final PAM INSTANCE = new PAM();

    public static PAM getInstance() {
        return PAM.INSTANCE;
    }

    @Override
    public double score(ClusterPrototypes medoids, double[][] matrix) {
        List<Heap> asHeaps = Heap.fromMatrix(matrix);
        double result = 0;

        for (int i = 0; i < matrix.length; i++) {
            for (int closest : asHeaps.get(i)) {
                if (medoids.isPrototype(closest)) {
                    result += matrix[closest][i];
                    break;
                }
            }
        }

        return -result;
    }

    @Override
    public String toString() {
        return "PAM";
    }

    private PAM() {
    }
}