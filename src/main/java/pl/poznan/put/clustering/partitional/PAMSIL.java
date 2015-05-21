package pl.poznan.put.clustering.partitional;

public class PAMSIL implements ScoringFunction {
    private static final PAMSIL INSTANCE = new PAMSIL();

    public static PAMSIL getInstance() {
        return PAMSIL.INSTANCE;
    }

    @Override
    public double score(ClusterPrototypes medoids, double[][] matrix) {
        ClusterAssignment assignment = ClusterAssignment.fromPrototypes(medoids, matrix);
        double result = 0;

        for (int prototype : assignment.getPrototypes()) {
            if (assignment.getAssignedCount(prototype) <= 1) {
                continue;
            }

            for (int j : assignment.getAssignedTo(prototype)) {
                double aj = 0;
                for (int i : assignment.getPrototypes()) {
                    aj += matrix[j][i];
                }
                aj /= assignment.getPrototypes().size();

                double bj = Double.POSITIVE_INFINITY;

                for (int inner : assignment.getPrototypes()) {
                    if (inner == prototype) {
                        continue;
                    }

                    double bjk = 0;
                    for (int k : assignment.getAssignedTo(inner)) {
                        bjk += matrix[j][k];
                    }
                    bjk /= assignment.getAssignedCount(inner);

                    if (bjk < bj) {
                        bj = bjk;
                    }
                }

                result += (bj - aj) / Math.max(aj, bj);
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return "PAMSIL";
    }

    private PAMSIL() {
    }
}