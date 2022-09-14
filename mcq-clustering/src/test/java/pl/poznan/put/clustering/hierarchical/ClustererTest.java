package pl.poznan.put.clustering.hierarchical;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ClustererTest {
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
  public final void testHierarchicalComplete() {
    final Clusterer clusterer =
        new Clusterer(ClustererTest.NAMES, ClustererTest.DISTANCE_MATRIX, Linkage.COMPLETE);
    final HierarchicalClustering clustering = clusterer.cluster();
    final List<HierarchicalClusterMerge> merges = clustering.getMerges();
    assertThat(merges.size(), is(8));

    HierarchicalClusterMerge merge = merges.get(0);
    assertThat(merge.getLeft(), is(0));
    assertThat(merge.getRight(), is(1));
    assertEquals(206.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(1);
    assertThat(merge.getLeft(), is(4));
    assertThat(merge.getRight(), is(5));
    assertEquals(379.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(2);
    assertThat(merge.getLeft(), is(0));
    assertThat(merge.getRight(), is(5));
    assertEquals(429.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(3);
    assertThat(merge.getLeft(), is(1));
    assertThat(merge.getRight(), is(5));
    assertEquals(963.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(4);
    assertThat(merge.getLeft(), is(1));
    assertThat(merge.getRight(), is(3));
    assertEquals(1131.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(5);
    assertThat(merge.getLeft(), is(1));
    assertThat(merge.getRight(), is(3));
    assertEquals(1307.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(6);
    assertThat(merge.getLeft(), is(0));
    assertThat(merge.getRight(), is(1));
    assertEquals(1504.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(7);
    assertThat(merge.getLeft(), is(0));
    assertThat(merge.getRight(), is(1));
    assertEquals(3273.0, merge.getDistance(), ClustererTest.EPSILON);
  }

  @Test
  public final void testHierarchicalSingle() {
    final Clusterer clusterer =
        new Clusterer(ClustererTest.NAMES, ClustererTest.DISTANCE_MATRIX, Linkage.SINGLE);
    final HierarchicalClustering clustering = clusterer.cluster();
    final List<HierarchicalClusterMerge> merges = clustering.getMerges();
    assertThat(merges.size(), is(8));

    HierarchicalClusterMerge merge = merges.get(0);
    assertThat(merge.getLeft(), is(0));
    assertThat(merge.getRight(), is(1));
    assertEquals(206.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(1);
    assertThat(merge.getLeft(), is(0));
    assertThat(merge.getRight(), is(7));
    assertEquals(233.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(2);
    assertThat(merge.getLeft(), is(3));
    assertThat(merge.getRight(), is(4));
    assertEquals(379.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(3);
    assertThat(merge.getLeft(), is(1));
    assertThat(merge.getRight(), is(4));
    assertEquals(671.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(4);
    assertThat(merge.getLeft(), is(1));
    assertThat(merge.getRight(), is(3));
    assertEquals(808.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(5);
    assertThat(merge.getLeft(), is(1));
    assertThat(merge.getRight(), is(2));
    assertEquals(996.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(6);
    assertThat(merge.getLeft(), is(1));
    assertThat(merge.getRight(), is(2));
    assertEquals(1059.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(7);
    assertThat(merge.getLeft(), is(0));
    assertThat(merge.getRight(), is(1));
    assertEquals(1075.0, merge.getDistance(), ClustererTest.EPSILON);
  }

  @Test
  public final void testHierarchicalAverage() {
    final Clusterer clusterer =
        new Clusterer(ClustererTest.NAMES, ClustererTest.DISTANCE_MATRIX, Linkage.AVERAGE);
    final HierarchicalClustering clustering = clusterer.cluster();
    final List<HierarchicalClusterMerge> merges = clustering.getMerges();
    assertThat(merges.size(), is(8));

    HierarchicalClusterMerge merge = merges.get(0);
    assertThat(merge.getLeft(), is(0));
    assertThat(merge.getRight(), is(1));
    assertEquals(206.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(1);
    assertThat(merge.getLeft(), is(0));
    assertThat(merge.getRight(), is(7));
    assertEquals(331.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(2);
    assertThat(merge.getLeft(), is(3));
    assertThat(merge.getRight(), is(4));
    assertEquals(379.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(3);
    assertThat(merge.getLeft(), is(1));
    assertThat(merge.getRight(), is(4));
    assertEquals(812.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(4);
    assertThat(merge.getLeft(), is(1));
    assertThat(merge.getRight(), is(3));
    assertEquals(969.5, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(5);
    assertThat(merge.getLeft(), is(1));
    assertThat(merge.getRight(), is(3));
    assertEquals(1200.0 + (1.0 / 3.0), merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(6);
    assertThat(merge.getLeft(), is(0));
    assertThat(merge.getRight(), is(1));
    assertEquals(1304.0, merge.getDistance(), ClustererTest.EPSILON);

    merge = merges.get(7);
    assertThat(merge.getLeft(), is(0));
    assertThat(merge.getRight(), is(1));
    assertEquals(2469.5, merge.getDistance(), ClustererTest.EPSILON);
  }
}
