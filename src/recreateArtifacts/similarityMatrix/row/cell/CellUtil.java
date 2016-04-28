package recreateArtifacts.similarityMatrix.row.cell;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import recreateArtifacts.similarityMatrix.row.RowUtil;

public class CellUtil {
	
	public static Integer[] getBatchOfIndices(String allRowsBase, Integer nKeys, Integer batchSize,
			boolean buildingRows) throws IOException {
		Integer nAdded = 0;
		List<Integer> indices = new LinkedList<Integer>();
		for (Integer rowIndex = 0; rowIndex < nKeys; rowIndex++) {

			boolean includeInBatch = buildingRows ? !RowUtil.rowExists(allRowsBase, nKeys, rowIndex)
					: RowUtil.hasUnverifiedTimeouts(allRowsBase, nKeys, rowIndex);
			if (includeInBatch) {
				indices.add(rowIndex);
				nAdded++;
			}
			if (nAdded >= batchSize) {
				return indices.toArray(new Integer[indices.size()]);
			}
		}
		return indices.toArray(new Integer[indices.size()]);
	}

	// http://stackoverflow.com/questions/4819855/time-limit-on-individual-threads-with-executorservice
	public static <T> Future<T> executeTask(Callable<T> c, long timeoutMS, ExecutorService service,
			ScheduledExecutorService canceller) {
		final Future<T> future = service.submit(c);
		canceller.schedule(new Callable<Void>() {
			public Void call() {
				future.cancel(true);
				return null;
			}
		}, timeoutMS, TimeUnit.MILLISECONDS);
		return future;
	}
	
	public static ExecutorService getCustomExecutorService(int nThreads, int priority) {
		ExecutorService eService = Executors.newFixedThreadPool(nThreads, new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setPriority(priority);
				t.setDaemon(true);
				return t;
			}
		});
		return eService;
	}
	
	public static Integer getMaxNonMatches(double minSimilarity, Integer nMatchingStrings) {
		/**
		 * this is a simple calculation, but an important number, if we want to
		 * skip doing n calculations and claim that it is because that row would
		 * have turned out to be below the minimum similarity anyway.
		 */
		double partMaxNonMatch = 1 - minSimilarity;
		double maxNonMatchDouble = nMatchingStrings * partMaxNonMatch;

		// this may round down, so add one back just to be sure
		int nMaxNonMatch = (int) maxNonMatchDouble + 1;
		return nMaxNonMatch;
	}

}
