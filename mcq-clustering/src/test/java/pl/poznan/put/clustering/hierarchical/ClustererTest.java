package pl.poznan.put.clustering.hierarchical;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ClustererTest {
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
  public final void testHierarchicalComplete() {
    final Clusterer clusterer =
        new Clusterer(ClustererTest.NAMES, ClustererTest.DISTANCE_MATRIX, Linkage.COMPLETE);
    final HierarchicalClustering clustering = clusterer.cluster();
    final List<HierarchicalClusterMerge> merges = clustering.getMerges();
    assertEquals(8, merges.size());

    HierarchicalClusterMerge merge = merges.get(0);
    assertEquals(0, merge.getLeft());
    assertEquals(1, merge.getRight());
    assertEquals(206.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(1);
    assertEquals(4, merge.getLeft());
    assertEquals(5, merge.getRight());
    assertEquals(379.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(2);
    assertEquals(0, merge.getLeft());
    assertEquals(5, merge.getRight());
    assertEquals(429.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(3);
    assertEquals(1, merge.getLeft());
    assertEquals(5, merge.getRight());
    assertEquals(963.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(4);
    assertEquals(1, merge.getLeft());
    assertEquals(3, merge.getRight());
    assertEquals(1131.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(5);
    assertEquals(1, merge.getLeft());
    assertEquals(3, merge.getRight());
    assertEquals(1307.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(6);
    assertEquals(0, merge.getLeft());
    assertEquals(1, merge.getRight());
    assertEquals(1504.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(7);
    assertEquals(0, merge.getLeft());
    assertEquals(1, merge.getRight());
    assertEquals(3273.0, merge.getDistance(), ClustererTest.EPSILON);
  }

  @Test
  public final void testHierarchicalSingle() {
    final Clusterer clusterer =
        new Clusterer(ClustererTest.NAMES, ClustererTest.DISTANCE_MATRIX, Linkage.SINGLE);
    final HierarchicalClustering clustering = clusterer.cluster();
    final List<HierarchicalClusterMerge> merges = clustering.getMerges();
    assertEquals(8, merges.size());

    HierarchicalClusterMerge merge = merges.get(0);
    assertEquals(0, merge.getLeft());
    assertEquals(1, merge.getRight());
    assertEquals(206.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(1);
    assertEquals(0, merge.getLeft());
    assertEquals(7, merge.getRight());
    assertEquals(233.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(2);
    assertEquals(3, merge.getLeft());
    assertEquals(4, merge.getRight());
    assertEquals(379.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(3);
    assertEquals(1, merge.getLeft());
    assertEquals(4, merge.getRight());
    assertEquals(671.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(4);
    assertEquals(1, merge.getLeft());
    assertEquals(3, merge.getRight());
    assertEquals(808.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(5);
    assertEquals(1, merge.getLeft());
    assertEquals(2, merge.getRight());
    assertEquals(996.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(6);
    assertEquals(1, merge.getLeft());
    assertEquals(2, merge.getRight());
    assertEquals(1059.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(7);
    assertEquals(0, merge.getLeft());
    assertEquals(1, merge.getRight());
    assertEquals(1075.0, merge.getDistance(), ClustererTest.EPSILON);
  }

  @Test
  public final void testHierarchicalAverage() {
    final Clusterer clusterer =
        new Clusterer(ClustererTest.NAMES, ClustererTest.DISTANCE_MATRIX, Linkage.AVERAGE);
    final HierarchicalClustering clustering = clusterer.cluster();
    final List<HierarchicalClusterMerge> merges = clustering.getMerges();
    assertEquals(8, merges.size());

    HierarchicalClusterMerge merge = merges.get(0);
    assertEquals(0, merge.getLeft());
    assertEquals(1, merge.getRight());
    assertEquals(206.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(1);
    assertEquals(0, merge.getLeft());
    assertEquals(7, merge.getRight());
    assertEquals(331.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(2);
    assertEquals(3, merge.getLeft());
    assertEquals(4, merge.getRight());
    assertEquals(379.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(3);
    assertEquals(1, merge.getLeft());
    assertEquals(4, merge.getRight());
    assertEquals(812.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(4);
    assertEquals(1, merge.getLeft());
    assertEquals(3, merge.getRight());
    assertEquals(969.5, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(5);
    assertEquals(1, merge.getLeft());
    assertEquals(3, merge.getRight());
    assertEquals(1200.0 + (1.0 / 3.0), merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(6);
    assertEquals(0, merge.getLeft());
    assertEquals(1, merge.getRight());
    assertEquals(1304.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(7);
    assertEquals(0, merge.getLeft());
    assertEquals(1, merge.getRight());
    assertEquals(2469.5, merge.getDistance(), ClustererTest.EPSILON);
  }
}
