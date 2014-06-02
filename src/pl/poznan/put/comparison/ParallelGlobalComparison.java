package pl.poznan.put.comparison;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.structure.StructureSelection;

/**
 * An abstraction of all global comparison measures.
 * 
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public class ParallelGlobalComparison {
    public interface ComparisonListener {
        void stateChanged(long all, long completed);
    }

    private static class CompareCallable implements Callable<SingleResult> {
        private final GlobalComparator comparator;
        private final int i;
        private final int j;
        private final StructureSelection s1;
        private final StructureSelection s2;

        public CompareCallable(GlobalComparator comparator,
                List<StructureSelection> structures, int i, int j) {
            this.comparator = comparator;
            s1 = structures.get(i);
            s2 = structures.get(j);
            this.i = i;
            this.j = j;
        }

        @Override
        public SingleResult call() throws Exception {
            try {
                GlobalComparisonResult comp = comparator.compareGlobally(s1, s2);
                return new SingleResult(i, j, comp);
            } catch (IncomparableStructuresException e) {
                ParallelGlobalComparison.LOGGER.error(
                        "Failed to compare structures: " + s1.getName()
                                + " and " + s2.getName(), e);
            }

            return null;
        }
    }

    private static class SingleResult {
        final int i;
        final int j;
        final GlobalComparisonResult value;

        public SingleResult(int i, int j, GlobalComparisonResult value) {
            this.i = i;
            this.j = j;
            this.value = value;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelGlobalComparison.class);
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private static final ExecutorCompletionService<SingleResult> EXECUTOR = new ExecutorCompletionService<>(
            ParallelGlobalComparison.THREAD_POOL);

    public static GlobalComparisonResultMatrix run(
            final GlobalComparator comparator,
            final List<StructureSelection> structures,
            final ComparisonListener listener) {
        /*
         * Create distance matrix, set diagonal to 0 and other values to NaN
         */
        final int size = structures.size();
        GlobalComparisonResultMatrix matrix = new GlobalComparisonResultMatrix(
                comparator.getName(), size);

        /*
         * Create a fixed pool of threads and a service to gather results from
         * each calculation
         */
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                CompareCallable task = new CompareCallable(comparator,
                        structures, i, j);
                ParallelGlobalComparison.EXECUTOR.submit(task);
            }
        }

        /*
         * In a separate thread, inform a listener about current status of
         * execution
         */
        final long all = size * (size - 1) / 2;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!ParallelGlobalComparison.THREAD_POOL.awaitTermination(
                            1, TimeUnit.SECONDS)) {
                        if (listener != null) {
                            long completed = ((ThreadPoolExecutor) ParallelGlobalComparison.THREAD_POOL).getCompletedTaskCount();
                            listener.stateChanged(all, completed);
                        }
                    }

                    if (listener != null) {
                        long completed = ((ThreadPoolExecutor) ParallelGlobalComparison.THREAD_POOL).getCompletedTaskCount();
                        listener.stateChanged(all, completed);
                    }
                } catch (InterruptedException e) {
                    ParallelGlobalComparison.THREAD_POOL.shutdownNow();
                }
            }
        });

        thread.start();

        /*
         * Finally gather the results back in the matrix
         */
        for (int i = 0; i < all; i++) {
            try {
                SingleResult result = ParallelGlobalComparison.EXECUTOR.take().get();
                matrix.setResult(result.i, result.j, result.value);
            } catch (InterruptedException | ExecutionException e) {
                ParallelGlobalComparison.LOGGER.error(
                        "Failed to compare a pair of structures", e);
            }
        }

        return matrix;
    }
}
