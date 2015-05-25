package pl.poznan.put.comparison.global;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.matching.StructureSelection;

public class ParallelGlobalComparator extends Thread {
    public class CompareCallable implements Callable<CompareCallable.SingleResult> {
        public class SingleResult {
            final int i;
            final int j;
            final GlobalResult value;

            public SingleResult(int i, int j, GlobalResult value) {
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
            GlobalResult comp = comparator.compareGlobally(s1, s2);
            return new SingleResult(row, column, comp);
        }
    }

    public interface ProgressListener {
        void setProgress(int progress);

        void complete(GlobalMatrix matrix);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelGlobalComparator.class);

    private final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private final ExecutorCompletionService<CompareCallable.SingleResult> executor = new ExecutorCompletionService<>(threadPool);

    private final MeasureType measure;
    private final List<StructureSelection> structures;
    private final ProgressListener progressListener;

    public ParallelGlobalComparator(MeasureType measure,
            List<StructureSelection> structures,
            ProgressListener progressListener) {
        this.measure = measure;
        this.structures = structures;
        this.progressListener = progressListener;
    }

    @Override
    public void run() {
        submitAll();
        waitForCompletion();

        List<String> names = collectNames();
        GlobalResult[][] results = fillResultsMatrix();
        GlobalMatrix matrix = new GlobalMatrix(measure, names, results);

        progressListener.complete(matrix);
    }

    private void submitAll() {
        GlobalComparator comparator = measure.getComparator();
        int size = structures.size();

        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                CompareCallable task = new CompareCallable(comparator, structures, i, j);
                executor.submit(task);
            }
        }
    }

    private void waitForCompletion() {
        int size = structures.size();
        long all = size * (size - 1) / 2;
        long completed = 0;

        while ((completed = threadPool.getCompletedTaskCount()) < all) {
            progressListener.setProgress((int) completed);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                interrupt();
            }
        }
        progressListener.setProgress((int) all);
    }

    private List<String> collectNames() {
        List<String> names = new ArrayList<>();
        for (StructureSelection selection : structures) {
            names.add(selection.getName());
        }
        return names;
    }

    private GlobalResult[][] fillResultsMatrix() {
        int size = structures.size();
        int all = size * (size - 1) / 2;

        GlobalResult[][] results = new GlobalResult[size][];
        for (int i = 0; i < size; i++) {
            results[i] = new GlobalResult[size];
        }

        for (int i = 0; i < all; i++) {
            try {
                CompareCallable.SingleResult result = executor.take().get();
                results[result.i][result.j] = results[result.j][result.i] = result.value;
            } catch (InterruptedException | ExecutionException e) {
                ParallelGlobalComparator.LOGGER.error("Failed to compare a pair of structures", e);
            }
        }

        return results;
    }
}
