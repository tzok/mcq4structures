package pl.poznan.put.clustering.partitional;

public final class PAMSIL implements ScoringFunction {
  private static final PAMSIL SUM_INSTANCE = new PAMSIL();

  private PAMSIL() {
    super();
    // empty constructor
  }

  public static PAMSIL getInstance() {
    return PAMSIL.SUM_INSTANCE;
  }

  private static double averageDistanceToCluster(
      final ClusterAssignment assignment, final double[] distanceVector, final int prototype) {
    final double ai =
        assignment.getAssignedTo(prototype).stream()
            .mapToInt(j -> j)
            .mapToDouble(j -> distanceVector[j])
            .sum();
    return ai / assignment.getAssignedTo(prototype).size();
  }

  private static double averageDistanceToNextClosestCluster(
      final ClusterPrototypes prototypes,
      final ClusterAssignment assignment,
      final double[] distanceVector,
      final int myPrototype) {
    double minDi = Double.POSITIVE_INFINITY;

    for (final int otherPrototype : prototypes.getPrototypesIndices()) {
      if (otherPrototype == myPrototype) {
        continue;
      }

      final double di = PAMSIL.averageDistanceToCluster(assignment, distanceVector, otherPrototype);

      if (di < minDi) {
        minDi = di;
      }
    }

    return minDi;
  }

  @Override
  public double score(final ClusterPrototypes prototypes, final double[][] distanceMatrix) {
    final ClusterAssignment assignment =
        ClusterAssignment.fromPrototypes(prototypes, distanceMatrix);
    double result = 0.0;

    for (int i = 0; i < distanceMatrix.length; i++) {
      final int myPrototype = assignment.getPrototype(i);

      if (assignment.getAssignedTo(myPrototype).size() <= 1) {
        continue;
      }

      final double ai = PAMSIL.averageDistanceToCluster(assignment, distanceMatrix[i], myPrototype);
      final double bi =
          PAMSIL.averageDistanceToNextClosestCluster(
              prototypes, assignment, distanceMatrix[i], myPrototype);
      final double si = (bi - ai) / Math.max(ai, bi);
      result += si;
    }

    return result;
  }

  @Override
  public String toString() {
    return "PAMSIL";
  }
}
