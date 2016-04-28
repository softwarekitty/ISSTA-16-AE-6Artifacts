package recreateArtifacts.similarityMatrix.threading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import recreateArtifacts.similarityMatrix.MatrixRow;
import recreateArtifacts.similarityMatrix.RegexGroup;
import recreateArtifacts.similarityMatrix.RowUtil;

public class RegexRunner {

	public static void runBatchOfRows(int batchSize, String allRowsBase, RegexGroup group, String rexStringsBase,
			double minSimilarity, int nMatchStrings) throws Exception {
		int rowTimeLimitMS = 8000;

		boolean buildingRows = true;
		Integer[] batchIndices = CellUtil.getBatchOfIndices(allRowsBase, group.size(), batchSize, buildingRows);

		// this needs to block anyway, so let it wait until things clear up
		ExecutorService service = CellUtil.getCustomExecutorService(36, Thread.MIN_PRIORITY);
		
		// but when we want to cancel, don't let it be starved
		ScheduledExecutorService canceller = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setPriority(Thread.MAX_PRIORITY);
				t.setDaemon(true);
				return t;
			}
		});
//		List<RowTask> batchOfRowTasks = new ArrayList<RowTask>(batchSize);
		
		
		for (int i = 0; i < batchIndices.length; i++) {
			int rowIndex = batchIndices[i];
			String[] matchStrings = RowUtil.getRexGeneratedStrings(rowIndex, group.size(), rexStringsBase,
					nMatchStrings);
			
			System.out.println("starting row: "+rowIndex);
			RowResult result = computeOneRow(rowIndex, matchStrings, group, rowTimeLimitMS,
					minSimilarity, service, canceller);
			
			MatrixRow mr = new MatrixRow(rowIndex, result.getRowArray(), group.size());
			mr.writeRowToFile(allRowsBase, minSimilarity);
			System.out.println(
					"completed i: " + rowIndex + "/" + group.size() + " nMatchStrings:" + nMatchStrings);
			
		}
			// create the row task for each row in the batch
//			batchOfRowTasks.add(new RowTask(rowIndex, matchStrings, group, rowTimeLimitMS, minSimilarity, service, canceller));

//		List<Future<RowResult>> resultFutures = service.invokeAll(batchOfRowTasks);
//		for (Future<RowResult> resultFuture : resultFutures) {
//			
//			// can block here until the row gets cancelled
//			RowResult result = resultFuture.get();
//			int currentRowIndex = result.getRowIndex();
//
//			MatrixRow mr = new MatrixRow(currentRowIndex, result.getRowArray(), group.size());
//			mr.writeRowToFile(allRowsBase, minSimilarity);
//			System.out.println(
//					"completed i: " + currentRowIndex + "/" + group.size() + " nMatchStrings:" + nMatchStrings);
//		}
		service.shutdownNow();
		canceller.shutdownNow();
		service.awaitTermination(10000,TimeUnit.MILLISECONDS);
		canceller.awaitTermination(10000,TimeUnit.MILLISECONDS);
	}
	
	public static RowResult computeOneRow(int rowIndex, String[] matchStrings, RegexGroup group, int rowTimeLimitMS,
			double minSimilarity, ExecutorService service, ScheduledExecutorService canceller) throws Exception {
		// initialize the row array
		int nKeys = group.size();
		double[] rowArray = new double[nKeys];
		for (int rowArrayIndex = 0; rowArrayIndex < nKeys; rowArrayIndex++) {
			rowArray[rowArrayIndex] = RowUtil.INITIALIZED;
		}

		List<CellMeasuringTask> matchingTasks = new ArrayList<CellMeasuringTask>(nKeys);
		for (int colIndex = 0; colIndex < nKeys; colIndex++) {
			matchingTasks.add(new CellMeasuringTask(rowIndex, colIndex, minSimilarity, group.getRegex(colIndex),
					matchStrings, rowTimeLimitMS, service, canceller));
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
		return new RowResult(rowArray, rowIndex);
	}
}
