package pl.poznan.put.clustering.partitional;

public class PAMSIL implements ScoringFunction {
  private static final PAMSIL SUM_INSTANCE = new PAMSIL();

  private PAMSIL() {
    // empty constructor
  }

  public static PAMSIL getInstance() {
    return PAMSIL.SUM_INSTANCE;
  }

  @Override
  public double score(ClusterPrototypes prototypes, double[][] distanceMatrix) {
    ClusterAssignment assignment = ClusterAssignment.fromPrototypes(prototypes, distanceMatrix);
    double result = 0.0;

    for (int i = 0; i < distanceMatrix.length; i++) {
      int myPrototype = assignment.getPrototype(i);

      if (assignment.getAssignedTo(myPrototype).size() <= 1) {
        continue;
      }

      double ai = PAMSIL.averageDistanceToCluster(assignment, distanceMatrix[i], myPrototype);
      double bi =
          PAMSIL.averageDistanceToNextClosestCluster(
              prototypes, assignment, distanceMatrix[i], myPrototype);
      double si = (bi - ai) / Math.max(ai, bi);
      result += si;
    }

    return result;
  }

  private static double averageDistanceToCluster(
      ClusterAssignment assignment, double[] distanceVector, int prototype) {
    double ai = 0.0;
    for (int j : assignment.getAssignedTo(prototype)) {
      ai += distanceVector[j];
    }
    return ai / assignment.getAssignedTo(prototype).size();
  }

  private static double averageDistanceToNextClosestCluster(
      ClusterPrototypes prototypes,
      ClusterAssignment assignment,
      double[] distanceVector,
      int myPrototype) {
    double minDi = Double.POSITIVE_INFINITY;

    for (int otherPrototype : prototypes.getPrototypesIndices()) {
      if (otherPrototype == myPrototype) {
        continue;
      }

      double di = PAMSIL.averageDistanceToCluster(assignment, distanceVector, otherPrototype);

      if (di < minDi) {
        minDi = di;
      }
    }

    return minDi;
  }

  @Override
  public String toString() {
    return "PAMSIL";
  }
}
