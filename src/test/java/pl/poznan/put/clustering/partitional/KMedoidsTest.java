package pl.poznan.put.clustering.partitional;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;

public class KMedoidsTest {
  private static final double EPSILON = 1.0e-3;

  private static final List<String> NAMES =
      Arrays.asList("Boston", "NY", "DC", "Miami", "Chicago", "Seattle", "SF", "LA", "Denver");

  // @formatter:off
  private static final double[][] DISTANCE_MATRIX = {
    new double[] {0, 206.0, 429.0, 1504.0, 963.0, 2976.0, 3095.0, 2979.0, 1949.0},
    new double[] {206.0, 0, 233.0, 1308.0, 802.0, 2915.0, 2934.0, 2786.0, 1771.0},
    new double[] {429.0, 233.0, 0, 1075.0, 671.0, 2684.0, 2799.0, 2631.0, 1616.0},
    new double[] {1504.0, 1308.0, 1075.0, 0, 1329.0, 3273.0, 3053.0, 2687.0, 2037.0},
    new double[] {963.0, 802.0, 671.0, 1329.0, 0, 2013.0, 2142.0, 2054.0, 996.0},
    new double[] {2976.0, 2915.0, 2684.0, 3273.0, 2013.0, 0, 808.0, 1131.0, 1307.0},
    new double[] {3095.0, 2934.0, 2799.0, 3053.0, 2142.0, 808.0, 0, 379.0, 1235.0},
    new double[] {2979.0, 2786.0, 2631.0, 2687.0, 2054.0, 1131.0, 379.0, 0, 1059.0},
    new double[] {1949.0, 1771.0, 1616.0, 2037.0, 996.0, 1307.0, 1235.0, 1059.0, 0}
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

    Assert.assertThat(prototypesIndices.get(0), is(2));
    Assert.assertThat(prototypesIndices.get(1), is(6));
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

    Assert.assertThat(prototypesIndices.get(0), is(2));
    Assert.assertThat(prototypesIndices.get(1), is(6));
    Assert.assertThat(prototypesIndices.get(2), is(8));
    assertEquals(-3595.0, clusteringResult.getScore(), KMedoidsTest.EPSILON);
    assertEquals(4.537467647, clusteringResult.getSilhouette(), KMedoidsTest.EPSILON);
  }
}
