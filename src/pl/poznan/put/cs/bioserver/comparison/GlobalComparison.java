package pl.poznan.put.cs.bioserver.comparison;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.biojava.bio.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.cs.bioserver.helper.PdbManager;

/**
 * An abstraction of all global comparison measures.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public abstract class GlobalComparison {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GlobalComparison.class);

	private class CompareThread extends Thread {
		private Structure s1;
		private Structure s2;
		private double[][] result;
		private int i;
		private int j;
		public IncomparableStructuresException exception;

		public CompareThread(Structure[] structures, double[][] result, int i,
				int j) {
			s1 = structures[i];
			s2 = structures[j];
			this.result = result;
			this.i = i;
			this.j = j;
		}

		@Override
		public void run() {
			try {
				double value = compare(s1, s2);
				result[i][j] = value;
				result[j][i] = value;
			} catch (IncomparableStructuresException e) {
				exception = e;
			}
		}
	}

	/**
	 * Compare two structures.
	 * 
	 * @param s1
	 *            First structure.
	 * @param s2
	 *            Second structure.
	 * @return Distance between the structures according to some measure.
	 * @throws IncomparableStructuresException
	 *             If the two structure could not be compared.
	 */
	public abstract double compare(Structure s1, Structure s2)
			throws IncomparableStructuresException;

	/**
	 * Compare each structures with each other.
	 * 
	 * @param structures
	 *            An array of structures to be compared.
	 * @return A distance matrix.
	 * @throws IncomparableStructuresException
	 *             If any two structures were not comparable.
	 */
	public double[][] compare(final Structure[] structures,
			ComparisonListener listener) throws IncomparableStructuresException {
		final double[][] result = new double[structures.length][];
		for (int i = 0; i < structures.length; ++i) {
			result[i] = new double[structures.length];
		}

		final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors
				.newCachedThreadPool();

		final List<CompareThread> tasks = new ArrayList<>();
		Thread submit = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < structures.length; ++i) {
					for (int j = i + 1; j < structures.length; ++j) {
						GlobalComparison.LOGGER.trace("Comparing: "
								+ PdbManager.getStructureName(structures[i])
								+ " "
								+ PdbManager.getStructureName(structures[j]));

						CompareThread t = new CompareThread(structures, result,
								i, j);
						tasks.add(t);
						threadPool.execute(t);
					}
				}
				threadPool.shutdown();
			}
		});
		submit.start();

		try {
			long all = structures.length * (structures.length - 1) / 2;
			while (!threadPool.awaitTermination(1, TimeUnit.SECONDS)) {
				if (listener != null) {
					listener.stateChanged(all,
							threadPool.getCompletedTaskCount());
				}
			}
			if (listener != null) {
				listener.stateChanged(all, threadPool.getCompletedTaskCount());
			}
		} catch (InterruptedException e) {
			threadPool.shutdownNow();
			GlobalComparison.LOGGER.error("Failed to compare structures", e);
			throw new IncomparableStructuresException(e);
		}

		for (CompareThread thread : tasks) {
			if (thread.exception != null) {
				GlobalComparison.LOGGER.error("Failed to compare structures",
						thread.exception);
				throw thread.exception;
			}
		}

		return result;
	}
}
