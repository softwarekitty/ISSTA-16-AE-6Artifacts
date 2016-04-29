package recreateArtifacts.similarityMatrix;

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
import java.util.concurrent.TimeUnit;

public class BatchController {
	
//	private static final int ROW_TIME_LIMIT_MS = 8000;
	private static final int CELL_TIME_LIMIT_MS = 8000;
	private static final int ROW_THREAD_COUNT = 64;

	// these seem like some common names - package visibility please
	public final static double INITIALIZED = 0.00000987654321;
	public final static double INCOMPLETE = 0.00000123456789;
	public final static double CANCELLED = 0.0000050101010101;
	public final static double VERIFIED_TIMEOUT = 0.00000701702703;
	public final static double BELOW_MIN = 0.00000307207107;

	public static void buildBatchOfRows(RegexInputGroup group, double minSimilarity, int[] batchIndices) throws Exception {

		// this needs to block anyway, so let it wait until things clear up
		ExecutorService service = Executors.newFixedThreadPool(ROW_THREAD_COUNT, new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setPriority(Thread.NORM_PRIORITY);
				t.setDaemon(true);
				return t;
			}
		});

		// but when we want to cancel, don't let it be starved
		ScheduledExecutorService canceller = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setPriority(Thread.MAX_PRIORITY);
				t.setDaemon(true);
				return t;
			}
		});

		for (int i = 0; i < batchIndices.length; i++) {
			int rowIndex = batchIndices[i];
			String[] matchStrings = group.getInputSetStrings(rowIndex);

			System.out.println("starting row: " + rowIndex);
			double[] rowArray = computeOneRow(rowIndex, matchStrings, group, CELL_TIME_LIMIT_MS, minSimilarity, service,
					canceller);
			group.setRow(rowIndex, rowArray, minSimilarity);
			System.out
					.println("completed i: " + rowIndex + "/" + group.size() + " nMatchStrings:" + matchStrings.length);

		}
		service.shutdownNow();
		canceller.shutdownNow();
		service.awaitTermination(10000, TimeUnit.MILLISECONDS);
		canceller.awaitTermination(10000, TimeUnit.MILLISECONDS);
	}

	public static double[] computeOneRow(int rowIndex, String[] matchStrings, RegexInputGroup group,
			int cellTimeLimitMS, double minSimilarity, ExecutorService service, ScheduledExecutorService canceller)
			throws Exception {
		// initialize the row array
		int nKeys = group.size();
		double[] rowArray = new double[nKeys];
		for (int rowArrayIndex = 0; rowArrayIndex < nKeys; rowArrayIndex++) {
			rowArray[rowArrayIndex] = INITIALIZED;
		}

		List<CellMeasuringTask> matchingTasks = new ArrayList<CellMeasuringTask>(nKeys);
		for (int colIndex = 0; colIndex < nKeys; colIndex++) {
			matchingTasks.add(
					new CellMeasuringTask(rowIndex, colIndex, minSimilarity, group.getRegex(colIndex), matchStrings));
		}

		HashMap<Integer, Future<CellResult>> rowIndexResultFutureMap = new HashMap<Integer, Future<CellResult>>();
		for (CellMeasuringTask task : matchingTasks) {
			rowIndexResultFutureMap.put(task.getColIndex(), executeTask(task, cellTimeLimitMS, service, canceller));
		}

		for (Integer colIndex : rowIndexResultFutureMap.keySet()) {
			Future<CellResult> future = rowIndexResultFutureMap.get(colIndex);
			double cellValue = INCOMPLETE;
			try {
				cellValue = future.get().getValue();
			} catch (CancellationException ce) {
				cellValue = CANCELLED;
			}
			rowArray[colIndex] = cellValue;
		}
		return rowArray;
	}
	
	public static void verifyBatchOfRows(RegexInputGroup group, double minSimilarity, int[] batchIndices) throws Exception {
		// TODO
	}

	public static Future<CellResult> executeTask(CellMeasuringTask task, int timeoutMS, ExecutorService service,
			ScheduledExecutorService canceller) {
		final Future<CellResult> future = service.submit(task);
		canceller.schedule(new Callable<Void>() {
			public Void call() {
				future.cancel(true);
				return null;
			}
		}, timeoutMS, TimeUnit.MILLISECONDS);
		return future;
	}
}
