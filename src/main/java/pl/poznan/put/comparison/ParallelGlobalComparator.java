package pl.poznan.put.comparison;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.matching.StructureSelection;

public class ParallelGlobalComparator extends Thread {
    public interface ProgressListener {
        void setProgress(int progress);

        void complete(GlobalComparisonResultMatrix matrix);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelGlobalComparator.class);

    private final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private final ExecutorCompletionService<CompareCallable.SingleResult> executor = new ExecutorCompletionService<>(threadPool);

    private final GlobalComparisonMeasure measure;
    private final List<StructureSelection> structures;
    private final ProgressListener progressListener;

    public ParallelGlobalComparator(GlobalComparisonMeasure measure,
            List<StructureSelection> structures,
            ProgressListener progressListener) {
        this.measure = measure;
        this.structures = structures;
        this.progressListener = progressListener;
    }

    @Override
    public void run() {
        int size = structures.size();
        int all = size * (size - 1) / 2;
        GlobalComparator comparator = measure.getComparator();
        GlobalComparisonResultMatrix matrix = new GlobalComparisonResultMatrix(comparator.getName(), size);

        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                CompareCallable task = new CompareCallable(comparator, structures, i, j);
                executor.submit(task);
            }
        }

        long completed = 0;
        while ((completed = threadPool.getCompletedTaskCount()) < all) {
            progressListener.setProgress((int) completed);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                interrupt();
            }
        }
        progressListener.setProgress(all);

        for (int i = 0; i < all; i++) {
            try {
                CompareCallable.SingleResult result = executor.take().get();
                matrix.setResult(result.i, result.j, result.value);
            } catch (InterruptedException | ExecutionException e) {
                ParallelGlobalComparator.LOGGER.error("Failed to compare a pair of structures", e);
            }
        }

        progressListener.complete(matrix);
    }
}
