package pl.poznan.put.clustering.partitional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ClusterPrototypes {
  private static final Random RANDOM = new Random();
  private final Set<Integer> prototypes;

  public ClusterPrototypes(final Set<Integer> prototypes) {
    super();
    this.prototypes = Collections.unmodifiableSet(prototypes);
  }

  public static void setSeed(final long seed) {
    ClusterPrototypes.RANDOM.setSeed(seed);
  }

  // http://en.wikipedia.org/wiki/K-means%2B%2B#Initialization_algorithm
  public static ClusterPrototypes initializeRandomly(final double[][] matrix, final int k) {
    final Set<Integer> setMedoids = new HashSet<>();
    setMedoids.add(ClusterPrototypes.RANDOM.nextInt(matrix.length));
    final List<Heap> listHeaps = Heap.fromMatrix(matrix);
    assert listHeaps.size() == matrix.length
        : "listHeaps.size() = " + listHeaps.size() + ", matrix.length = " + matrix.length;

    for (int i = 1; i < k; i++) {
      final LinkedHashMap<Integer, Double> mapElementNearest = new LinkedHashMap<>();
      double total = 0;

      for (int j = 0; j < matrix.length; j++) {
        if (setMedoids.contains(j)) {
          continue;
        }

        for (final int nearest : listHeaps.get(j)) {
          if (setMedoids.contains(nearest)) {
            final double distance = matrix[j][nearest];
            total += (distance * distance);
            mapElementNearest.put(j, total);
            break;
          }
        }
      }

      final Collection<Integer> setCandidates = new HashSet<>();
      for (int j = 0; j < matrix.length; j++) {
        setCandidates.add(j);
      }
      setCandidates.removeAll(setMedoids);

      final double randomToken = ClusterPrototypes.RANDOM.nextDouble() * total;

      for (final Integer candidate : setCandidates) {
        if (randomToken < mapElementNearest.get(candidate)) {
          setMedoids.add(candidate);
          break;
        }
      }
    }

    return new ClusterPrototypes(setMedoids);
  }

  public static ClusterPrototypes initializeLinearly(final int k) {
    final Set<Integer> set = new HashSet<>();

    for (int i = 0; i < k; i++) {
      set.add(i);
    }

    return new ClusterPrototypes(set);
  }

  public final Set<Integer> getPrototypesIndices() {
    return Collections.unmodifiableSet(prototypes);
  }

  public final boolean isPrototype(final int index) {
    return prototypes.contains(index);
  }

  public final ClusterPrototypes swap(final int existing, final int other) {
    final Set<Integer> set = new HashSet<>(prototypes);
    set.remove(existing);
    set.add(other);
    return new ClusterPrototypes(set);
  }
}
