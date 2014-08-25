package pl.poznan.put.clustering.partitional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClusterAssignment {
    public static ClusterAssignment fromPrototypes(
            ClusterPrototypes prototypes, double[][] matrix) {
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

    private final int[] assignments;
    private final Map<Integer, List<Integer>> assignedToPrototype;

    public ClusterAssignment(int[] assignments,
            Map<Integer, List<Integer>> assignedToPrototype) {
        super();
        this.assignments = assignments;
        this.assignedToPrototype = assignedToPrototype;
    }

    public int getCluster(int index) {
        return assignments[index];
    }

    public Set<Integer> getPrototypes() {
        return assignedToPrototype.keySet();
    }

    public int getPrototypesCount() {
        return assignedToPrototype.size();
    }

    public List<Integer> getAssignedTo(int prototype) {
        return assignedToPrototype.get(prototype);
    }

    public int getAssignedCount(int prototype) {
        return assignedToPrototype.get(prototype).size();
    }
}
