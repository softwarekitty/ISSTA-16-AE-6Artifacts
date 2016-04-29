package recreateArtifacts.similarityMatrix.row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import recreateArtifacts.similarityMatrix.BatchController;
import recreateArtifacts.similarityMatrix.RegexInputGroup;
import recreateArtifacts.similarityMatrix.row.cell.CellMeasuringTask;
import recreateArtifacts.similarityMatrix.row.cell.CellResult;

public class RowTask implements Callable<RowResult> {
	private final int rowIndex;
	private final String[] matchStrings;
	private final RegexInputGroup group;
	private final int rowTimeLimitMS;
	private final double minSimilarity;
	private final ExecutorService service;
	private final ScheduledExecutorService canceller;

	public RowTask(int rowIndex, String[] matchStrings, RegexInputGroup group, int rowTimeLimitMS, double minSimilarity,
			ExecutorService service, ScheduledExecutorService canceller) {
		this.rowIndex = rowIndex;
		this.matchStrings = matchStrings;
		this.group = group;
		this.rowTimeLimitMS = rowTimeLimitMS;
		this.minSimilarity = minSimilarity;
		this.service = service;
		this.canceller = canceller;
	}

	@Override
	public RowResult call() throws Exception {
		// initialize the row array
		int nKeys = group.size();
		double[] rowArray = new double[nKeys];
		for (int rowArrayIndex = 0; rowArrayIndex < nKeys; rowArrayIndex++) {
			rowArray[rowArrayIndex] = BatchController.INITIALIZED;
		}

		List<CellMeasuringTask> matchingTasks = new ArrayList<CellMeasuringTask>(nKeys);
		for (int colIndex = 0; colIndex < nKeys; colIndex++) {
			matchingTasks.add(
					new CellMeasuringTask(rowIndex, colIndex, minSimilarity, group.getRegex(colIndex), matchStrings));
		}

		HashMap<Integer, Future<CellResult>> rowIndexResultFutureMap = new HashMap<Integer, Future<CellResult>>();
		for (CellMeasuringTask task : matchingTasks) {
			rowIndexResultFutureMap.put(task.getColIndex(),
					BatchController.executeTask(task, rowTimeLimitMS, service, canceller));
		}

		for (Integer colIndex : rowIndexResultFutureMap.keySet()) {
			Future<CellResult> future = rowIndexResultFutureMap.get(colIndex);
			double cellValue = BatchController.INCOMPLETE;
			try {
				cellValue = future.get().getValue();
			} catch (CancellationException ce) {
				cellValue = BatchController.CANCELLED;
			}
			rowArray[colIndex] = cellValue;
		}
		return new RowResult(rowArray, rowIndex);
	}
}
