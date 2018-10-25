package pl.poznan.put.clustering.partitional;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class KMedoidsTest {
  private static final double EPSILON = 1.0e-3;

  private static final List<String> NAMES =
      Arrays.asList("Boston", "NY", "DC", "Miami", "Chicago", "Seattle", "SF", "LA", "Denver");

  // @formatter:off
  private static final double[][] DISTANCE_MATRIX = {
    new double[] {0, 206, 429, 1504, 963, 2976, 3095, 2979, 1949},
    new double[] {206, 0, 233, 1308, 802, 2915, 2934, 2786, 1771},
    new double[] {429, 233, 0, 1075, 671, 2684, 2799, 2631, 1616},
    new double[] {1504, 1308, 1075, 0, 1329, 3273, 3053, 2687, 2037},
    new double[] {963, 802, 671, 1329, 0, 2013, 2142, 2054, 996},
    new double[] {2976, 2915, 2684, 3273, 2013, 0, 808, 1131, 1307},
    new double[] {3095, 2934, 2799, 3053, 2142, 808, 0, 379, 1235},
    new double[] {2979, 2786, 2631, 2687, 2054, 1131, 379, 0, 1059},
    new double[] {1949, 1771, 1616, 2037, 996, 1307, 1235, 1059, 0}
  };
  // @formatter:on

  @Test
  public final void testMedoidsWithK2() {
    final PrototypeBasedClusterer kmedoids = new KMedoids();
    final ScoringFunction scoringFunction = PAM.getInstance();
    final ClusterPrototypes initialPrototypes = ClusterPrototypes.initializeLinearly(2);
    final ScoredClusteringResult clusteringResult =
        kmedoids.findPrototypes(KMedoidsTest.DISTANCE_MATRIX, scoringFunction, initialPrototypes);
    final ClusterPrototypes prototypes = clusteringResult.getPrototypes();

    final List<Integer> prototypesIndices = new ArrayList<>(prototypes.getPrototypesIndices());
    Collections.sort(prototypesIndices);

    assertEquals(2, (int) prototypesIndices.get(0));
    assertEquals(6, (int) prototypesIndices.get(1));
    assertEquals(-4830.0, clusteringResult.getScore(), KMedoidsTest.EPSILON);
    assertEquals(6.293843727, clusteringResult.getSilhouette(), KMedoidsTest.EPSILON);
  }

  @Test
  public final void testMedoidsWithK3() {
    final PrototypeBasedClusterer kmedoids = new KMedoids();
    final ScoringFunction scoringFunction = PAM.getInstance();
    final ClusterPrototypes initialPrototypes = ClusterPrototypes.initializeLinearly(3);
    final ScoredClusteringResult clusteringResult =
        kmedoids.findPrototypes(KMedoidsTest.DISTANCE_MATRIX, scoringFunction, initialPrototypes);
    final ClusterPrototypes prototypes = clusteringResult.getPrototypes();

    final List<Integer> prototypesIndices = new ArrayList<>(prototypes.getPrototypesIndices());
    Collections.sort(prototypesIndices);

    assertEquals(2, (int) prototypesIndices.get(0));
    assertEquals(6, (int) prototypesIndices.get(1));
    assertEquals(8, (int) prototypesIndices.get(2));
    assertEquals(-3595.0, clusteringResult.getScore(), KMedoidsTest.EPSILON);
    assertEquals(4.537467647, clusteringResult.getSilhouette(), KMedoidsTest.EPSILON);
  }
}
