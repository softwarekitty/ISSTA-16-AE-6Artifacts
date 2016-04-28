package recreateArtifacts.similarityMatrix.threading;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import recreateArtifacts.similarityMatrix.RowUtil;

public class CellMeasuringTask implements Callable<CellResult> {
	private final int rowIndex;
	private final int colIndex;
	private final double minSim;
	private final Regex regex;
	private final String[] matchStrings;
	private final int rowTimeLimitMS;
	private final ExecutorService service;
	private final ScheduledExecutorService canceller;

	public CellMeasuringTask(int rowIndex, int colIndex, double minSim, Regex regex, String[] matchStrings,
			int rowTimeLimitMS, ExecutorService service, ScheduledExecutorService canceller) {
		this.rowIndex = rowIndex;
		this.colIndex = colIndex;
		this.minSim = minSim;
		this.regex = regex;
		this.matchStrings = matchStrings;
		this.rowTimeLimitMS = rowTimeLimitMS;
		this.service = service;
		this.canceller = canceller;
	}

	public Integer getColIndex() {
		return colIndex;
	}

	@Override
	public CellResult call() throws Exception {
		double resultValue = RowUtil.INCOMPLETE;

		try {
			// above all, prefer canceling threads to starving the cancelers

			MatchTask[] matchTasks = new MatchTask[matchStrings.length];
			for (int i = 0; i < matchStrings.length; i++) {
				matchTasks[i] = new MatchTask(regex, matchStrings[i]);
			}

			List<Future<Boolean>> matchResults = new ArrayList<Future<Boolean>>();
			for (int i = 0; i < matchTasks.length; i++) {
				matchResults.add(CellUtil.executeTask(matchTasks[i], rowTimeLimitMS, service, canceller));
			}
			double alsoMatchingCounter = 0;
			int notMatchingCounter = 0;

			boolean allFinished = false;

			// for backing off, wait between 128 and 4096 millis
			int nChecksCounter = 0;
			int backoffAfterNChecks = (int) (matchStrings.length * 1.41);
			int currentBackoffExponent = 7;
			int maxExponent = 12;

			int maxNonMatches = CellUtil.getMaxNonMatches(minSim, matchStrings.length);
			Iterator<Future<Boolean>> it = matchResults.iterator();
			while (notMatchingCounter <= maxNonMatches && !allFinished) {
				if (matchResults.size() == 0) {
					allFinished = true;
				} else if (it.hasNext()) {
					Future<Boolean> result = it.next();
					if (result.isDone()) {
						Boolean matches = result.get();
						if (matches) {
							alsoMatchingCounter++;
						} else {
							notMatchingCounter++;
						}
						it.remove();
						backoffAfterNChecks--;
					} else if (result.isCancelled()) {
						notMatchingCounter++;
						it.remove();
						backoffAfterNChecks--;
					}
				} else {
					it = matchResults.iterator();
				}
				nChecksCounter++;
				if (nChecksCounter >= backoffAfterNChecks) {

					// quit looping for a moment
					Thread.sleep(2 ^ currentBackoffExponent);

					currentBackoffExponent = (currentBackoffExponent < maxExponent) ? currentBackoffExponent + 1
							: currentBackoffExponent;
					nChecksCounter = 0;
				}
			}

			if (notMatchingCounter > maxNonMatches) {
				resultValue = RowUtil.BELOW_MIN;
			} else {
				resultValue = alsoMatchingCounter / matchStrings.length;
			}
		} catch (Exception e) {
			System.err.println("unexpected exception in cell - row: " + rowIndex + " col: " + colIndex
					+ " exception type: " + e.toString());
			e.printStackTrace();
			return new CellResult(resultValue, colIndex, rowIndex);
		}
		return new CellResult(resultValue, colIndex, rowIndex);
	}
}
