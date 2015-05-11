package pl.poznan.put.comparison;

import java.util.List;
import java.util.concurrent.Callable;

import pl.poznan.put.matching.StructureSelection;

public class CompareCallable implements Callable<CompareCallable.SingleResult> {
    public class SingleResult {
        final int i;
        final int j;
        final GlobalComparisonResult value;

        public SingleResult(int i, int j, GlobalComparisonResult value) {
            this.i = i;
            this.j = j;
            this.value = value;
        }
    }

    private final GlobalComparator comparator;
    private final int row;
    private final int column;
    private final StructureSelection s1;
    private final StructureSelection s2;

    public CompareCallable(GlobalComparator comparator,
            List<StructureSelection> structures, int row, int column) {
        this.comparator = comparator;
        s1 = structures.get(row);
        s2 = structures.get(column);
        this.row = row;
        this.column = column;
    }

    @Override
    public SingleResult call() throws Exception {
        GlobalComparisonResult comp = comparator.compareGlobally(s1, s2);
        return new SingleResult(row, column, comp);
    }
}