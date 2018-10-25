package pl.poznan.put.clustering.partitional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClusterAssignment {
  private final int[] assignments;
  private final Map<Integer, List<Integer>> assignedToPrototype;

  private ClusterAssignment(
      final int[] assignments, final Map<Integer, List<Integer>> assignedToPrototype) {
    super();
    this.assignments = assignments.clone();
    this.assignedToPrototype = assignedToPrototype;
  }

  public static ClusterAssignment fromPrototypes(
      final ClusterPrototypes prototypes, final double[][] matrix) {
    final int[] assignments = new int[matrix.length];
    final Map<Integer, List<Integer>> assignedToPrototype = new HashMap<>();
    final List<Heap> binaryHeaps = Heap.fromMatrix(matrix);

    for (int i = 0; i < matrix.length; i++) {
      if (prototypes.isPrototype(i)) {
        assignedToPrototype.put(i, new ArrayList<>());
      }
    }

    for (int i = 0; i < matrix.length; i++) {
      for (final int closest : binaryHeaps.get(i)) {
        if (prototypes.isPrototype(closest)) {
          assignments[i] = closest;
          assignedToPrototype.get(closest).add(i);
          break;
        }
      }
    }

    return new ClusterAssignment(assignments, assignedToPrototype);
  }

  public final int getPrototype(final int index) {
    return assignments[index];
  }

  public final Set<Integer> getPrototypesIndices() {
    return Collections.unmodifiableSet(assignedToPrototype.keySet());
  }

  public final List<Integer> getAssignedTo(final int prototypeIndex) {
    return Collections.unmodifiableList(assignedToPrototype.get(prototypeIndex));
  }
}
