package pl.poznan.put.comparison;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.matching.StructureSelection;

public class ParallelGlobalComparator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelGlobalComparator.class);
    private static final ParallelGlobalComparator MCQ_INSTANCE = new ParallelGlobalComparator(GlobalComparisonMeasure.MCQ);
    private static final ParallelGlobalComparator RMSD_INSTANCE = new ParallelGlobalComparator(GlobalComparisonMeasure.RMSD);

    public static ParallelGlobalComparator getInstance(
            GlobalComparisonMeasure measure) {
        switch (measure) {
        case MCQ:
            return ParallelGlobalComparator.MCQ_INSTANCE;
        case RMSD:
            return ParallelGlobalComparator.RMSD_INSTANCE;
        default:
            throw new IllegalArgumentException("Unknown comparison measure: " + measure);
        }
    }

    private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private final ExecutorCompletionService<CompareCallable.SingleResult> executor = new ExecutorCompletionService<>(threadPool);
    private final GlobalComparisonMeasure measure;

    private ParallelGlobalComparator(GlobalComparisonMeasure measure) {
        this.measure = measure;
    }

    public GlobalComparisonResultMatrix run(
            List<StructureSelection> structures, ComparisonListener listener) {
        GlobalComparator comparator = measure.getComparator();
        int size = structures.size();
        long all = size * (size - 1) / 2;

        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                CompareCallable task = new CompareCallable(comparator, structures, i, j);
                executor.submit(task);
            }
        }

        GlobalComparisonResultMatrix matrix = new GlobalComparisonResultMatrix(comparator.getName(), size);
        Thread listenerThread = createListenerThread(structures, listener);
        listenerThread.start();

        for (int i = 0; i < all; i++) {
            try {
                CompareCallable.SingleResult result = executor.take().get();
                matrix.setResult(result.i, result.j, result.value);
            } catch (InterruptedException | ExecutionException e) {
                ParallelGlobalComparator.LOGGER.error("Failed to compare a pair of structures", e);
            }
        }

        listenerThread.interrupt();
        listener.stateChanged(all, all);

        return matrix;
    }

    private Thread createListenerThread(
            final List<StructureSelection> structures,
            final ComparisonListener listener) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int size = structures.size();
                    long all = size * (size - 1) / 2;

                    while (true) {
                        long completed = ((ThreadPoolExecutor) threadPool).getCompletedTaskCount();
                        listener.stateChanged(all, completed);
                        wait(1000);
                    }
                } catch (InterruptedException e) {
                    threadPool.shutdownNow();
                }
            }
        });
    }
}
