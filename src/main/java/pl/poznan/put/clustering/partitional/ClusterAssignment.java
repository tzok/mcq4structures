package pl.poznan.put.clustering.partitional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ClusterAssignment {
  private final int[] assignments;
  private final Map<Integer, ? extends List<Integer>> assignedToPrototype;

  private ClusterAssignment(
      final int[] assignments, final Map<Integer, ? extends List<Integer>> assignedToPrototype) {
    super();
    this.assignments = assignments.clone();
    this.assignedToPrototype = assignedToPrototype;
  }

  public static ClusterAssignment fromPrototypes(
      final ClusterPrototypes prototypes, final double[][] matrix) {
    final int[] assignments = new int[matrix.length];
    final Map<Integer, List<Integer>> assignedToPrototype;
    final List<Heap> binaryHeaps = Heap.fromMatrix(matrix);

      assignedToPrototype = IntStream.range(0, matrix.length).filter(prototypes::isPrototype).boxed().collect(Collectors.toMap(Function.identity(), i -> new ArrayList<>(), (a, b) -> b));

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

  public int getPrototype(final int index) {
    return assignments[index];
  }

  public Set<Integer> getPrototypesIndices() {
    return Collections.unmodifiableSet(assignedToPrototype.keySet());
  }

  public List<Integer> getAssignedTo(final int prototypeIndex) {
    return Collections.unmodifiableList(assignedToPrototype.get(prototypeIndex));
  }
}
