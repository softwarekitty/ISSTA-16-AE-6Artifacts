package recreateArtifacts.similarityMatrix.threading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class RowTask implements Callable<RowResult>{
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
			rowArray[rowArrayIndex] = CellUtil.INITIALIZED;
		}
		
		// we cannot finish a row until all the cells are done. each cell's matching tasks have priority.
		ExecutorService normalPriorityService = CellUtil.getCustomExecutorService(36, Thread.NORM_PRIORITY);
		List<CellMeasuringTask> matchingTasks = new ArrayList<CellMeasuringTask>(nKeys);
		for(int colIndex = 0;colIndex<nKeys;colIndex++){
			matchingTasks.set(colIndex, new CellMeasuringTask(rowIndex,colIndex,minSimilarity,regexMap.get(colIndex),matchStrings, rowTimeLimitMS));
		}
		
		// put off policing the timeouts to the lowest level.
		List<Future<CellResult>> results = normalPriorityService.invokeAll(matchingTasks);
		for(Future<CellResult> result : results){
			rowArray[result.get().getColIndex()] = result.get().getValue();
		}
		return new RowResult(rowArray,rowIndex);
	}

}
