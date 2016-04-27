package recreateArtifacts.similarityMatrix.threading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import recreateArtifacts.similarityMatrix.RowUtil;

public class RowTask implements Callable<RowResult> {
	private final int rowIndex;
	private final String[] matchStrings;
	private final HashMap<Integer, Regex> regexMap;
	private final int rowTimeLimitMS;
	private final double minSimilarity;

	public RowTask(int rowIndex, String[] matchStrings, HashMap<Integer, Regex> regexMap, int rowTimeLimitMS,
			double minSimilarity) {
		this.rowIndex = rowIndex;
		this.matchStrings = matchStrings;
		this.regexMap = regexMap;
		this.rowTimeLimitMS = rowTimeLimitMS;
		this.minSimilarity = minSimilarity;
	}

	@Override
	public RowResult call() throws Exception {
		// initialize the row array
		int nKeys = regexMap.size();
		double[] rowArray = new double[nKeys];
		for (int rowArrayIndex = 0; rowArrayIndex < nKeys; rowArrayIndex++) {
			rowArray[rowArrayIndex] = RowUtil.INITIALIZED;
		}

		// we cannot finish a row until all the cells are done. each cell's
		// matching tasks have priority.
		ExecutorService service = CellUtil.getCustomExecutorService(36, Thread.NORM_PRIORITY);

		// but when we want to cancel, don't let it be starved
		ScheduledExecutorService canceller = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setPriority(Thread.MAX_PRIORITY);
				t.setDaemon(true);
				return t;
			}
		});

		List<CellMeasuringTask> matchingTasks = new ArrayList<CellMeasuringTask>(nKeys);
		for (int colIndex = 0; colIndex < nKeys; colIndex++) {
			matchingTasks.set(colIndex, new CellMeasuringTask(rowIndex, colIndex, minSimilarity, regexMap.get(colIndex),
					matchStrings, rowTimeLimitMS));
		}

		// only cancel a row after a very long time - that's 20 minutes.
		int longTimeMS = 1000 * 60 * 20;
		HashMap<Integer, Future<CellResult>> rowIndexResultFutureMap = new HashMap<Integer, Future<CellResult>>();
		for (CellMeasuringTask task : matchingTasks) {
			rowIndexResultFutureMap.put(task.getColIndex(), RowUtil.executeTask(task, longTimeMS, service, canceller));
		}

		for (Integer colIndex : rowIndexResultFutureMap.keySet()) {
			Future<CellResult> future = rowIndexResultFutureMap.get(colIndex);
			double cellValue = RowUtil.INCOMPLETE;
			try{
				cellValue = future.get().getValue();
			}catch (CancellationException ce) {
				cellValue = RowUtil.CANCELLED;
			}
			rowArray[colIndex] = cellValue;
		}

		if (service != null) {
			service.shutdown();
		}
		if (canceller != null) {
			canceller.shutdown();
		}
		return new RowResult(rowArray, rowIndex);
	}

}
