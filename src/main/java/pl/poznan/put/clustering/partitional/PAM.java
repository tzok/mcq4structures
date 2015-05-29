package pl.poznan.put.clustering.partitional;

import java.util.List;

public class PAM implements ScoringFunction {
    private static final PAM INSTANCE = new PAM();

    public static PAM getInstance() {
        return PAM.INSTANCE;
    }

    @Override
    public double score(ClusterPrototypes prototypes, double[][] distanceMatrix) {
        List<Heap> asHeaps = Heap.fromMatrix(distanceMatrix);
        double result = 0;

        for (int i = 0; i < distanceMatrix.length; i++) {
            for (int closest : asHeaps.get(i)) {
                if (prototypes.isPrototype(closest)) {
                    result += distanceMatrix[closest][i];
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