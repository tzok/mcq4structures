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

  public ClusterAssignment(int[] assignments, Map<Integer, List<Integer>> assignedToPrototype) {
    super();
    this.assignments = assignments.clone();
    this.assignedToPrototype = assignedToPrototype;
  }

  public static ClusterAssignment fromPrototypes(ClusterPrototypes prototypes, double[][] matrix) {
    int[] assignments = new int[matrix.length];
    Map<Integer, List<Integer>> assignedToPrototype = new HashMap<>();
    List<Heap> binaryHeaps = Heap.fromMatrix(matrix);

    for (int i = 0; i < matrix.length; i++) {
      if (prototypes.isPrototype(i)) {
        assignedToPrototype.put(i, new ArrayList<Integer>());
      }
    }

    for (int i = 0; i < matrix.length; i++) {
      for (int closest : binaryHeaps.get(i)) {
        if (prototypes.isPrototype(closest)) {
          assignments[i] = closest;
          assignedToPrototype.get(closest).add(i);
          break;
        }
      }
    }

    return new ClusterAssignment(assignments, assignedToPrototype);
  }

  public int getPrototype(int index) {
    return assignments[index];
  }

  public Set<Integer> getPrototypesIndices() {
    return Collections.unmodifiableSet(assignedToPrototype.keySet());
  }

  public List<Integer> getAssignedTo(int prototypeIndex) {
    return Collections.unmodifiableList(assignedToPrototype.get(prototypeIndex));
  }
}
